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

    /** Especie del catálogo (la crea el administrador) seleccionada y marcada como plantada en el huerto del gestor. */
    private void delCatalogo(Escenario e, String nombreComun, String nombreCientifico) {
        FichaEspecie ficha = new FichaEspecie();
        ficha.setNombreComun(nombreComun);
        ficha.setNombreCientifico(nombreCientifico);
        Long especieId = catalogo.guardar(e.revisor().getId(), null, ficha, null).getId();
        SiembraPlanta siembra = new SiembraPlanta();
        siembra.setCantidad(1);
        siembra.setPlantada(true);
        catalogo.seleccionar(e.gestorUsuario().getId(), especieId, siembra);
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
                .containsExactly("manzanilla", "menta");
        assertThat(fragmento(e)).doesNotContain("Ruda no plantada");

        huertos.cambiarEstadoPlanta(gestor, menta, false);                       // desactivada: sale del dibujo
        assertThat(virtual.paraGestor(gestor).plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre)
                .containsExactly("Manzanilla");

        huertos.cambiarPlantada(gestor, manzanilla, false);                      // ya no plantada
        assertThat(virtual.paraGestor(gestor).plantas()).isEmpty();
    }

    @Test
    void laParcelaDibujaTodasLasPlantadasEnCuadriculaYSoloCortaPasadasLasNueve() throws Exception {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        for (String n : List.of("Ruda", "Albahaca", "Menta")) plantada(e, n, 1);
        Long inactiva = plantada(e, "Aaa inactiva", 1);
        huertos.cambiarEstadoPlanta(gestor, inactiva, false);
        assertThat(huertos.plantasPlantadasDeGestor(gestor)).hasSize(4);

        HuertoVirtual v = virtual.paraGestor(gestor);
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Albahaca", "Menta", "Ruda");
        assertThat(v.totalPlantas()).isEqualTo(3);
        assertThat(v.columnas()).isEqualTo(2);

        String html = fragmento(e);
        assertThat(ocurrencias(html, "class=\"hv-etiqueta-madera\"")).isEqualTo(3);
        assertThat(ocurrencias(html, "class=\"hv-sprite\"")).isEqualTo(3);
        assertThat(html).contains("hv-cols-2").contains("Ilustración de Ruda").doesNotContain("Aaa inactiva")
                .doesNotContain("Mostrando");

        for (int i = 0; i < 7; i++) plantada(e, "Zeta " + i, 1);                // 10 activas plantadas en total
        HuertoVirtual diez = virtual.paraGestor(gestor);
        assertThat(diez.plantas()).hasSize(9);
        assertThat(diez.totalPlantas()).isEqualTo(10);
        assertThat(diez.columnas()).isEqualTo(3);
        assertThat(fragmento(e).replaceAll("\\s+", " ")).contains("Mostrando <span>9</span> de <span>10</span> plantadas.");
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
        assertThat(html).contains("class=\"hv-sprite\"").contains("alt=\"Ilustración de Manzanilla\"")
                .contains("src=\"/img/huerto/plantas/manzanilla.svg\"")
                .contains("visually-hidden").contains("con 1 planta: Manzanilla")
                .doesNotContain("/evidencias/").doesNotContain(".jpg").doesNotContain(".png");
        assertThat(ocurrencias(html, "<img")).isEqualTo(1);
        // Sin dibujos incrustados en la página: los objetos de la escena son solo referencias <use> al archivo de objetos.
        assertThat(html).as("sin sprites incrustados").doesNotContain("<path").doesNotContain("class=\"hv-hoja\"");
    }

    @Test
    void conArchivoDibujaImgPorNombreCientificoYSinArchivoUsaLaGenerica() throws Exception {
        Escenario e = fabrica.escenario(1);
        // Nombre común fuera de la tabla: la ilustración sale del nombre científico que trae PlantaVirtualLectura.
        delCatalogo(e, "Menta piperita del barrio", "Mentha × piperita L.");
        delCatalogo(e, "Romero", "Salvia rosmarinus Spenn.");

        HuertoVirtual v = virtual.paraGestor(e.gestorUsuario().getId());
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Menta piperita del barrio", "Romero");
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::sprite).containsExactly("menta", SpritePlanta.GENERICA);

        String html = fragmento(e);
        java.util.regex.Matcher img = java.util.regex.Pattern.compile("<img[^>]*>").matcher(html);
        assertThat(img.find()).isTrue();
        String etiqueta = img.group();
        assertThat(etiqueta).contains("class=\"hv-sprite\"").contains("src=\"/img/huerto/plantas/menta.svg\"")
                .contains("alt=\"Ilustración de Menta piperita del barrio\"").contains("loading=\"lazy\"")
                .contains("decoding=\"async\"").contains("width=\"300\"").contains("height=\"300\"");
        assertThat(img.find()).as("solo la especie con archivo usa <img>").isFalse();
        // Romero no tiene archivo: sprite genérico incrustado, con su texto alternativo y su etiqueta.
        assertThat(html).contains("aria-label=\"Ilustración de Romero\"").contains("viewBox=\"0 0 120 120\"")
                .doesNotContain("romero.svg");
        assertThat(ocurrencias(html, "class=\"hv-etiqueta-madera\"")).isEqualTo(2);
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
                .containsExactly("Albahaca", "Menta", "Ruda");

        // Se renombra la especie de «Ruda» en el catálogo; la columna planta.nombre_comun conserva «Ruda».
        Long especieId = huertos.plantaDeGestor(gestor, ruda).getEspecie().getId();
        FichaEspecie ficha = FichaEspecie.de(catalogo.obtener(gestor, especieId));
        ficha.setNombreComun("Anís");
        catalogo.guardar(e.revisor().getId(), especieId, ficha, null);

        HuertoVirtual v = virtual.paraGestor(gestor);
        assertThat(v.plantas()).extracting(HuertoVirtual.PlantaVirtual::nombre).containsExactly("Albahaca", "Anís", "Menta");
        assertThat(fragmento(e)).contains("Ilustración de Anís").doesNotContain("Ilustración de Ruda");
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

    @Test
    void laEscenaEnTierraMuestraMedallaCaminoAlRankingYObjetosDeLaFinca() throws Exception {
        Escenario e = fabrica.escenario(1);
        plantada(e, "Menta", 1);
        // Cualquier diseño de huerto usa la misma escena en tierra en esta primera versión.
        sueloYDiseno(e, "ARENOSO", "PISO_MACETAS");
        String html = fragmento(e);
        assertThat(html).contains("hv-escena", "hv-etapa-0", "hv-medalla", "hv-camino", "hv-regadera", "hv-bomba",
                "escena-objetos.svg#trofeo", "escena-objetos.svg#regadera", "Cada actividad aprobada suma")
                .contains("hv-suelo-arenoso").contains("hv-composicion-piso");
        // El camino al ranking dice algo útil: lidera o cuánto falta para alcanzar el siguiente puesto.
        assertThat(html.replaceAll("\\s+", " ")).containsPattern("Lidera el ranking|Comparte el puesto #1|Faltan <strong>");
        // La medalla y el camino son lectura: no aparecen botones ni formularios dentro de la escena.
        assertThat(html).doesNotContain("<form").doesNotContain("<button");
    }

    @Test
    void elHuertoVacioMuestraBrotesYNoMariposasHastaCrecer() throws Exception {
        Escenario e = fabrica.escenario(1);
        String html = fragmento(e);
        assertThat(html).contains("hv-brotes", "hv-etapa-0").doesNotContain("hv-sprite-caja");
    }
}
