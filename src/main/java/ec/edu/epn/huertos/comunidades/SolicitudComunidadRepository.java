package ec.edu.epn.huertos.comunidades;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudComunidadRepository extends JpaRepository<SolicitudComunidad, Long> {
    List<SolicitudComunidad> findByEstadoOrderByCreadaEnAsc(EstadoSolicitudComunidad estado);
    List<SolicitudComunidad> findBySolicitanteIdOrderByCreadaEnDesc(Long solicitanteId);
    boolean existsBySolicitanteIdAndTipoAndEstado(Long solicitanteId, TipoSolicitudComunidad tipo,
                                                  EstadoSolicitudComunidad estado);
    boolean existsByComunidadIdAndTipoAndEstado(Long comunidadId, TipoSolicitudComunidad tipo,
                                                 EstadoSolicitudComunidad estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitudComunidad s where s.id = :id")
    Optional<SolicitudComunidad> bloquearPorId(@Param("id") Long id);
}
