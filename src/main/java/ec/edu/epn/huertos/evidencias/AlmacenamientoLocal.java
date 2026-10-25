package ec.edu.epn.huertos.evidencias;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Almacenamiento en disco fuera de los recursos estáticos. Los archivos se nombran con UUID y se guardan
 * en subcarpetas año/mes; la clave se valida con un patrón estricto antes de resolver cualquier ruta.
 */
@Component
public class AlmacenamientoLocal implements AlmacenamientoArchivos {

    private static final Pattern CLAVE = Pattern.compile("\\d{4}/\\d{2}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png)");

    private final Path base;

    public AlmacenamientoLocal(PropiedadesAlmacenamiento propiedades) {
        this.base = Path.of(propiedades.directorio()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de evidencias " + base, e);
        }
    }

    @Override
    public String guardar(ImagenValidada imagen) {
        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        String clave = "%04d/%02d/%s.%s".formatted(hoy.getYear(), hoy.getMonthValue(), UUID.randomUUID(), imagen.extension());
        Path destino = resolver(clave);
        try {
            Files.createDirectories(destino.getParent());
            Path temporal = Files.createTempFile(destino.getParent(), "subida-", ".tmp");
            try {
                Files.write(temporal, imagen.contenido());
                try {
                    Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporal, destino);
                }
            } finally {
                Files.deleteIfExists(temporal);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la fotografía", e);
        }
        return clave;
    }

    @Override
    public Resource cargar(String clave) {
        return new FileSystemResource(resolver(clave));
    }

    @Override
    public void eliminar(String clave) {
        try {
            Files.deleteIfExists(resolver(clave));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolver(String clave) {
        if (clave == null || !CLAVE.matcher(clave).matches()) {
            throw new IllegalArgumentException("Clave de almacenamiento no válida");
        }
        Path ruta = base.resolve(clave).normalize();
        if (!ruta.startsWith(base)) {
            throw new IllegalArgumentException("Ruta fuera del directorio de evidencias");
        }
        return ruta;
    }
}
