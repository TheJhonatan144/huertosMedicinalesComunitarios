package ec.edu.epn.huertos.soporte.datos;

import ec.edu.epn.huertos.actividades.aplicacion.ActividadServicio;
import ec.edu.epn.huertos.actividades.aplicacion.FormularioActividad;
import ec.edu.epn.huertos.actividades.dominio.TipoActividad;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.autenticacion.dominio.Usuario;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import ec.edu.epn.huertos.compartido.dominio.Fechas;
import ec.edu.epn.huertos.comunidades.dominio.AsignacionRevisor;
import ec.edu.epn.huertos.comunidades.dominio.Comunidad;
import ec.edu.epn.huertos.comunidades.dominio.Integrante;
import ec.edu.epn.huertos.comunidades.persistencia.AsignacionRevisorRepository;
import ec.edu.epn.huertos.comunidades.persistencia.ComunidadRepository;
import ec.edu.epn.huertos.comunidades.persistencia.IntegranteRepository;
import ec.edu.epn.huertos.huertos.dominio.Huerto;
import ec.edu.epn.huertos.huertos.persistencia.HuertoRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public FabricaDatos(
            UsuarioRepository usuarios,
            ComunidadRepository comunidades,
            IntegranteRepository integrantes,
            AsignacionRevisorRepository asignaciones,
            HuertoRepository huertos,
            PasswordEncoder encoder,
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
    public record Escenario(
            Comunidad comunidad,
            Huerto huerto,
            Usuario gestorUsuario,
            Integrante gestor,
            List<Usuario> participanteUsuarios,
            List<Integrante> participantes,
            Usuario revisor) {}

    public Escenario escenario(int participantes) {
        int n = SECUENCIA.incrementAndGet();
        Comunidad comunidad =
                comunidades.save(
                        new Comunidad(
                                "Comunidad de prueba " + n,
                                "Datos sintéticos",
                                "Imbabura",
                                "Otavalo",
                                0.23,
                                -78.26));
        Huerto huerto =
                huertos.save(
                        new Huerto(
                                comunidad.getId(),
                                "Huerto de prueba " + n,
                                "Datos sintéticos",
                                0.23,
                                -78.26));
        Usuario gestorUsuario = usuario("gestor" + n, Rol.GESTOR_HUERTO);
        Integrante gestor =
                integrantes.save(new Integrante(comunidad.getId(), gestorUsuario.getId(), true));
        List<Usuario> us = new ArrayList<>();
        List<Integrante> is = new ArrayList<>();
        for (int i = 1; i <= participantes; i++) {
            Usuario u = usuario("part" + n + "_" + i, Rol.PARTICIPANTE);
            us.add(u);
            is.add(integrantes.save(new Integrante(comunidad.getId(), u.getId(), false)));
        }
        Usuario revisor = usuario("revisor" + n, Rol.ADMIN_MINISTERIO_SALUD);
        asignaciones.save(new AsignacionRevisor(revisor.getId(), comunidad.getId()));
        return new Escenario(comunidad, huerto, gestorUsuario, gestor, us, is, revisor);
    }

    /**
     * Registra una actividad de hoy a nombre del gestor del escenario, con los participantes
     * indicados.
     */
    public Long registrarActividad(Escenario e, List<Integrante> participantes) {
        FormularioActividad form = new FormularioActividad();
        form.setTipo(TipoActividad.RIEGO);
        form.setDescripcion("Riego manual de prueba");
        form.setFecha(Fechas.hoy());
        form.setParticipanteIds(participantes.stream().map(Integrante::getId).toList());
        return actividades.registrar(e.gestorUsuario().getId(), form, ImagenesPrueba.foto());
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
}
