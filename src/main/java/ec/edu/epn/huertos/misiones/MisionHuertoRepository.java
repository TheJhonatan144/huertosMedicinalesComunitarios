package ec.edu.epn.huertos.misiones;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MisionHuertoRepository extends JpaRepository<MisionHuerto, Long> {
    boolean existsByHuertoId(Long huertoId);
    List<MisionHuerto> findByHuertoIdOrderByIdAsc(Long huertoId);
    Optional<MisionHuerto> findByActividadId(Long actividadId);
    Optional<MisionHuerto> findByIdAndHuertoId(Long id, Long huertoId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MisionHuerto m where m.id = :id and m.huertoId = :huertoId")
    Optional<MisionHuerto> bloquearPorIdYHuerto(@Param("id") Long id, @Param("huertoId") Long huertoId);
}
