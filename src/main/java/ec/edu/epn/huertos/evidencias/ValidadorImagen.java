package ec.edu.epn.huertos.evidencias;

import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Valida fotografías por su contenido: firma binaria (JPEG o PNG), coherencia con el decodificador,
 * dimensiones máximas y decodificación completa (con submuestreo para acotar memoria).
 * Ignora la extensión y el Content-Type enviados por el cliente.
 */
@Component
public class ValidadorImagen {

    private static final String MENSAJE_FORMATO = "El archivo no es una fotografía JPEG o PNG válida.";

    private final PropiedadesAlmacenamiento propiedades;

    public ValidadorImagen(PropiedadesAlmacenamiento propiedades) {
        this.propiedades = propiedades;
    }

    public ImagenValidada validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Adjunta una fotografía como evidencia.");
        }
        if (archivo.getSize() > propiedades.tamanoMaximoBytes()) {
            throw excesoDeTamano();
        }
        byte[] datos;
        try {
            datos = archivo.getBytes();
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo leer el archivo enviado.");
        }
        return validar(datos, archivo.getOriginalFilename());
    }

    public ImagenValidada validar(byte[] datos, String nombreOriginal) {
        if (datos.length > propiedades.tamanoMaximoBytes()) {
            throw excesoDeTamano();
        }
        String tipo = detectarTipo(datos);
        if (tipo == null) {
            throw new ReglaNegocioException(MENSAJE_FORMATO);
        }
        try {
            comprobarDecodificacion(datos, tipo);
        } catch (ReglaNegocioException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new ReglaNegocioException(MENSAJE_FORMATO);
        }
        String extension = tipo.equals("image/png") ? "png" : "jpg";
        return new ImagenValidada(datos, tipo, extension, sha256(datos), nombreSeguro(nombreOriginal));
    }

    private ReglaNegocioException excesoDeTamano() {
        return new ReglaNegocioException("La fotografía supera el tamaño máximo de "
                + propiedades.tamanoMaximoBytes() / (1024 * 1024) + " MB.");
    }

    private static String detectarTipo(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        if (d.length >= png.length) {
            for (int i = 0; i < png.length; i++) {
                if (d[i] != png[i]) {
                    return null;
                }
            }
            return "image/png";
        }
        return null;
    }

    private void comprobarDecodificacion(byte[] datos, String tipo) throws IOException {
        try (ImageInputStream flujo = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(flujo);
            if (!lectores.hasNext()) {
                throw new ReglaNegocioException(MENSAJE_FORMATO);
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(flujo, true, true);
                String formato = lector.getFormatName().toLowerCase();
                boolean coherente = tipo.equals("image/png") ? formato.equals("png") : formato.startsWith("jpeg") || formato.equals("jpg");
                if (!coherente) {
                    throw new ReglaNegocioException(MENSAJE_FORMATO);
                }
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho <= 0 || alto <= 0 || ancho > propiedades.ladoMaximoPx() || alto > propiedades.ladoMaximoPx()
                        || (long) ancho * alto > propiedades.pixelesMaximos()) {
                    throw new ReglaNegocioException("Las dimensiones de la fotografía exceden el límite permitido.");
                }
                ImageReadParam parametros = lector.getDefaultReadParam();
                int paso = Math.max(1, (int) Math.ceil(Math.max(ancho, alto) / 512.0));
                parametros.setSourceSubsampling(paso, paso, 0, 0);
                lector.read(0, parametros);
            } finally {
                lector.dispose();
            }
        }
    }

    /** Conserva solo el último segmento del nombre y caracteres inocuos; es información de apoyo, no una ruta. */
    static String nombreSeguro(String original) {
        if (original == null || original.isBlank()) {
            return "evidencia";
        }
        String base = original.substring(Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\')) + 1);
        base = base.replaceAll("[^\\p{L}\\p{N} ._-]", "_").strip();
        if (base.length() > 100) {
            base = base.substring(0, 100);
        }
        return base.isBlank() || base.chars().allMatch(c -> c == '.') ? "evidencia" : base;
    }

    private static String sha256(byte[] datos) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(datos));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
