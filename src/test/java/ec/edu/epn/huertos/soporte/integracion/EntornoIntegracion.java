package ec.edu.epn.huertos.soporte.integracion;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/** Recursos exclusivos de la suite; nunca utiliza el directorio de evidencias de la aplicación. */
public final class EntornoIntegracion {
    public static final String ESQUEMA = "huertos_pruebas";
    private static final Path ARCHIVOS = crearDirectorio();

    private EntornoIntegracion() {}

    public static Path archivos() {
        return ARCHIVOS;
    }

    public static void limpiarArchivos() throws IOException {
        Files.walkFileTree(
                ARCHIVOS,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path archivo, BasicFileAttributes atributos)
                            throws IOException {
                        Files.delete(archivo);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path carpeta, IOException error)
                            throws IOException {
                        if (error != null) throw error;
                        if (!carpeta.equals(ARCHIVOS)) Files.delete(carpeta);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    private static Path crearDirectorio() {
        try {
            Path carpeta = Files.createTempDirectory("huertos-integracion-").toAbsolutePath();
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(
                                    () -> {
                                        try {
                                            limpiarArchivos();
                                            Files.deleteIfExists(carpeta);
                                        } catch (IOException error) {
                                            System.err.println(
                                                    "No se pudo retirar el directorio temporal de"
                                                        + " pruebas: "
                                                            + error.getMessage());
                                        }
                                    },
                                    "limpieza-archivos-prueba"));
            return carpeta;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear el almacenamiento aislado de pruebas", e);
        }
    }
}
