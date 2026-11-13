package ec.edu.epn.huertos.arquitectura;

import static org.assertj.core.api.Assertions.assertThat;

import ec.edu.epn.huertos.HuertosApplication;

import jakarta.persistence.Entity;

import org.junit.jupiter.api.Test;
import org.springframework.asm.*;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Reglas ejecutables sobre bytecode real; no requieren Docker ni dependencias de análisis
 * adicionales.
 */
@org.junit.jupiter.api.Tag("arquitectura")
class ArquitecturaTest {
    private static final String BASE = "ec.edu.epn.huertos.";

    @Test
    void losServiciosDefinenLaFronteraTransaccional() throws Exception {
        for (Class<?> c : clases())
            if (c.isAnnotationPresent(org.springframework.stereotype.Service.class)) {
                assertThat(
                                c.isAnnotationPresent(
                                        org.springframework.transaction.annotation.Transactional
                                                .class))
                        .as(c.getName() + " debe declarar su transacción por defecto")
                        .isTrue();
            }
    }

    @Test
    void laPresentacionTieneUnaUnicaUbicacionDeRecursos() throws Exception {
        for (String antigua :
                List.of("src/main/resources/templates", "src/main/resources/static")) {
            Path carpeta = Path.of(antigua);
            if (!Files.exists(carpeta)) continue;
            try (var archivos = Files.walk(carpeta)) {
                assertThat(archivos.filter(Files::isRegularFile).toList()).as(antigua).isEmpty();
            }
        }
    }

    private List<Class<?>> clases() throws Exception {
        Path raiz =
                Path.of(
                        HuertosApplication.class
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .toURI());
        try (var archivos = Files.walk(raiz.resolve(BASE.replace('.', '/')))) {
            return archivos.filter(p -> p.toString().endsWith(".class"))
                    .map(raiz::relativize)
                    .map(Path::toString)
                    .map(p -> p.replace('\\', '.').replace('/', '.').replaceAll("\\.class$", ""))
                    .<Class<?>>map(
                            nombre -> {
                                try {
                                    return Class.forName(
                                            nombre, false, getClass().getClassLoader());
                                } catch (ClassNotFoundException e) {
                                    throw new IllegalStateException(e);
                                }
                            })
                    .toList();
        }
    }

    @Test
    void elDominioNoDependeDeCapasExternas() throws Exception {
        List<String> errores = new ArrayList<>();
        for (Class<?> c : clases())
            if (c.getName().contains(".dominio.")) {
                for (String d : dependencias(c)) {
                    if (d.startsWith(BASE)
                            && (d.contains(".aplicacion.")
                                    || d.contains(".persistencia.")
                                    || d.contains(".presentacion.")
                                    || d.contains(".configuracion.")))
                        errores.add(c.getName() + " -> " + d);
                    if (d.startsWith("org.springframework.web.")
                            || d.startsWith("jakarta.servlet."))
                        errores.add(c.getName() + " -> " + d);
                }
            }
        assertThat(errores)
                .as("El dominio solo conoce dominio, valores y bibliotecas base")
                .isEmpty();
    }

    @Test
    void laPresentacionNoAccedeAPersistenciaNiAEntidades() throws Exception {
        Set<String> entidades = new HashSet<>();
        for (Class<?> c : clases())
            if (c.isAnnotationPresent(Entity.class)) entidades.add(c.getName());
        List<String> errores = new ArrayList<>();
        for (Class<?> c : clases())
            if (c.getName().contains(".presentacion.")) {
                for (String d : dependencias(c))
                    if (d.contains(".persistencia.")
                            || entidades.contains(d)
                            || d.startsWith("org.springframework.jdbc.")
                            || d.equals("jakarta.persistence.EntityManager"))
                        errores.add(c.getName() + " -> " + d);
            }
        assertThat(errores)
                .as("Controladores y advice solo usan aplicación, comandos y records")
                .isEmpty();
    }

    @Test
    void aplicacionYPersistenciaNoDependenDePresentacion() throws Exception {
        List<String> errores = new ArrayList<>();
        for (Class<?> c : clases())
            if (c.getName().contains(".aplicacion.") || c.getName().contains(".persistencia.")) {
                for (String d : dependencias(c))
                    if (d.startsWith(BASE) && d.contains(".presentacion."))
                        errores.add(c.getName() + " -> " + d);
            }
        assertThat(errores).isEmpty();
    }

    @Test
    void elAccesoSqlSeConcentraEnPersistencia() throws Exception {
        List<String> errores = new ArrayList<>();
        for (Class<?> c : clases())
            if (!c.getName().contains(".persistencia.")) {
                for (String d : dependencias(c))
                    if (d.startsWith("org.springframework.jdbc.")
                            || d.equals("jakarta.persistence.EntityManager"))
                        errores.add(c.getName() + " -> " + d);
            }
        assertThat(errores).isEmpty();
    }

    @Test
    void lasEntidadesRelacionanOtrasEntidadesPorIdentificador() throws Exception {
        Set<String> asociaciones =
                Set.of(
                        "jakarta.persistence.ManyToOne",
                        "jakarta.persistence.OneToOne",
                        "jakarta.persistence.OneToMany",
                        "jakarta.persistence.ManyToMany");
        for (Class<?> c : clases())
            if (c.isAnnotationPresent(Entity.class)) {
                for (var campo : c.getDeclaredFields()) {
                    assertThat(campo.getType().isAnnotationPresent(Entity.class))
                            .as(c.getName() + "." + campo.getName())
                            .isFalse();
                    for (var a : campo.getAnnotations())
                        assertThat(asociaciones).doesNotContain(a.annotationType().getName());
                }
            }
    }

    @Test
    void losRecordsNoContienenEntidadesNiColeccionesDeEntidades() throws Exception {
        for (Class<?> c : clases())
            if (c.isRecord()) {
                for (var componente : c.getRecordComponents())
                    sinEntidad(
                            componente.getGenericType(), c.getName() + "." + componente.getName());
            }
    }

    private void sinEntidad(Type t, String origen) {
        if (t instanceof Class<?> c)
            assertThat(c.isAnnotationPresent(Entity.class)).as(origen).isFalse();
        if (t instanceof ParameterizedType p)
            for (Type argumento : p.getActualTypeArguments()) sinEntidad(argumento, origen);
    }

    private Set<String> dependencias(Class<?> clase) throws IOException {
        Set<String> refs = new HashSet<>();
        var patron = Pattern.compile("L([^;<]+)");
        java.util.function.Consumer<String> descriptor =
                texto -> {
                    if (texto == null) return;
                    var m = patron.matcher(texto);
                    while (m.find()) refs.add(m.group(1).replace('/', '.'));
                };
        java.util.function.Consumer<String> tipo =
                nombre -> {
                    if (nombre != null) refs.add(nombre.replace('/', '.'));
                };
        try (var in =
                clase.getResourceAsStream("/" + clase.getName().replace('.', '/') + ".class")) {
            new ClassReader(Objects.requireNonNull(in))
                    .accept(
                            new ClassVisitor(Opcodes.ASM9) {
                                @Override
                                public void visit(
                                        int v,
                                        int a,
                                        String n,
                                        String firma,
                                        String padre,
                                        String[] interfaces) {
                                    descriptor.accept(firma);
                                    tipo.accept(padre);
                                    if (interfaces != null)
                                        for (String i : interfaces) tipo.accept(i);
                                }

                                @Override
                                public FieldVisitor visitField(
                                        int a, String n, String d, String firma, Object valor) {
                                    descriptor.accept(d);
                                    descriptor.accept(firma);
                                    return null;
                                }

                                @Override
                                public MethodVisitor visitMethod(
                                        int a,
                                        String n,
                                        String d,
                                        String firma,
                                        String[] excepciones) {
                                    descriptor.accept(d);
                                    descriptor.accept(firma);
                                    return new MethodVisitor(Opcodes.ASM9) {
                                        @Override
                                        public void visitTypeInsn(int o, String t) {
                                            tipo.accept(t);
                                        }

                                        @Override
                                        public void visitFieldInsn(
                                                int o, String owner, String n, String desc) {
                                            tipo.accept(owner);
                                            descriptor.accept(desc);
                                        }

                                        @Override
                                        public void visitMethodInsn(
                                                int o,
                                                String owner,
                                                String n,
                                                String desc,
                                                boolean i) {
                                            tipo.accept(owner);
                                            descriptor.accept(desc);
                                            if (clase.getName().contains(".presentacion.")
                                                    && owner.replace('/', '.').startsWith(BASE)
                                                    && owner.contains("/aplicacion/")) {
                                                try {
                                                    Class<?> destino =
                                                            Class.forName(
                                                                    owner.replace('/', '.'),
                                                                    false,
                                                                    getClass().getClassLoader());
                                                    for (var metodo :
                                                            destino.getDeclaredMethods()) {
                                                        if (metodo.getName().equals(n)
                                                                && org.springframework.asm.Type
                                                                        .getMethodDescriptor(metodo)
                                                                        .equals(desc)) {
                                                            sinEntidad(
                                                                    metodo.getGenericReturnType(),
                                                                    clase.getName()
                                                                            + " -> "
                                                                            + destino.getName()
                                                                            + "."
                                                                            + n);
                                                        }
                                                    }
                                                } catch (ClassNotFoundException ex) {
                                                    throw new IllegalStateException(ex);
                                                }
                                            }
                                        }

                                        @Override
                                        public void visitLdcInsn(Object valor) {
                                            if (valor instanceof org.springframework.asm.Type t)
                                                descriptor.accept(t.getDescriptor());
                                        }

                                        @Override
                                        public void visitInvokeDynamicInsn(
                                                String n,
                                                String desc,
                                                Handle h,
                                                Object... argumentos) {
                                            descriptor.accept(desc);
                                            for (Object arg : argumentos) {
                                                if (arg instanceof Handle handle) {
                                                    tipo.accept(handle.getOwner());
                                                    descriptor.accept(handle.getDesc());
                                                }
                                                if (arg instanceof org.springframework.asm.Type t)
                                                    descriptor.accept(t.getDescriptor());
                                            }
                                        }
                                    };
                                }
                            },
                            ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return refs;
    }
}
