package ec.edu.epn.huertos.evidencias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.FabricaDatos;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AlmacenamientoLocalTest {

    @TempDir
    Path directorio;

    private AlmacenamientoLocal almacenamiento() {
        return new AlmacenamientoLocal(new PropiedadesAlmacenamiento(directorio.toString(), 5_000_000, 8000, 25_000_000));
    }

    private ImagenValidada imagen() {
        return new ValidadorImagen(new PropiedadesAlmacenamiento(directorio.toString(), 5_000_000, 8000, 25_000_000))
                .validar(FabricaDatos.pngBytes(32, 32), "../../peligroso.png");
    }

    @Test
    void guardaConNombreInternoSeguroDentroDelDirectorio() throws IOException {
        AlmacenamientoLocal almacenamiento = almacenamiento();
        ImagenValidada imagen = imagen();
        String clave = almacenamiento.guardar(imagen);

        assertThat(clave).matches("\\d{4}/\\d{2}/[0-9a-f-]{36}\\.png").doesNotContain("peligroso").doesNotContain("..");
        Path archivo = directorio.resolve(clave);
        assertThat(archivo).exists().startsWith(directorio);
        assertThat(Files.readAllBytes(archivo)).isEqualTo(imagen.contenido());
        try (Stream<Path> restos = Files.walk(directorio)) {
            assertThat(restos.filter(p -> p.toString().endsWith(".tmp"))).isEmpty();
        }
    }

    @Test
    void losArchivosSobrevivenAUnReinicioDelAlmacenamiento() throws IOException {
        ImagenValidada original = imagen();
        String clave = almacenamiento().guardar(original);
        // Una instancia nueva (como tras reiniciar la aplicación) encuentra el mismo archivo.
        var recurso = almacenamiento().cargar(clave);
        assertThat(recurso.exists()).isTrue();
        assertThat(recurso.getContentAsByteArray()).isEqualTo(original.contenido());
    }

    @Test
    void rechazaClavesConRecorridoDeDirectorios() {
        AlmacenamientoLocal almacenamiento = almacenamiento();
        assertThatThrownBy(() -> almacenamiento.cargar("../../etc/passwd")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> almacenamiento.cargar("2026/01/../../../secreto.png")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> almacenamiento.eliminar("/absoluta.png")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> almacenamiento.cargar(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eliminaUnArchivoGuardado() {
        AlmacenamientoLocal almacenamiento = almacenamiento();
        String clave = almacenamiento.guardar(imagen());
        almacenamiento.eliminar(clave);
        assertThat(almacenamiento.cargar(clave).exists()).isFalse();
    }
}
