package ec.edu.epn.huertos.publico.presentacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.epn.huertos.compartido.configuracion.SeguridadConfig;
import ec.edu.epn.huertos.comunidades.dominio.Comunidad;
import ec.edu.epn.huertos.gamificacion.aplicacion.FilaRanking;
import ec.edu.epn.huertos.gamificacion.aplicacion.ProgresoHuerto;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoVirtual;
import ec.edu.epn.huertos.huertos.dominio.EspacioHuerto;
import ec.edu.epn.huertos.huertos.dominio.Huerto;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.publico.aplicacion.PublicoServicio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Renderizado real de Thymeleaf y contratos públicos; no requiere PostgreSQL. */
@WebMvcTest(PublicoController.class)
@Import(SeguridadConfig.class)
@org.junit.jupiter.api.Tag("web")
class PublicoPaginasTest {
    @Autowired MockMvc mvc;
    @MockitoBean PublicoServicio publico;

    static final ProgresoHuerto PROGRESO =
            new ProgresoHuerto(35, 100, 35, "Planta joven", "Huerto floreciente", 25);

    private PublicoServicio.Ficha ficha(boolean conHuerto) {
        Comunidad c =
                new Comunidad(
                        "Sumak Allpa",
                        "Un espacio de encuentro para compartir saberes y cuidar la tierra.",
                        "Pichincha",
                        "Quito",
                        "099-PRIVADO",
                        "privado@example.test",
                        -.215,
                        -78.502);
        Huerto h =
                new Huerto(
                        1L,
                        "Huerto de las raíces",
                        "Cultivamos plantas y aprendemos en comunidad.",
                        -.214,
                        -78.501);
        Planta p = new Planta(1L, "Manzanilla", "Matricaria chamomilla", "", 4);
        p.cambiarPlantada(true);
        HuertoVirtual hv =
                new HuertoVirtual(
                        h.getNombre(),
                        "LIMOSO",
                        "TIERRA_SURCOS",
                        List.of(
                                new HuertoVirtual.PlantaVirtual("Manzanilla", "manzanilla"),
                                new HuertoVirtual.PlantaVirtual("Menta", "menta")),
                        2,
                        PROGRESO,
                        false);
        return fichaPublica(
                c,
                conHuerto ? h : null,
                conHuerto
                        ? List.of(
                                new EspacioHuerto(
                                        1L,
                                        "Jardín de aromáticas",
                                        "TIERRA_SURCOS",
                                        new BigDecimal("24"),
                                        ""))
                        : List.of(),
                conHuerto ? List.of(p) : List.of(),
                8,
                140,
                PROGRESO,
                1,
                conHuerto ? hv : null);
    }

    private String pagina(String ruta) throws Exception {
        return mvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                        "Content-Security-Policy",
                                        containsString("https://tile.openstreetmap.org ")))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void renderizaLasTresPantallasConEnlacesYFragmentoDeSoloLectura() throws Exception {
        when(publico.huertosParaMapa())
                .thenReturn(
                        List.of(
                                new PublicoServicio.HuertoMapa(
                                        1L,
                                        "Sumak Allpa",
                                        "Huerto de las raíces",
                                        "Pichincha",
                                        "Quito",
                                        -.214,
                                        -78.501,
                                        140,
                                        "Planta joven",
                                        35),
                                new PublicoServicio.HuertoMapa(
                                        2L,
                                        "Pachamama Wasi",
                                        "Jardín del encuentro",
                                        "Imbabura",
                                        "Otavalo",
                                        .234,
                                        -78.262,
                                        60,
                                        "Brote",
                                        15),
                                new PublicoServicio.HuertoMapa(
                                        3L,
                                        "Semillas del Austro",
                                        "Huerto del río",
                                        "Azuay",
                                        "Cuenca",
                                        -2.9,
                                        -79.004,
                                        0,
                                        "Semilla",
                                        0)));
        when(publico.ranking())
                .thenReturn(
                        List.of(
                                new FilaRanking(1, 1L, "Sumak Allpa", "Pichincha", 140),
                                new FilaRanking(2, 2L, "Pachamama Wasi", "Imbabura", 60)));
        when(publico.ficha(1L)).thenReturn(ficha(true));
        when(publico.ficha(2L))
                .thenReturn(
                        otraFicha(
                                "Pachamama Wasi",
                                "Imbabura",
                                "Otavalo",
                                "Jardín del encuentro",
                                .234,
                                -78.262,
                                60));
        when(publico.ficha(3L))
                .thenReturn(
                        otraFicha(
                                "Semillas del Austro",
                                "Azuay",
                                "Cuenca",
                                "Huerto del río",
                                -2.9,
                                -79.004,
                                0));
        String inicio = pagina("/");
        String mapa = pagina("/mapa");
        String detalle = pagina("/comunidades/1");
        String segunda = pagina("/comunidades/2");
        String tercera = pagina("/comunidades/3");
        assertThat(segunda)
                .contains("Pachamama Wasi", "Jardín del encuentro")
                .doesNotContain("Sumak Allpa");
        assertThat(tercera)
                .contains("Semillas del Austro", "Huerto del río")
                .doesNotContain("Sumak Allpa");
        assertThat(inicio)
                .contains(
                        "Descubrir los huertos",
                        "amazonia-1920.webp",
                        "selva amazónica",
                        "portada.js",
                        "href=\"/mapa\"",
                        "ALLPANA");
        assertThat(mapa)
                .contains(
                        "data-id=\"1\"",
                        "data-lat=\"-0.214\"",
                        "href=\"/comunidades/2\"",
                        "mapa-filtros");
        assertThat(detalle)
                .contains("Huerto virtual", "Manzanilla", "Menta", "Jardín de aromáticas")
                .doesNotContain(
                        "099-PRIVADO",
                        "privado@example.test",
                        "Ir a mis plantas",
                        "/gestor/huerto",
                        "/evidencias/envios/");
        // Exportación optativa para inspección visual local, usando las mismas plantillas
        // renderizadas.
        String salida = System.getProperty("publico.preview");
        if (salida != null) {
            Path dir = Path.of(salida);
            Files.createDirectories(dir.resolve("comunidades"));
            Files.writeString(dir.resolve("index.html"), inicio);
            Files.writeString(dir.resolve("mapa.html"), mapa);
            Files.writeString(dir.resolve("comunidades/1.html"), detalle);
            Files.writeString(dir.resolve("comunidades/2.html"), segunda);
            Files.writeString(dir.resolve("comunidades/3.html"), tercera);
        }
    }

    private PublicoServicio.Ficha otraFicha(
            String nombre,
            String provincia,
            String canton,
            String huerto,
            double lat,
            double lng,
            long puntos) {
        Comunidad c =
                new Comunidad(
                        nombre,
                        "Un huerto para aprender y compartir en comunidad.",
                        provincia,
                        canton,
                        lat,
                        lng);
        Huerto h = new Huerto(2L, huerto, "Un espacio de cuidado colectivo.", lat, lng);
        ProgresoHuerto p =
                puntos > 0
                        ? new ProgresoHuerto(15, 100, 15, "Brote", "Planta joven", 15)
                        : new ProgresoHuerto(0, 100, 0, "Semilla", "Brote", 10);
        HuertoVirtual hv =
                new HuertoVirtual(huerto, "LIMOSO", "TIERRA_SURCOS", List.of(), 0, p, false);
        return fichaPublica(c, h, List.of(), List.of(), 5, puntos, p, puntos > 0 ? 2 : 3, hv);
    }

    @Test
    void estadosVaciosSiguenSiendoUtilesSinHuertosNiPlantas() throws Exception {
        when(publico.huertosParaMapa()).thenReturn(List.of());
        when(publico.ranking()).thenReturn(List.of());
        when(publico.ficha(1L)).thenReturn(ficha(false));
        assertThat(pagina("/mapa")).contains("Todo empieza con una semilla");
        assertThat(pagina("/")).contains("Las primeras historias");
        assertThat(pagina("/comunidades/1"))
                .contains("Un huerto por comenzar", "Pronto, nuevas hojas.")
                .doesNotContain("hv-parcela");
    }

    @Test
    void escapaElContenidoDelMapaYNoAgregaDatosPrivadosEnLaApi() throws Exception {
        when(publico.huertosParaMapa())
                .thenReturn(
                        List.of(
                                new PublicoServicio.HuertoMapa(
                                        9L,
                                        "<img src=x onerror=alert(1)>",
                                        null,
                                        "Pichincha",
                                        "Quito",
                                        0,
                                        -78,
                                        0,
                                        "Semilla",
                                        0)));
        assertThat(pagina("/mapa"))
                .contains("&lt;img")
                .doesNotContain("<img src=x", "onerror=\"alert");
        String json = pagina("/api/publico/huertos");
        assertThat(json)
                .contains("comunidadId", "latitud")
                .doesNotContain(
                        "contactoTelefono",
                        "contactoCorreo",
                        "direccionExacta",
                        "claveAlmacenamiento");
    }

    private PublicoServicio.Ficha fichaPublica(
            Comunidad c,
            Huerto h,
            List<EspacioHuerto> zonas,
            List<Planta> plantas,
            long integrantes,
            long puntos,
            ProgresoHuerto progreso,
            Integer posicion,
            HuertoVirtual virtual) {
        return new PublicoServicio.Ficha(
                ec.edu.epn.huertos.comunidades.aplicacion.ComunidadVista.de(c),
                ec.edu.epn.huertos.huertos.aplicacion.HuertoVista.de(h),
                zonas.stream().map(ec.edu.epn.huertos.huertos.aplicacion.EspacioVista::de).toList(),
                plantas.stream()
                        .map(p -> ec.edu.epn.huertos.huertos.aplicacion.PlantaVista.de(p, null))
                        .toList(),
                integrantes,
                puntos,
                progreso,
                posicion,
                virtual);
    }
}
