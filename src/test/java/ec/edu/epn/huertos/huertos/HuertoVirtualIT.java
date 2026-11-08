package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

/** Huerto virtual 2D en el panel del gestor: suelo, diseño, plantas plantadas y activas, progreso y aislamiento. */
@AutoConfigureMockMvc
class HuertoVirtualIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired HuertoServicio huertos;
    @Autowired HuertoRepository huertoRepository;
    @Autowired HuertoVirtualServicio virtual;
    @Autowired GamificacionServicio gamificacion;
    @Autowired RevisionServicio revision;
    @Autowired EnvioEvidenciaRepository envios;
    @Autowired MockMvc mvc;
    @Autowired CatalogoServicio catalogo;

    private FormularioPlanta planta(String nombre, int cantidad) {
        FormularioPlanta f = new FormularioPlanta();
        f.setNombreComun(nombre);
        f.setCantidad(cantidad);
        return f;
    }

    /** Crea la planta con el servicio y la marca como plantada (el servicio no limita cuántas). */
    private Long plantada(Escenario e, String nombre, int cantidad) {
        Long gestor = e.gestorUsuario().getId();
        Long id = huertos.crearPlanta(gestor, planta(nombre, cantidad)).getId();
        huertos.cambiarPlantada(gestor, id, true);
        return id;
    }

    /** Cambia suelo y diseño conservando el resto de datos del huerto (evita el formulario completo de comunidad). */
    private void sueloYDiseno(Escenario e, String suelo, String diseno) {
        Huerto h = huertoRepository.findById(e.huerto().getId()).orElseThrow();
        h.actualizar(h.getNombre(), h.getDescripcion(), h.getProvincia(), h.getParroquia(), h.getDireccion(),
                h.getAreaM2(), diseno, suelo, h.getFuenteAgua(), h.getAbonoNatural(),
                h.getLatitud(), h.getLongitud());
        huertoRepository.save(h);
    }

    private String panel(Escenario e) throws Exception {
        return mvc.perform(get("/gestor").with(user(new UsuarioAutenticado(e.gestorUsuario()))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    /** Solo el HTML del fragmento del huerto virtual dentro del panel. */
    private String fragmento(Escenario e) throws Exception {
        String html = panel(e);
        int inicio = html.indexOf("hv-huerto");
        assertThat(inicio).as("el panel incluye el fragmento").isPositive();
        return html.substring(inicio, html.indexOf("</section>", inicio));
    }

    private static int ocurrencias(String texto, String buscado) {
        return texto.split(java.util.regex.Pattern.quote(buscado), -1).length - 1;
    }

    @Test
    void conLosValoresPorDefectoSeDibujaSueloLimosoYComposicionDeTierra() throws Exception {
        Escenario e = fabrica.escenario(1);
        String html = fragmento(e);
        assertThat(html).contains("hv-suelo-limoso").contains("hv-composicion-tierra").contains("Suelo limoso")
                .contains("En tierra · surcos").doesNotContain("Falta registrar el tipo de suelo");
        assertThat(panel(e)).doesNotContain("Representación del huerto");
    }

    @Test
    void cadaTipoDeSueloTieneSuVariante() throws Exception {
        Escenario e = fabrica.escenario(1);
        for (String suelo : List.of("ARCILLOSO", "LIMOSO", "ARENOSO", "MARGOSO", "GREDOSO", "PANTANOSO")) {
            sueloYDiseno(e, suelo, "TIERRA_SURCOS");
            String html = fragmento(e);
            assertThat(html).as(suelo).contains("hv-suelo-" + suelo.toLowerCase())
                    .contains("Suelo " + suelo.toLowerCase()).doesNotContain("hv-suelo-neutro");
        }
    }

    @Test
    void losTreceDisenosSeAgrupanPorPrefijo() throws Exception {
        Escenario e = fabrica.escenario(1);
        Map<String, String> esperado = Map.ofEntries(
                Map.entry("TIERRA_SURCOS", "tierra"), Map.entry("TIERRA_CAMADAS", "tierra"),
                Map.entry("TIERRA_CAMELLONES", "tierra"), Map.entry("TIERRA_TERRAZAS", "tierra"),
                Map.entry("VERTICAL_PAREDES", "vertical"), Map.entry("COLGANTE_TECHOS", "colgante"),
                Map.entry("PISO_MACETAS", "piso"), Map.entry("PISO_BOTELLAS_PATIO", "piso"),
                Map.entry("PISO_LLANTAS_PATIO", "piso"), Map.entry("PISO_TERRAZA", "piso"),
                Map.entry("PISO_GRADAS", "piso"), Map.entry("PISO_BALCONES", "piso"),
                Map.entry("PISO_FILOS_VENTANA", "piso"));
        for (var par : esperado.entrySet()) {
            sueloYDiseno(e, "ARENOSO", par.getKey());
            String html = fragmento(e);
            assertThat(html).as(par.getKey()).contains("hv-composicion-" + par.getValue())
                    .contains("Diseño: " + HuertoVirtual.DISENOS.get(par.getKey()))
                    .doesNotContain("hv-composicion-neutra");
        }
    }

    @Test
    void soloAparecenPlantasPlantadasYActivasSinImportarLaCantidad() throws Exception {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        huertos.crearPlanta(gestor, planta("Ruda no plantada", 8));             // activa, no plantada
        Long manzanilla = plantada(e, "Manzanilla", 0);                          // cantidad 0 pero plantada
        Long menta = plantada(e, "Menta", 3);

        HuertoVirtual v = virtual.paraGestor(gestor);
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Manzanilla", "Menta");
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::sprite)
                .containsExactly(SpritePlanta.MANZANILLA, SpritePlanta.MENTA);
        assertThat(fragmento(e)).doesNotContain("Ruda no plantada");

        huertos.cambiarEstadoPlanta(gestor, menta, false);                       // desactivada: sale del dibujo
        assertThat(virtual.paraGestor(gestor).plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre)
                .containsExactly("Manzanilla");

        huertos.cambiarPlantada(gestor, manzanilla, false);                      // ya no plantada
        assertThat(virtual.paraGestor(gestor).plantas()).isEmpty();
    }

    @Test
    void sePuedenMarcarMasDeDosPlantadasYElDibujoMuestraDosEnOrdenConElTotal() throws Exception {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        // «plantada» es el estado real y no se limita: la tercera y la cuarta se aceptan por el mismo servicio.
        for (String n : List.of("Ruda", "Albahaca", "Menta")) plantada(e, n, 1);
        Long inactiva = plantada(e, "Aaa inactiva", 1);
        huertos.cambiarEstadoPlanta(gestor, inactiva, false);
        assertThat(huertos.plantasPlantadasDeGestor(gestor)).hasSize(4);

        HuertoVirtual v = virtual.paraGestor(gestor);
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Albahaca", "Menta");
        assertThat(v.totalPlantas()).isEqualTo(3);

        String html = fragmento(e);
        assertThat(ocurrencias(html, "class=\"hv-etiqueta-madera\"")).isEqualTo(2);
        assertThat(ocurrencias(html, "class=\"hv-sprite\"")).isEqualTo(2);
        assertThat(html).contains("Ilustración de Albahaca").contains("Ilustración de Menta")
                .doesNotContain("Ilustración de Ruda").doesNotContain("Aaa inactiva");
        assertThat(html.replaceAll("\\s+", " ")).contains("Mostrando <span>2</span> de <span>3</span> plantadas.");
    }

    @Test
    void sinPlantasSeMuestraLaParcelaVaciaConMensajeAmable() throws Exception {
        Escenario e = fabrica.escenario(1);
        String html = fragmento(e);
        assertThat(html).contains("Tu parcela está lista y esperando plantas").contains("/gestor/plantas")
                .contains("sin plantas plantadas").doesNotContain("Mostrando").doesNotContain("hasta dos");
    }

    @Test
    void usaIlustracionesConAlternativaTextualYNuncaFotos() throws Exception {
        Escenario e = fabrica.escenario(1);
        plantada(e, "Manzanilla", 2);
        fabrica.registrarActividad(e, e.participantes());
        String html = fragmento(e);
        assertThat(html).contains("class=\"hv-sprite\"").contains("Ilustración de Manzanilla")
                .contains("visually-hidden").contains("con 1 planta: Manzanilla")
                .doesNotContain("<img").doesNotContain("/evidencias/");
    }

    @Test
    void elProgresoSeLeeDeGamificacionYSoloAvanzaConAprobadas() throws Exception {
        Escenario e = fabrica.escenario(2);
        Long comunidad = e.comunidad().getId();
        Long gestor = e.gestorUsuario().getId();
        Long envio = envios.findByActividadIdOrderByNumeroAsc(fabrica.registrarActividad(e, e.participantes()))
                .get(0).getId();
        assertThat(virtual.paraGestor(gestor).progreso().progreso()).as("pendiente").isZero();

        revision.decidir(e.revisor().getId(), envio, EstadoRevision.RECHAZADA, "Foto borrosa");
        assertThat(virtual.paraGestor(gestor).progreso().progreso()).as("rechazada").isZero();

        Long envio2 = envios.findByActividadIdOrderByNumeroAsc(fabrica.registrarActividad(e, e.participantes()))
                .get(0).getId();
        revision.decidir(e.revisor().getId(), envio2, EstadoRevision.APROBADA, null);
        int tras = virtual.paraGestor(gestor).progreso().progreso();
        assertThat(tras).as("aprobada").isPositive().isEqualTo(gamificacion.progreso(comunidad).progreso());

        long puntos = gamificacion.puntosComunitarios(comunidad);
        panel(e);
        panel(e);
        assertThat(gamificacion.progreso(comunidad).progreso()).as("ver la vista no otorga nada").isEqualTo(tras);
        assertThat(gamificacion.puntosComunitarios(comunidad)).isEqualTo(puntos);
    }

    @Test
    void cadaGestorVeSoloSuHuertoYOtrosRolesNoAccedenAlPanel() throws Exception {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        plantada(a, "Planta exclusiva de A", 1);
        sueloYDiseno(a, "PANTANOSO", "COLGANTE_TECHOS");

        String deB = fragmento(b);
        assertThat(deB).doesNotContain("Planta exclusiva de A").doesNotContain("hv-suelo-pantanoso")
                .contains(b.huerto().getNombre());
        assertThat(fragmento(a)).contains("Planta exclusiva de A").contains("hv-composicion-colgante");

        assertThatThrownBy(() -> virtual.paraGestor(a.participanteUsuarios().get(0).getId()))
                .isInstanceOf(AccessDeniedException.class);
        mvc.perform(get("/gestor").with(user(new UsuarioAutenticado(a.participanteUsuarios().get(0)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void elDibujoUsaElNombreDeLaEspecieYCambiaAlRenombrarlaEnElCatalogo() throws Exception {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        plantada(e, "Albahaca", 1);
        plantada(e, "Menta", 1);
        Long ruda = plantada(e, "Ruda", 1);
        assertThat(virtual.paraGestor(gestor).plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre)
                .containsExactly("Albahaca", "Menta");

        // Se renombra la especie de «Ruda» en el catálogo; la columna planta.nombre_comun conserva «Ruda».
        Long especieId = huertos.plantaDeGestor(gestor, ruda).getEspecie().getId();
        FichaEspecie ficha = FichaEspecie.de(catalogo.obtener(gestor, especieId));
        ficha.setNombreComun("Anís");
        catalogo.guardar(gestor, especieId, ficha, null);

        HuertoVirtual v = virtual.paraGestor(gestor);
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Albahaca", "Anís");
        assertThat(fragmento(e)).contains("Ilustración de Anís").doesNotContain("Ilustración de Ruda")
                .doesNotContain("Ilustración de Menta");
    }

    @Test
    void paraComunidadEsSoloLecturaYNoOfreceEnlacesDeGestion() {
        Escenario e = fabrica.escenario(1);
        plantada(e, "Menta", 1);
        HuertoVirtual v = virtual.paraComunidad(e.comunidad().getId()).orElseThrow();
        assertThat(v.puedeRegistrar()).isFalse();
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Menta");
        assertThat(virtual.paraComunidad(-1L)).isEmpty();
    }
}
