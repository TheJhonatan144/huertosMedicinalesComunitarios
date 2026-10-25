package ec.edu.epn.huertos.evidencias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ValidadorImagenTest {

    private final ValidadorImagen validador = new ValidadorImagen(
            new PropiedadesAlmacenamiento("ignorado", 200_000, 1000, 500_000));

    @Test
    void aceptaUnPngRealYDetectaSuTipoPorContenido() {
        // El navegador declara "application/octet-stream" y una extensión engañosa: no importa.
        var archivo = new MockMultipartFile("foto", "cualquiera.txt", "application/octet-stream",
                FabricaDatos.pngBytes(40, 30));
        ImagenValidada imagen = validador.validar(archivo);
        assertThat(imagen.tipoContenido()).isEqualTo("image/png");
        assertThat(imagen.extension()).isEqualTo("png");
        assertThat(imagen.sha256()).hasSize(64);
    }

    @Test
    void aceptaUnJpegReal() {
        ImagenValidada imagen = validador.validar(FabricaDatos.jpegBytes(80, 60), "foto.jpeg");
        assertThat(imagen.tipoContenido()).isEqualTo("image/jpeg");
        assertThat(imagen.extension()).isEqualTo("jpg");
    }

    @Test
    void rechazaTextoQueFingeSerImagenPorExtensionYContentType() {
        var falso = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", "esto no es una imagen".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> validador.validar(falso))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("JPEG o PNG");
    }

    @Test
    void rechazaFormatosNoPermitidosComoGif() {
        byte[] gif = "GIF89a\u0001\u0000\u0001\u0000".getBytes(StandardCharsets.ISO_8859_1);
        assertThatThrownBy(() -> validador.validar(gif, "animada.gif")).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazaUnaFirmaPngSeguidaDeBasura() {
        byte[] basura = new byte[200];
        Arrays.fill(basura, (byte) 7);
        byte[] firma = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] datos = new byte[firma.length + basura.length];
        System.arraycopy(firma, 0, datos, 0, firma.length);
        System.arraycopy(basura, 0, datos, firma.length, basura.length);
        assertThatThrownBy(() -> validador.validar(datos, "falsa.png")).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazaUnPngTruncado() {
        byte[] completo = FabricaDatos.pngBytes(200, 200);
        byte[] truncado = Arrays.copyOf(completo, completo.length / 2);
        assertThatThrownBy(() -> validador.validar(truncado, "corrupta.png")).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazaArchivosVaciosONulos() {
        assertThatThrownBy(() -> validador.validar(null)).isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Adjunta");
        assertThatThrownBy(() -> validador.validar(new MockMultipartFile("foto", new byte[0])))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazaArchivosQueSuperanElTamanoMaximo() {
        var limitado = new ValidadorImagen(new PropiedadesAlmacenamiento("x", 100, 1000, 500_000));
        assertThatThrownBy(() -> limitado.validar(FabricaDatos.pngBytes(40, 30), "grande.png"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("tamaño máximo");
    }

    @Test
    void rechazaDimensionesExcesivas() {
        var estricto = new ValidadorImagen(new PropiedadesAlmacenamiento("x", 200_000, 100, 500_000));
        assertThatThrownBy(() -> estricto.validar(FabricaDatos.pngBytes(300, 10), "ancha.png"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("dimensiones");
    }

    @Test
    void sanitizaElNombreOriginalYNuncaConservaRutas() {
        assertThat(ValidadorImagen.nombreSeguro("../../etc/passwd")).isEqualTo("passwd");
        assertThat(ValidadorImagen.nombreSeguro("C:\\Users\\x\\mi foto.png")).isEqualTo("mi foto.png");
        assertThat(ValidadorImagen.nombreSeguro("<script>alert(1)</script>.png")).doesNotContain("<").doesNotContain("/");
        assertThat(ValidadorImagen.nombreSeguro("..")).isEqualTo("evidencia");
        assertThat(ValidadorImagen.nombreSeguro(null)).isEqualTo("evidencia");
    }
}
