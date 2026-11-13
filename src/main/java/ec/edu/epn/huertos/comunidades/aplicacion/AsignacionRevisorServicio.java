package ec.edu.epn.huertos.comunidades.aplicacion;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.comunidades.dominio.AsignacionRevisor;
import ec.edu.epn.huertos.comunidades.persistencia.AsignacionBloqueo;
import ec.edu.epn.huertos.comunidades.persistencia.AsignacionRevisorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Distribuye nuevas comunidades entre administradores MSP activos con menor carga. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AsignacionRevisorServicio {
    private static final long BLOQUEO_ASIGNACION = 74192831L;
    private final AsignacionBloqueo bloqueo;
    private final UsuarioRepository usuarios;
    private final AsignacionRevisorRepository asignaciones;

    public AsignacionRevisorServicio(AsignacionBloqueo bloqueo, UsuarioRepository usuarios,
                                    AsignacionRevisorRepository asignaciones) {
        this.bloqueo = bloqueo;
        this.usuarios = usuarios;
        this.asignaciones = asignaciones;
    }

    @Transactional
    public Long asignarAutomaticamente(Long comunidadId) {
        bloqueo.adquirir(BLOQUEO_ASIGNACION);
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
