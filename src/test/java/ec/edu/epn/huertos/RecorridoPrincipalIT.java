package ec.edu.epn.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.evidencias.EnvioEvidenciaRepository;
import ec.edu.epn.huertos.gamificacion.RecompensaComunitariaRepository;
import ec.edu.epn.huertos.gamificacion.RecompensaIndividualRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Recorrido completo por HTTP: visitante, registro, rechazo con reenvío, aprobación y consulta de resultados. */
@AutoConfigureMockMvc
class RecorridoPrincipalIT extends PruebaIntegracion {

    @Autowired MockMvc mvc;
    @Autowired FabricaDatos fabrica;
    @Autowired EnvioEvidenciaRepository envios;
    @Autowired RecompensaIndividualRepository individuales;
    @Autowired RecompensaComunitariaRepository comunitarias;

    private RequestPostProcessor como(Usuario u) {
        return user(new UsuarioAutenticado(u));
    }

    @Test
    void recorridoDeVisitanteAAprobacionConRechazoYReenvio() throws Exception {
        Escenario e = fabrica.escenario(3);
        Long comunidadId = e.comunidad().getId();

        // 1. El visitante consulta la comunidad.
        mvc.perform(get("/comunidades/" + comunidadId)).andExpect(status().isOk());

        // 2. El gestor registra una actividad con participantes y fotografía.
        var foto = new MockMultipartFile("foto", "riego.png", "image/png", FabricaDatos.pngBytes(50, 40));
        String ubicacion = mvc.perform(multipart("/gestor/actividades").file(foto).with(como(e.gestorUsuario())).with(csrf())
                        .param("tipo", "RIEGO").param("descripcion", "Riego manual por la mañana")
                        .param("fecha", Fechas.hoy().toString())
                        .param("participanteIds", e.participantes().get(0).getId().toString(),
                                e.participantes().get(1).getId().toString()))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        Long actividadId = Long.valueOf(ubicacion.substring(ubicacion.lastIndexOf('/') + 1));
        assertThat(comunitarias.totalPuntos(comunidadId)).as("antes de aprobar").isZero();

        // 3. El revisor asignado la ve en su bandeja.
        Long envio1 = envios.findByActividadIdOrderByNumeroAsc(actividadId).get(0).getId();
        String bandeja = mvc.perform(get("/revision").with(como(e.revisor()))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(bandeja).contains("/revision/envios/" + envio1);
        mvc.perform(get("/revision/envios/" + envio1).with(como(e.revisor()))).andExpect(status().isOk());

        // 4. Rechazo sin observaciones: no procede. Con observaciones: sí.
        mvc.perform(post("/revision/envios/" + envio1 + "/decision").with(como(e.revisor())).with(csrf())
                        .param("decision", "RECHAZADA").param("observaciones", ""))
                .andExpect(redirectedUrl("/revision/envios/" + envio1)).andExpect(flash().attributeExists("error"));
        mvc.perform(post("/revision/envios/" + envio1 + "/decision").with(como(e.revisor())).with(csrf())
                        .param("decision", "RECHAZADA").param("observaciones", "La foto no muestra el huerto"))
                .andExpect(redirectedUrl("/revision")).andExpect(flash().attributeExists("mensaje"));
        assertThat(comunitarias.totalPuntos(comunidadId)).as("tras rechazar").isZero();

        // 5. El gestor ve las observaciones y reenvía una corrección.
        String detalle = mvc.perform(get("/gestor/actividades/" + actividadId).with(como(e.gestorUsuario())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(detalle).contains("La foto no muestra el huerto").contains("Corregir y reenviar");
        var fotoNueva = new MockMultipartFile("foto", "riego2.png", "image/png", FabricaDatos.pngBytes(60, 40));
        mvc.perform(multipart("/gestor/actividades/" + actividadId + "/reenviar").file(fotoNueva)
                        .with(como(e.gestorUsuario())).with(csrf())
                        .param("tipo", "RIEGO").param("descripcion", "Riego manual por la mañana")
                        .param("fecha", Fechas.hoy().toString()).param("comentario", "Nueva foto"))
                .andExpect(redirectedUrl("/gestor/actividades/" + actividadId)).andExpect(flash().attributeExists("mensaje"));
        assertThat(envios.findByActividadIdOrderByNumeroAsc(actividadId)).hasSize(2);

        // 6. El revisor aprueba; un segundo intento no duplica nada.
        Long envio2 = envios.findByActividadIdOrderByNumeroAsc(actividadId).get(1).getId();
        mvc.perform(post("/revision/envios/" + envio2 + "/decision").with(como(e.revisor())).with(csrf()).param("decision", "APROBADA"))
                .andExpect(redirectedUrl("/revision")).andExpect(flash().attributeExists("mensaje"));
        mvc.perform(post("/revision/envios/" + envio2 + "/decision").with(como(e.revisor())).with(csrf()).param("decision", "APROBADA"))
                .andExpect(redirectedUrl("/revision")).andExpect(flash().attributeExists("error"));
        assertThat(individuales.findByActividadId(actividadId)).hasSize(2);
        assertThat(comunitarias.totalPuntos(comunidadId)).isEqualTo(20);

        // 7. Resultados: participante seleccionado (10), no seleccionado (0), ranking y detalle del gestor.
        String panelSeleccionado = mvc.perform(get("/participante").with(como(e.participanteUsuarios().get(0))))
                .andReturn().getResponse().getContentAsString();
        assertThat(panelSeleccionado).contains("Aprendiz").containsPattern("display-6\">10<");
        String panelNoSeleccionado = mvc.perform(get("/participante").with(como(e.participanteUsuarios().get(2))))
                .andReturn().getResponse().getContentAsString();
        assertThat(panelNoSeleccionado).containsPattern("display-6\">0<");
        assertThat(mvc.perform(get("/ranking")).andReturn().getResponse().getContentAsString())
                .contains(e.comunidad().getNombre());
        assertThat(mvc.perform(get("/comunidades/" + comunidadId)).andReturn().getResponse().getContentAsString())
                .contains("puntos comunitarios").contains("Semilla");
        String detalleFinal = mvc.perform(get("/gestor/actividades/" + actividadId).with(como(e.gestorUsuario())))
                .andReturn().getResponse().getContentAsString();
        assertThat(detalleFinal).contains("Aprobada").contains("La foto no muestra el huerto");
    }
}
