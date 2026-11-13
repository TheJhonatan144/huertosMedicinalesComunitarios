package ec.edu.epn.huertos.comunidades.persistencia;
import ec.edu.epn.huertos.comunidades.dominio.EstadoInvitacion;
import ec.edu.epn.huertos.comunidades.dominio.InvitacionComunidad;
import ec.edu.epn.huertos.comunidades.dominio.TipoInvitacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitacionRepository extends JpaRepository<InvitacionComunidad, Long> {
    boolean existsByComunidadIdAndUsuarioDestinoIdAndTipoAndEstado(Long comunidadId, Long usuarioDestinoId,
                                                                   TipoInvitacion tipo, EstadoInvitacion estado);
    List<InvitacionComunidad> findByUsuarioDestinoIdAndEstadoOrderByCreadaEnDesc(Long usuarioDestinoId, EstadoInvitacion estado);
    List<InvitacionComunidad> findByInvitadaPorAndEstadoOrderByCreadaEnDesc(Long invitadaPor, EstadoInvitacion estado);
    List<InvitacionComunidad> findByComunidadIdAndEstado(Long comunidadId, EstadoInvitacion estado);
}
