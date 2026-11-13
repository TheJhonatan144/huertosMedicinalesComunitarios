package ec.edu.epn.huertos.evidencias.aplicacion;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.evidencias.dominio.VideoValidado;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Valida MP4 en un archivo temporal y lee sus metadatos reales con ffprobe. */
@Component
public class ValidadorVideo {
    private final long maximoBytes;
    private final int maximoSegundos;

    public ValidadorVideo(@Value("${app.almacenamiento.video-tamano-maximo-bytes:52428800}") long maximoBytes,
                          @Value("${app.almacenamiento.video-duracion-maxima-segundos:120}") int maximoSegundos) {
        this.maximoBytes = maximoBytes;
        this.maximoSegundos = maximoSegundos;
    }

    public VideoValidado validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new ReglaNegocioException("Adjunta una foto o un video como evidencia.");
        if (archivo.getSize() > maximoBytes) throw new ReglaNegocioException("El video supera el límite de 50 MB.");
        Path temporal = null;
        try {
            temporal = Files.createTempFile("huerto-video-", ".mp4");
            archivo.transferTo(temporal);
            String metadatos;
            Process proceso = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries",
                    "format=format_name,duration:stream=codec_type,codec_name", "-of", "default=noprint_wrappers=1", temporal.toString())
                    .redirectErrorStream(true).start();
            if (!proceso.waitFor(15, TimeUnit.SECONDS)) {
                proceso.destroyForcibly();
                throw new ReglaNegocioException("No se pudo verificar el video. Inténtalo con un MP4 válido.");
            }
            metadatos = new String(proceso.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            if (proceso.exitValue() != 0 || !metadatos.contains("format_name=") || !metadatos.contains("mp4"))
                throw new ReglaNegocioException("El archivo debe ser un video MP4 válido.");
            if (!metadatos.contains("codec_type=video") || !metadatos.contains("codec_name=h264"))
                throw new ReglaNegocioException("El MP4 debe contener video H.264.");
            String valor = metadatos.lines().filter(l -> l.startsWith("duration=")).map(l -> l.substring(9)).findFirst()
                    .orElseThrow(() -> new ReglaNegocioException("No se pudo determinar la duración del video."));
            double segundos = Double.parseDouble(valor);
            if (segundos <= 0 || segundos > maximoSegundos)
                throw new ReglaNegocioException("El video debe durar como máximo 2 minutos.");
            return new VideoValidado(temporal, ValidadorImagen.nombreSeguro(archivo.getOriginalFilename()), archivo.getSize(),
                    sha256(temporal), (int) Math.ceil(segundos));
        } catch (ReglaNegocioException e) {
            eliminar(temporal); throw e;
        } catch (IOException e) {
            eliminar(temporal); throw new ReglaNegocioException("No se pudo verificar el video. Comprueba el archivo MP4.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); eliminar(temporal);
            throw new ReglaNegocioException("Se interrumpió la validación del video.");
        } catch (NumberFormatException e) {
            eliminar(temporal); throw new ReglaNegocioException("No se pudo verificar la duración del video.");
        }
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(path)) {
                byte[] buffer = new byte[64 * 1024]; int leidos;
                while ((leidos = in.read(buffer)) != -1) digest.update(buffer, 0, leidos);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    private static void eliminar(Path path) {
        if (path != null) try { Files.deleteIfExists(path); } catch (IOException ignored) { }
    }
}
