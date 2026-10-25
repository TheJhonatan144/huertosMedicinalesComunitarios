package ec.edu.epn.huertos.evidencias;

import ec.edu.epn.huertos.actividades.Actividad;
import ec.edu.epn.huertos.actividades.ActividadParticipanteRepository;
import ec.edu.epn.huertos.actividades.ActividadRepository;
import ec.edu.epn.huertos.actividades.DetalleActividad;
import ec.edu.epn.huertos.actividades.DetalleActividadServicio;
import ec.edu.epn.huertos.autenticacion.AutorizacionServicio;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.comunidades.Comunidad;
import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del administrador de evidencias. La decisión, su historial y la recompensa se resuelven
 * en una sola transacción; el envío se bloquea con SELECT ... FOR UPDATE para serializar revisiones concurrentes.
 */
@Service
public class RevisionServicio {

    private final AutorizacionServicio autorizacion;
    private final ComunidadServicio comunidades;
    private final ActividadRepository actividades;
    private final ActividadParticipanteRepository participantes;
    private final EnvioEvidenciaRepository envios;
    private final RevisionRepository revisiones;
    private final UsuarioRepository usuarios;
    private final GamificacionServicio gamificacion;
    private final DetalleActividadServicio detalles;

    public RevisionServicio(AutorizacionServicio autorizacion, ComunidadServicio comunidades,
                            ActividadRepository actividades, ActividadParticipanteRepository participantes,
                            EnvioEvidenciaRepository envios, RevisionRepository revisiones, UsuarioRepository usuarios,
                            GamificacionServicio gamificacion, DetalleActividadServicio detalles) {
        this.autorizacion = autorizacion;
        this.comunidades = comunidades;
        this.actividades = actividades;
        this.participantes = participantes;
        this.envios = envios;
        this.revisiones = revisiones;
        this.usuarios = usuarios;
        this.gamificacion = gamificacion;
        this.detalles = detalles;
    }

    public record ItemBandeja(Long envioId, Long actividadId, String comunidad, String tipo, String registradaPor,
                              int numeroEnvio, String enviadoEn, int participantes) {
    }

    public record DetalleRevision(Long envioId, EstadoRevision estadoEnvio, int numeroEnvio, DetalleActividad actividad) {
    }

    public record ItemHistorial(Long actividadId, Long envioId, String comunidad, String tipo, int numeroEnvio,
                                EstadoRevision decision, String observaciones, String revisadaEn) {
    }

    public record Resultado(EstadoRevision decision, boolean recompensaGenerada) {
    }

    @Transactional(readOnly = true)
    public List<ItemBandeja> bandeja(Long revisorId) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_EVIDENCIAS);
        List<Long> asignadas = comunidades.comunidadesAsignadas(revisorId);
        if (asignadas.isEmpty()) {
            return List.of();
        }
        List<EnvioEvidencia> pendientes = envios.pendientesDeComunidades(asignadas);
        Map<Long, Actividad> porId = actividades.findAllById(pendientes.stream().map(EnvioEvidencia::getActividadId).toList())
                .stream().collect(Collectors.toMap(Actividad::getId, Function.identity()));
        return pendientes.stream()
                .filter(e -> !porId.get(e.getActividadId()).getRegistradaPor().equals(revisorId))
                .map(e -> {
                    Actividad a = porId.get(e.getActividadId());
                    String registrante = usuarios.findById(a.getRegistradaPor()).map(Usuario::getNombreCompleto).orElse("");
                    return new ItemBandeja(e.getId(), a.getId(), comunidades.comunidad(a.getComunidadId()).getNombre(),
                            a.getTipo().etiqueta(), registrante, e.getNumero(), Fechas.hora(e.getEnviadoEn()),
                            (int) participantes.countByActividadId(a.getId()));
                }).toList();
    }

    @Transactional(readOnly = true)
    public DetalleRevision detalle(Long revisorId, Long envioId) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_EVIDENCIAS);
        EnvioEvidencia envio = envios.findById(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El envío no existe."));
        Actividad actividad = actividadRevisable(revisorId, envio);
        return new DetalleRevision(envio.getId(), envio.getEstado(), envio.getNumero(), detalles.construir(actividad));
    }

    @Transactional(readOnly = true)
    public List<ItemHistorial> historial(Long revisorId) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_EVIDENCIAS);
        return revisiones.findByRevisorIdOrderByRevisadaEnDesc(revisorId).stream().map(r -> {
            EnvioEvidencia envio = envios.findById(r.getEnvioId()).orElseThrow();
            Actividad a = actividades.findById(envio.getActividadId()).orElseThrow();
            Comunidad c = comunidades.comunidad(a.getComunidadId());
            return new ItemHistorial(a.getId(), envio.getId(), c.getNombre(), a.getTipo().etiqueta(), envio.getNumero(),
                    r.getDecision(), r.getObservaciones(), Fechas.hora(r.getRevisadaEn()));
        }).toList();
    }

    /**
     * Aprueba o rechaza un envío. Un segundo intento sobre el mismo envío (doble clic, reintento o
     * revisor concurrente) recibe {@link ConflictoException} y no produce ningún efecto.
     */
    @Transactional
    public Resultado decidir(Long revisorId, Long envioId, EstadoRevision decision, String observaciones) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_EVIDENCIAS);
        if (decision != EstadoRevision.APROBADA && decision != EstadoRevision.RECHAZADA) {
            throw new ReglaNegocioException("La decisión debe ser aprobar o rechazar.");
        }
        EnvioEvidencia envio = envios.bloquearPorId(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El envío no existe."));
        Actividad actividad = actividadRevisable(revisorId, envio);
        if (envio.getEstado() != EstadoRevision.PENDIENTE) {
            throw new ConflictoException("Este envío ya fue revisado; no se puede decidir de nuevo.");
        }
        if (decision == EstadoRevision.RECHAZADA && (observaciones == null || observaciones.isBlank())) {
            throw new ReglaNegocioException("Para rechazar una evidencia debes escribir observaciones.");
        }

        revisiones.save(new Revision(envio.getId(), revisorId, decision, observaciones));
        envio.aplicarDecision(decision);
        actividad.aplicarDecision(decision);
        // Los triggers de recompensas consultan el estado persistido de la actividad: se vuelca antes de otorgar.
        actividades.flush();
        boolean recompensa = decision == EstadoRevision.APROBADA && gamificacion.otorgar(actividad);
        revisiones.flush();
        return new Resultado(decision, recompensa);
    }

    /** Exige comunidad asignada y prohíbe la autorrevisión. */
    private Actividad actividadRevisable(Long revisorId, EnvioEvidencia envio) {
        Actividad actividad = actividades.findById(envio.getActividadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La actividad no existe."));
        if (!comunidades.revisorAsignado(revisorId, actividad.getComunidadId())) {
            throw new AccessDeniedException("La comunidad no está asignada a este revisor");
        }
        if (actividad.getRegistradaPor().equals(revisorId)) {
            throw new AccessDeniedException("Un revisor no puede revisar actividades registradas por él mismo");
        }
        return actividad;
    }
}
