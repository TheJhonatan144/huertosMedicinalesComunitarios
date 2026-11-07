package ec.edu.epn.huertos.huertos;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HuertoRepository extends JpaRepository<Huerto, Long> {

    Optional<Huerto> findByComunidadId(Long comunidadId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Huerto h where h.id = :id")
    Optional<Huerto> bloquearPorId(@Param("id") Long id);
}
