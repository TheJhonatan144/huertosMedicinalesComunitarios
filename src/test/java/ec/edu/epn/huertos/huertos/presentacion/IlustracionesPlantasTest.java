package ec.edu.epn.huertos.huertos.presentacion;

import static org.assertj.core.api.Assertions.assertThat;

import ec.edu.epn.huertos.huertos.aplicacion.SpritePlanta;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Cada clave de {@link SpritePlanta} tiene su SVG en el classpath, y ningún SVG de la carpeta trae
 * código ni enlaces.
 */
@org.junit.jupiter.api.Tag("unitaria")
class IlustracionesPlantasTest {

    private static final String CARPETA = "static/img/huerto/plantas/";

    private static final Pattern SCRIPT = Pattern.compile("<\\s*script", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENTO =
            Pattern.compile("\\son[a-z]+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern HREF =
            Pattern.compile(
                    "(?:xlink:)?href\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_CSS =
            Pattern.compile("url\\(\\s*['\"]?(?!#)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PELIGROSO =
            Pattern.compile(
                    "<\\s*(foreignObject|iframe|embed|object|image)\\b|<!ENTITY|<!DOCTYPE|@import|javascript:|https?://(?!www\\.w3\\.org/)",
                    Pattern.CASE_INSENSITIVE);

    private static String leer(String recurso) throws IOException {
        try (InputStream in =
                IlustracionesPlantasTest.class.getClassLoader().getResourceAsStream(recurso)) {
            assertThat(in).as("falta en el classpath: " + recurso).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void cadaClaveDeLaTablaTieneSuArchivoSvg() throws IOException {
        for (String clave : SpritePlanta.clavesConArchivo()) {
            assertThat(clave)
                    .as("clave apta para URL y nombre de archivo")
                    .matches("[a-z]+(-[a-z]+)*");
            String svg = leer(CARPETA + clave + ".svg");
            assertThat(svg).as(clave).contains("<svg").contains("viewBox=\"0 0 1024 1024\"");
        }
    }

    @Test
    void ningunSvgDeLaCarpetaTieneScriptsEventosNiEnlacesExternos()
            throws IOException, URISyntaxException {
        URL url = getClass().getClassLoader().getResource(CARPETA);
        assertThat(url).as("carpeta en el classpath").isNotNull();
        List<Path> svgs;
        try (Stream<Path> archivos = Files.list(Path.of(url.toURI()))) {
            svgs = archivos.filter(p -> p.toString().endsWith(".svg")).sorted().toList();
        }
        assertThat(svgs).hasSizeGreaterThanOrEqualTo(SpritePlanta.clavesConArchivo().size());
        for (Path p : svgs) {
            String svg = Files.readString(p, StandardCharsets.UTF_8);
            String nombre = p.getFileName().toString();
            assertThat(SCRIPT.matcher(svg).find()).as(nombre + ": <script").isFalse();
            assertThat(EVENTO.matcher(svg).find()).as(nombre + ": atributo on*").isFalse();
            assertThat(URL_CSS.matcher(svg).find()).as(nombre + ": url() externa").isFalse();
            assertThat(PELIGROSO.matcher(svg).find())
                    .as(nombre + ": elemento o enlace no permitido")
                    .isFalse();
            Matcher href = HREF.matcher(svg);
            while (href.find())
                assertThat(href.group(1)).as(nombre + ": href externo").startsWith("#");
        }
    }

    @Test
    void elDetectorReconoceLoQueDebeRechazar() {
        assertThat(SCRIPT.matcher("<svg><script>alert(1)</script></svg>").find()).isTrue();
        assertThat(EVENTO.matcher("<svg onload=\"x()\">").find()).isTrue();
        assertThat(EVENTO.matcher("<path d=\"M0 0\"/>").find()).isFalse();
        Matcher m = HREF.matcher("<use xlink:href=\"https://ejemplo.org/a.svg#b\"/>");
        assertThat(m.find()).isTrue();
        assertThat(m.group(1)).doesNotStartWith("#");
        assertThat(URL_CSS.matcher("fill=\"url(https://x/y)\"").find()).isTrue();
        assertThat(URL_CSS.matcher("fill=\"url(#degradado)\"").find()).isFalse();
        assertThat(PELIGROSO.matcher("<image href=\"data:image/png;base64,AAA\"/>").find())
                .isTrue();
        assertThat(PELIGROSO.matcher("<svg xmlns=\"http://www.w3.org/2000/svg\">").find())
                .isFalse();
    }
}
