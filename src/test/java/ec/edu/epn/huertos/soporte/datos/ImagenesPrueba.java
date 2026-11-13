package ec.edu.epn.huertos.soporte.datos;

import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

/** Imágenes sintéticas deterministas; no inicia Spring ni depende del orden de pruebas. */
public final class ImagenesPrueba {
    private ImagenesPrueba() {}

    public static byte[] pngBytes(int ancho, int alto) {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                imagen.setRGB(x, y, (x * 7 + y * 13) & 0xFFFFFF);
            }
        }
        return codificar(imagen, "png");
    }

    public static byte[] jpegBytes(int ancho, int alto) {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                imagen.setRGB(x, y, (x * 3 + y * 5) & 0xFFFFFF);
            }
        }
        return codificar(imagen, "jpg");
    }

    public static MockMultipartFile foto() {
        return new MockMultipartFile("foto", "mi foto.png", "image/png", pngBytes(64, 48));
    }

    private static byte[] codificar(BufferedImage imagen, String formato) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, formato, salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
