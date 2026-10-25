package ec.edu.epn.huertos;

import ec.edu.epn.huertos.actividades.ActividadServicio;
import ec.edu.epn.huertos.actividades.FormularioActividad;
import ec.edu.epn.huertos.actividades.TipoActividad;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.comunidades.AsignacionRevisor;
import ec.edu.epn.huertos.comunidades.AsignacionRevisorRepository;
import ec.edu.epn.huertos.comunidades.Comunidad;
import ec.edu.epn.huertos.comunidades.ComunidadRepository;
import ec.edu.epn.huertos.comunidades.Integrante;
import ec.edu.epn.huertos.comunidades.IntegranteRepository;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.huertos.Huerto;
import ec.edu.epn.huertos.huertos.HuertoRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea comunidades, usuarios y fotografías sintéticas para las pruebas, con nombres únicos. */
@Component
public class FabricaDatos {

    public static final String CLAVE = "Prueba-2026!";
    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    private final UsuarioRepository usuarios;
    private final ComunidadRepository comunidades;
    private final IntegranteRepository integrantes;
    private final AsignacionRevisorRepository asignaciones;
    private final HuertoRepository huertos;
    private final PasswordEncoder encoder;
    private final ActividadServicio actividades;

    public FabricaDatos(UsuarioRepository usuarios, ComunidadRepository comunidades, IntegranteRepository integrantes,
                        AsignacionRevisorRepository asignaciones, HuertoRepository huertos, PasswordEncoder encoder,
                        ActividadServicio actividades) {
        this.usuarios = usuarios;
        this.comunidades = comunidades;
        this.integrantes = integrantes;
        this.asignaciones = asignaciones;
        this.huertos = huertos;
        this.encoder = encoder;
        this.actividades = actividades;
    }

    /** Comunidad con huerto, gestor, participantes activos y un revisor asignado. */
    public record Escenario(Comunidad comunidad, Huerto huerto, Usuario gestorUsuario, Integrante gestor,
                            List<Usuario> participanteUsuarios, List<Integrante> participantes, Usuario revisor) {
    }

    public Escenario escenario(int participantes) {
        int n = SECUENCIA.incrementAndGet();
        Comunidad comunidad = comunidades.save(new Comunidad("Comunidad de prueba " + n, "Datos sintéticos", "Imbabura",
                "Otavalo", 0.23, -78.26));
        Huerto huerto = huertos.save(new Huerto(comunidad.getId(), "Huerto de prueba " + n, "Datos sintéticos", 0.23, -78.26));
        Usuario gestorUsuario = usuario("gestor" + n, Rol.GESTOR_HUERTO);
        Integrante gestor = integrantes.save(new Integrante(comunidad.getId(), gestorUsuario.getId(), true));
        List<Usuario> us = new ArrayList<>();
        List<Integrante> is = new ArrayList<>();
        for (int i = 1; i <= participantes; i++) {
            Usuario u = usuario("part" + n + "_" + i, Rol.PARTICIPANTE);
            us.add(u);
            is.add(integrantes.save(new Integrante(comunidad.getId(), u.getId(), false)));
        }
        Usuario revisor = usuario("revisor" + n, Rol.ADMIN_EVIDENCIAS);
        asignaciones.save(new AsignacionRevisor(revisor.getId(), comunidad.getId()));
        return new Escenario(comunidad, huerto, gestorUsuario, gestor, us, is, revisor);
    }

    /** Registra una actividad de hoy a nombre del gestor del escenario, con los participantes indicados. */
    public Long registrarActividad(Escenario e, List<Integrante> participantes) {
        FormularioActividad form = new FormularioActividad();
        form.setTipo(TipoActividad.RIEGO);
        form.setDescripcion("Riego manual de prueba");
        form.setFecha(Fechas.hoy());
        form.setParticipanteIds(participantes.stream().map(Integrante::getId).toList());
        return actividades.registrar(e.gestorUsuario().getId(), form, foto());
    }

    public Usuario usuario(String nombre, Rol rol) {
        return usuarios.save(new Usuario(nombre, "Usuario " + nombre, encoder.encode(CLAVE), rol));
    }

    public Integrante participanteNuevo(Comunidad comunidad, boolean activo) {
        int n = SECUENCIA.incrementAndGet();
        Usuario u = usuario("extra" + n, Rol.PARTICIPANTE);
        Integrante integrante = new Integrante(comunidad.getId(), u.getId(), false);
        if (!activo) {
            integrante.darDeBaja();
        }
        return integrantes.save(integrante);
    }

    public static byte[] pngBytes(int ancho, int alto) {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                imagen.setRGB(x, y, (x * 7 + y * 13 + SECUENCIA.get()) & 0xFFFFFF);
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
