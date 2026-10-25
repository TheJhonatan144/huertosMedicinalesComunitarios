package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.AutorizacionServicio;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComunidadServicio {

    private final AutorizacionServicio autorizacion;
    private final ComunidadRepository comunidades;
    private final IntegranteRepository integrantes;
    private final AsignacionRevisorRepository asignaciones;
    private final int cupoMaximo;

    public ComunidadServicio(AutorizacionServicio autorizacion, ComunidadRepository comunidades,
                             IntegranteRepository integrantes, AsignacionRevisorRepository asignaciones,
                             @Value("${app.comunidades.cupo-maximo}") int cupoMaximo) {
        this.autorizacion = autorizacion;
        this.comunidades = comunidades;
        this.integrantes = integrantes;
        this.asignaciones = asignaciones;
        this.cupoMaximo = cupoMaximo;
    }

    /** Integrante gestor (activo) del usuario indicado; cualquier otro caso es acceso denegado. */
    @Transactional(readOnly = true)
    public Integrante gestorDe(Long usuarioId) {
        autorizacion.exigirRol(usuarioId, Rol.GESTOR_HUERTO);
        return integrantes.findByUsuarioId(usuarioId)
                .filter(i -> i.isEsGestor() && i.isActivo())
                .orElseThrow(() -> new AccessDeniedException("El usuario no gestiona ninguna comunidad"));
    }

    @Transactional(readOnly = true)
    public Integrante participanteDe(Long usuarioId) {
        autorizacion.exigirRol(usuarioId, Rol.PARTICIPANTE);
        return integrantes.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new AccessDeniedException("El usuario no pertenece a ninguna comunidad"));
    }

    @Transactional(readOnly = true)
    public Comunidad comunidad(Long id) {
        return comunidades.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
    }

    @Transactional(readOnly = true)
    public List<IntegranteVista> integrantesDeMiComunidad(Long gestorUsuarioId) {
        return integrantes.vistaPorComunidad(gestorDe(gestorUsuarioId).getComunidadId());
    }

    @Transactional(readOnly = true)
    public long activos(Long comunidadId) {
        return integrantes.countByComunidadIdAndActivoTrue(comunidadId);
    }

    public int cupoMaximo() {
        return cupoMaximo;
    }

    @Transactional
    public void darDeBaja(Long gestorUsuarioId, Long integranteId) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        Integrante objetivo = integrantePropio(gestor, integranteId);
        if (objetivo.isEsGestor()) {
            throw new ReglaNegocioException("El gestor de la comunidad no puede darse de baja.");
        }
        objetivo.darDeBaja();
    }

    /** Reactivar consume cupo: se serializa por comunidad y se revalida el límite. */
    @Transactional
    public void reactivar(Long gestorUsuarioId, Long integranteId) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        comunidades.bloquearPorId(gestor.getComunidadId());
        Integrante objetivo = integrantePropio(gestor, integranteId);
        if (objetivo.isActivo()) {
            return;
        }
        if (integrantes.countByComunidadIdAndActivoTrue(gestor.getComunidadId()) >= cupoMaximo) {
            throw new ConflictoException("La comunidad ya tiene " + cupoMaximo
                    + " integrantes activos. Da de baja a alguien para liberar un cupo.");
        }
        objetivo.reactivar();
        integrantes.flush();
    }

    @Transactional(readOnly = true)
    public boolean revisorAsignado(Long revisorUsuarioId, Long comunidadId) {
        return asignaciones.existsByUsuarioIdAndComunidadId(revisorUsuarioId, comunidadId);
    }

    @Transactional(readOnly = true)
    public List<Long> comunidadesAsignadas(Long revisorUsuarioId) {
        return asignaciones.findByUsuarioId(revisorUsuarioId).stream().map(AsignacionRevisor::getComunidadId).toList();
    }

    private Integrante integrantePropio(Integrante gestor, Long integranteId) {
        // Un integrante de otra comunidad se trata como acceso denegado, no como "no encontrado".
        return integrantes.findById(integranteId)
                .filter(i -> i.getComunidadId().equals(gestor.getComunidadId()))
                .orElseThrow(() -> new AccessDeniedException("El integrante no pertenece a tu comunidad"));
    }
}
