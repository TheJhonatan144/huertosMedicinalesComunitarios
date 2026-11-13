package ec.edu.epn.huertos.arquitectura;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/** Contrato de organización de la suite: ubicación coherente y clasificación ejecutable. */
@Tag("arquitectura")
class OrganizacionPruebasTest {
    private static final String BASE = "ec.edu.epn.huertos";

    @Test
    void lasUnitariasNoArrancanSpringNiInyectanSuContexto() throws Exception {
        try (var archivos = Files.walk(Path.of("src/test/java"))) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith("Test.java")).toList()) {
                var paquete =
                        Pattern.compile("package\\s+([^;]+);").matcher(Files.readString(archivo));
                if (!paquete.find()) continue;
                String simple = archivo.getFileName().toString().replace(".java", "");
                Class<?> clase =
                        Class.forName(
                                paquete.group(1) + "." + simple,
                                false,
                                getClass().getClassLoader());
                boolean unitaria =
                        Arrays.stream(clase.getAnnotationsByType(Tag.class))
                                .anyMatch(t -> t.value().equals("unitaria"));
                if (!unitaria) continue;
                assertThat(
                                org.springframework.core.annotation.AnnotatedElementUtils
                                        .hasAnnotation(
                                                clase,
                                                org.springframework.boot.test.context.SpringBootTest
                                                        .class))
                        .as(simple)
                        .isFalse();
                for (var campo : clase.getDeclaredFields())
                    assertThat(
                                    campo.isAnnotationPresent(
                                            org.springframework.beans.factory.annotation.Autowired
                                                    .class))
                            .as(simple + "." + campo.getName())
                            .isFalse();
            }
        }
    }

    @Test
    void losPaquetesDePruebasReflejanModuloYCapa() throws Exception {
        List<String> errores = new ArrayList<>();
        Path raiz = Path.of("src/test/java");
        try (var archivos = Files.walk(raiz)) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                String texto = Files.readString(archivo);
                var paquete = Pattern.compile("package\\s+([^;]+);").matcher(texto);
                assertThat(paquete.find()).as(archivo.toString()).isTrue();
                String nombre = paquete.group(1);
                String ruta = nombre.replace('.', '/') + "/" + archivo.getFileName();
                if (!raiz.relativize(archivo).toString().replace('\\', '/').equals(ruta))
                    errores.add(archivo + ": paquete/ruta diferentes");
                String relativo = nombre.substring(BASE.length());
                if (relativo.startsWith(".soporte.")
                        || relativo.equals(".arquitectura")
                        || relativo.equals(".recorridos")) continue;
                String[] partes = relativo.replaceFirst("^\\.", "").split("\\.");
                if (partes.length < 2
                        || !Set.of(
                                        "aplicacion",
                                        "dominio",
                                        "persistencia",
                                        "presentacion",
                                        "configuracion")
                                .contains(partes[1]))
                    errores.add(archivo + ": falta módulo/capa o ubicación de soporte");
            }
        }
        assertThat(errores).isEmpty();
    }

    @Test
    void cadaClaseEjecutableDeclaraSuTipoDePrueba() throws Exception {
        List<String> errores = new ArrayList<>();
        try (var archivos = Files.walk(Path.of("src/test/java"))) {
            for (Path archivo :
                    archivos.filter(p -> p.toString().matches(".*(?:Test|IT)\\.java")).toList()) {
                var paquete =
                        Pattern.compile("package\\s+([^;]+);").matcher(Files.readString(archivo));
                if (!paquete.find()) continue;
                String simple = archivo.getFileName().toString().replace(".java", "");
                Class<?> clase =
                        Class.forName(
                                paquete.group(1) + "." + simple,
                                false,
                                getClass().getClassLoader());
                Set<String> tipos = new HashSet<>();
                for (Tag tag : clase.getAnnotationsByType(Tag.class)) tipos.add(tag.value());
                if (Collections.disjoint(
                        tipos, Set.of("unitaria", "web", "integracion", "arquitectura")))
                    errores.add(simple + ": sin tag de tipo");
                if (simple.endsWith("IT") && !tipos.contains("integracion"))
                    errores.add(simple + ": IT debe ser integración");
                if (simple.endsWith("Test") && tipos.contains("integracion"))
                    errores.add(simple + ": integración debe terminar en IT");
            }
        }
        assertThat(errores).isEmpty();
    }
}
