package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.PruebaIntegracion;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.evidencias.EnvioEvidenciaRepository;
import ec.edu.epn.huertos.evidencias.EstadoRevision;
import ec.edu.epn.huertos.evidencias.RevisionServicio;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Huerto virtual 2D: suelo, diseño, plantas plantadas y progreso (solo aprobadas), con datos reales. */
@AutoConfigureMockMvc
class HuertoVirtualIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired HuertoServicio huertos;
    @Autowired HuertoVirtualServicio virtual;
    @Autowired RevisionServicio revision;
    @Autowired EnvioEvidenciaRepository envios;
    @Autowired MockMvc mvc;

    private FormularioPlanta planta(String nombre, int cantidad) {
        FormularioPlanta f = new FormularioPlanta();
        f.setNombreComun(nombre);
        f.setCantidad(cantidad);
        return f;
    }

    private void datosHuerto(Escenario e, TipoSuelo suelo, DisenoHuerto diseno) {
        FormularioHuerto f = new FormularioHuerto();
        f.setNombre("Huerto de prueba");
        f.setDescripcion("Datos sintéticos");
        f.setTipoSuelo(suelo);
        f.setDiseno(diseno);
        huertos.actualizarHuerto(e.gestorUsuario().getId(), f);
    }

    private String paginaPublica(Escenario e) throws Exception {
        return mvc.perform(get("/comunidades/" + e.comunidad().getId())).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void sinSueloNiDisenoRegistradosSeUsaAparienciaNeutraYSeIndicaQueFalta() throws Exception {
        Escenario e = fabrica.escenario(1);
        HuertoVirtual v = virtual.paraComunidad(e.comunidad().getId(), false).orElseThrow();
        assertThat(v.suelo()).isNull();
        assertThat(v.diseno()).isNull();
        assertThat(v.camposFaltantes()).containsExactly("tipo de suelo", "diseño del huerto");

        String html = paginaPublica(e);
        assertThat(html).contains("suelo-neutro").contains("diseno-predeterminado").contains("Suelo sin registrar")
                .contains("Falta registrar").contains("tipo de suelo");
    }

    @Test
    void alCambiarElTipoDeSueloCambiaLaParcela() throws Exception {
        Escenario e = fabrica.escenario(1);
        for (TipoSuelo suelo : TipoSuelo.values()) {
            datosHuerto(e, suelo, DisenoHuerto.TIERRA);
            String html = paginaPublica(e);
            assertThat(html).as(suelo.name()).contains("suelo-" + suelo.name().toLowerCase()).contains(suelo.etiqueta());
            assertThat(html).doesNotContain("suelo-neutro");
        }
    }

    @Test
    void alCambiarElDisenoCambiaLaComposicion() throws Exception {
        Escenario e = fabrica.escenario(1);
        for (DisenoHuerto diseno : DisenoHuerto.values()) {
            datosHuerto(e, TipoSuelo.FRANCO, diseno);
            assertThat(paginaPublica(e)).as(diseno.name()).contains("diseno-" + diseno.name().toLowerCase())
                    .contains("Diseño: " + diseno.etiqueta());
        }
    }

    @Test
    void soloSeMuestranPlantasRegistradasComoPlantadas() {
        Escenario e = fabrica.escenario(1);
        Long comunidad = e.comunidad().getId();
        Long gestor = e.gestorUsuario().getId();
        assertThat(virtual.paraComunidad(comunidad, true).orElseThrow().plantas()).isEmpty();

        Long manzanilla = huertos.crearPlanta(gestor, planta("Manzanilla", 5)).getId();
        Long hierba = huertos.crearPlanta(gestor, planta("Hierba luisa", 3)).getId();
        huertos.crearPlanta(gestor, planta("Sin unidades", 0));   // cantidad 0: no está plantada
        assertThat(virtual.paraComunidad(comunidad, true).orElseThrow().plantas())
                .extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Hierba luisa", "Manzanilla");

        huertos.cambiarEstadoPlanta(gestor, hierba, false);        // desactivada: deja de aparecer
        HuertoVirtual v = virtual.paraComunidad(comunidad, true).orElseThrow();
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Manzanilla");
        assertThat(v.plantas().get(0).sprite()).isEqualTo(SpritePlanta.MANZANILLA);

        huertos.cambiarEstadoPlanta(gestor, manzanilla, false);
        assertThat(virtual.paraComunidad(comunidad, true).orElseThrow().plantas()).isEmpty();
    }

    @Test
    void laPrimeraVersionDibujaHastaDosPlantasEIndicaLasDemas() throws Exception {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        for (String n : List.of("Manzanilla", "Menta", "Ruda")) {
            huertos.crearPlanta(gestor, planta(n, 2));
        }
        HuertoVirtual v = virtual.paraComunidad(e.comunidad().getId(), false).orElseThrow();
        assertThat(v.plantas()).hasSize(2);
        assertThat(v.plantasNoMostradas()).isEqualTo(1);

        String html = paginaPublica(e);
        assertThat(html.split("class=\"etiqueta-madera\"", -1).length - 1).isEqualTo(2);
        // Exactamente un sprite por planta dibujada (no se mezclan ilustraciones de otras especies).
        assertThat(html.split("class=\"sprite\"", -1).length - 1).isEqualTo(2);
        assertThat(html).contains("Ilustración de Manzanilla").contains("Ilustración de Menta")
                .doesNotContain("Ilustración de Ruda");
        assertThat(html).contains("planta(s) más plantadas");
    }

    @Test
    void sinPlantasSeMuestraLaParcelaVaciaConMensajeAmable() throws Exception {
        Escenario e = fabrica.escenario(1);
        assertThat(paginaPublica(e)).contains("Tu parcela está lista y esperando plantas")
                .contains("aún no registra plantas plantadas");
        String panelGestor = mvc.perform(get("/gestor").with(user(new UsuarioAutenticado(e.gestorUsuario()))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(panelGestor).contains("Registrar una planta").contains("/gestor/plantas/nueva");
    }

    @Test
    void elHuertoVirtualUsaIlustracionesYNuncaLasFotosDeEvidencia() throws Exception {
        Escenario e = fabrica.escenario(1);
        huertos.crearPlanta(e.gestorUsuario().getId(), planta("Manzanilla", 2));
        fabrica.registrarActividad(e, e.participantes());
        String html = paginaPublica(e);
        assertThat(html).contains("class=\"sprite\"").contains("Ilustración de Manzanilla")
                .doesNotContain("/evidencias/").doesNotContain("<img");
    }

    @Test
    void elProgresoSoloAvanzaConActividadesAprobadas() {
        Escenario e = fabrica.escenario(2);
        Long comunidad = e.comunidad().getId();
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        Long envio = envios.findByActividadIdOrderByNumeroAsc(actividad).get(0).getId();
        assertThat(virtual.paraComunidad(comunidad, true).orElseThrow().progreso().progreso()).as("pendiente").isZero();

        revision.decidir(e.revisor().getId(), envio, EstadoRevision.RECHAZADA, "Foto borrosa");
        assertThat(virtual.paraComunidad(comunidad, true).orElseThrow().progreso().progreso()).as("rechazada").isZero();

        Long actividad2 = fabrica.registrarActividad(e, e.participantes());
        Long envio2 = envios.findByActividadIdOrderByNumeroAsc(actividad2).get(0).getId();
        revision.decidir(e.revisor().getId(), envio2, EstadoRevision.APROBADA, null);
        assertThat(virtual.paraComunidad(comunidad, true).orElseThrow().progreso().progreso()).as("aprobada").isEqualTo(5);
    }

    @Test
    void elGestorYElParticipanteVenElHuertoVirtualEnSusPaneles() throws Exception {
        Escenario e = fabrica.escenario(1);
        datosHuerto(e, TipoSuelo.HUMIFERO, DisenoHuerto.COLGANTE);
        huertos.crearPlanta(e.gestorUsuario().getId(), planta("Menta", 2));
        for (var usuario : List.of(e.gestorUsuario(), e.participanteUsuarios().get(0))) {
            String ruta = usuario.equals(e.gestorUsuario()) ? "/gestor" : "/participante";
            String html = mvc.perform(get(ruta).with(user(new UsuarioAutenticado(usuario)))).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(html).as(ruta).contains("suelo-humifero").contains("diseno-colgante").contains("Suelo rico en humus")
                    .contains("Menta").contains("Progreso del huerto");
        }
    }
}
