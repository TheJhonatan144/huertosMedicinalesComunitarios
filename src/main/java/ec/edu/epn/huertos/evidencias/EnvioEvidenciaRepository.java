package ec.edu.epn.huertos.evidencias;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnvioEvidenciaRepository extends JpaRepository<EnvioEvidencia, Long> {

    List<EnvioEvidencia> findByActividadIdOrderByNumeroAsc(Long actividadId);

    long countByActividadId(Long actividadId);

    /** Bloqueo de fila: serializa revisiones concurrentes del mismo envío. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EnvioEvidencia e where e.id = :id")
    Optional<EnvioEvidencia> bloquearPorId(@Param("id") Long id);

    @Query("select coalesce(max(e.numero), 0) from EnvioEvidencia e where e.actividadId = :actividadId")
    int ultimoNumero(@Param("actividadId") Long actividadId);

    @Query("""
            select e from EnvioEvidencia e join ec.edu.epn.huertos.actividades.Actividad a on a.id = e.actividadId
            where e.estado = ec.edu.epn.huertos.evidencias.EstadoRevision.PENDIENTE and a.comunidadId in :comunidades
            order by e.enviadoEn asc
            """)
    List<EnvioEvidencia> pendientesDeComunidades(@Param("comunidades") Collection<Long> comunidades);
}
