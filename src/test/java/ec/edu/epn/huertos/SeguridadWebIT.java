package ec.edu.epn.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.evidencias.EnvioEvidenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Permisos por rol, aislamiento entre comunidades, CSRF y privacidad de las fotografías (sobre HTTP). */
@AutoConfigureMockMvc
class SeguridadWebIT extends PruebaIntegracion {

    @Autowired MockMvc mvc;
    @Autowired FabricaDatos fabrica;
    @Autowired EnvioEvidenciaRepository envios;
    @Autowired UsuarioRepository usuarios;

    private RequestPostProcessor como(Usuario u) {
        return user(new UsuarioAutenticado(u));
    }

    @Test
    void elVisitanteConsultaMapaComunidadRankingYApiPublica() throws Exception {
        Escenario e = fabrica.escenario(2);
        for (String ruta : new String[] {"/", "/mapa", "/ranking", "/comunidades/" + e.comunidad().getId(), "/login"}) {
            mvc.perform(get(ruta)).andExpect(status().isOk());
        }
        mvc.perform(get("/api/publico/huertos")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(e.comunidad().getNombre())));
        mvc.perform(get("/comunidades/99999999")).andExpect(status().isNotFound());
    }

    @Test
    void lasFichasPublicasNoExponenDatosPersonales() throws Exception {
        Escenario e = fabrica.escenario(2);
        String html = mvc.perform(get("/comunidades/" + e.comunidad().getId())).andReturn().getResponse().getContentAsString();
        assertThat(html).doesNotContain(e.gestorUsuario().getNombreUsuario())
                .doesNotContain(e.participanteUsuarios().get(0).getNombreUsuario())
                .doesNotContain(e.participanteUsuarios().get(0).getNombreCompleto());
        String api = mvc.perform(get("/api/publico/huertos")).andReturn().getResponse().getContentAsString();
        assertThat(api).doesNotContain(e.participanteUsuarios().get(0).getNombreUsuario());
    }

    @Test
    void elVisitanteNoPuedeEntrarAAreasPrivadasNiVerEvidencias() throws Exception {
        for (String ruta : new String[] {"/gestor", "/gestor/actividades", "/gestor/actividades/nueva", "/participante",
                "/revision", "/revision/historial", "/evidencias/envios/1/foto", "/panel"}) {
            mvc.perform(get(ruta)).andExpect(status().is3xxRedirection())
                    .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).endsWith("/login"));
        }
    }

    @Test
    void cadaRolSoloAccedeASuPropiaArea() throws Exception {
        Escenario e = fabrica.escenario(2);
        Usuario participante = e.participanteUsuarios().get(0);

        mvc.perform(get("/gestor").with(como(participante))).andExpect(status().isForbidden());
        mvc.perform(get("/revision").with(como(participante))).andExpect(status().isForbidden());
        mvc.perform(get("/participante").with(como(participante))).andExpect(status().isOk());

        mvc.perform(get("/revision").with(como(e.gestorUsuario()))).andExpect(status().isForbidden());
        mvc.perform(get("/participante").with(como(e.gestorUsuario()))).andExpect(status().isForbidden());
        mvc.perform(get("/gestor").with(como(e.gestorUsuario()))).andExpect(status().isOk());

        mvc.perform(get("/gestor").with(como(e.revisor()))).andExpect(status().isForbidden());
        mvc.perform(get("/gestor/actividades/nueva").with(como(e.revisor()))).andExpect(status().isForbidden());
        mvc.perform(get("/revision").with(como(e.revisor()))).andExpect(status().isOk());
    }

    @Test
    void ungestorNoVeNiModificaRecursosDeOtraComunidad() throws Exception {
        Escenario a = fabrica.escenario(2);
        Escenario b = fabrica.escenario(2);
        Long actividadDeA = fabrica.registrarActividad(a, a.participantes());
        Long integranteDeA = a.participantes().get(0).getId();

        mvc.perform(get("/gestor/actividades/" + actividadDeA).with(como(b.gestorUsuario()))).andExpect(status().isForbidden());
        mvc.perform(get("/gestor/actividades/" + actividadDeA + "/editar").with(como(b.gestorUsuario()))).andExpect(status().isForbidden());
        mvc.perform(post("/gestor/actividades/" + actividadDeA + "/editar").with(como(b.gestorUsuario())).with(csrf())
                        .param("tipo", "RIEGO").param("descripcion", "hackeado").param("fecha", Fechas())).andExpect(status().isForbidden());
        mvc.perform(post("/gestor/integrantes/" + integranteDeA + "/baja").with(como(b.gestorUsuario())).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/gestor/actividades/" + actividadDeA).with(como(a.gestorUsuario()))).andExpect(status().isOk());
    }

    @Test
    void lasFotografiasSoloLasVenElGestorYElRevisorAsignado() throws Exception {
        Escenario a = fabrica.escenario(2);
        Escenario b = fabrica.escenario(2);
        Long actividad = fabrica.registrarActividad(a, a.participantes());
        Long envio = envios.findByActividadIdOrderByNumeroAsc(actividad).get(0).getId();
        String url = "/evidencias/envios/" + envio + "/foto";

        mvc.perform(get(url).with(como(a.revisor()))).andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
        mvc.perform(get(url).with(como(a.gestorUsuario()))).andExpect(status().isOk());

        mvc.perform(get(url).with(como(b.gestorUsuario()))).andExpect(status().isForbidden());
        mvc.perform(get(url).with(como(b.revisor()))).andExpect(status().isForbidden());
        mvc.perform(get(url).with(como(a.participanteUsuarios().get(0)))).andExpect(status().isForbidden());
        mvc.perform(get(url)).andExpect(status().is3xxRedirection());
        mvc.perform(get("/evidencias/envios/99999999/foto").with(como(a.revisor()))).andExpect(status().isNotFound());
    }

    @Test
    void lasFotografiasNoSeSirvenComoRecursoEstatico() throws Exception {
        for (String ruta : new String[] {"/evidencias/", "/datos/evidencias/", "/uploads/", "/static/evidencias/"}) {
            int estado = mvc.perform(get(ruta)).andReturn().getResponse().getStatus();
            assertThat(estado).isIn(302, 403, 404);
        }
    }

    @Test
    void lasPeticionesQueModificanDatosExigenTokenCsrf() throws Exception {
        Escenario e = fabrica.escenario(2);
        Long integrante = e.participantes().get(0).getId();
        mvc.perform(post("/gestor/integrantes/" + integrante + "/baja").with(como(e.gestorUsuario())))
                .andExpect(status().isForbidden());
        mvc.perform(post("/gestor/integrantes/" + integrante + "/baja").with(como(e.gestorUsuario())).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/logout").with(como(e.gestorUsuario()))).andExpect(status().isForbidden());
    }

    @Test
    void elInicioDeSesionFuncionaYRechazaCredencialesIncorrectas() throws Exception {
        Escenario e = fabrica.escenario(1);
        mvc.perform(post("/login").with(csrf()).param("username", e.gestorUsuario().getNombreUsuario())
                        .param("password", FabricaDatos.CLAVE))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/panel"));
        mvc.perform(post("/login").with(csrf()).param("username", e.gestorUsuario().getNombreUsuario())
                        .param("password", "incorrecta"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error"));
        mvc.perform(post("/login").param("username", e.gestorUsuario().getNombreUsuario()).param("password", FabricaDatos.CLAVE))
                .andExpect(status().isForbidden());
    }

    @Test
    void lasContrasenasSeGuardanConHashNuncaEnClaro() {
        Escenario e = fabrica.escenario(1);
        String hash = usuarios.findById(e.gestorUsuario().getId()).orElseThrow().getClaveHash();
        assertThat(hash).startsWith("{bcrypt}").doesNotContain(FabricaDatos.CLAVE);
    }

    private static String Fechas() {
        return ec.edu.epn.huertos.compartido.Fechas.hoy().toString();
    }
}
