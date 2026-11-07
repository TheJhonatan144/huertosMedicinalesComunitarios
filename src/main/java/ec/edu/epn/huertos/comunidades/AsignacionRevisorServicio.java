package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.compartido.ConflictoException;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Distribuye nuevas comunidades entre administradores MSP activos con menor carga. */
@Service
public class AsignacionRevisorServicio {
    private static final long BLOQUEO_ASIGNACION = 74192831L;
    private final EntityManager entityManager;
    private final UsuarioRepository usuarios;
    private final AsignacionRevisorRepository asignaciones;

    public AsignacionRevisorServicio(EntityManager entityManager, UsuarioRepository usuarios,
                                    AsignacionRevisorRepository asignaciones) {
        this.entityManager = entityManager;
        this.usuarios = usuarios;
        this.asignaciones = asignaciones;
    }

    @Transactional
    public Long asignarAutomaticamente(Long comunidadId) {
        entityManager.createNativeQuery("select pg_advisory_xact_lock(:clave)")
                .setParameter("clave", BLOQUEO_ASIGNACION).getSingleResult();
        List<AsignacionRevisor> actual = asignaciones.findByComunidadId(comunidadId);
        if (!actual.isEmpty()) return actual.getFirst().getUsuarioId();
        List<Long> candidatos = usuarios.administradoresActivosPorCarga();
        if (candidatos.isEmpty())
            throw new ConflictoException("No hay administradores MSP activos disponibles para asignar esta comunidad.");
        Long responsable = candidatos.getFirst();
        asignaciones.saveAndFlush(new AsignacionRevisor(responsable, comunidadId));
        return responsable;
    }

    @Transactional(readOnly = true)
    public List<ComunidadAsignadaVista> comunidadesAsignadas(Long usuarioId) {
        return asignaciones.comunidadesVista(usuarioId);
    }
}
