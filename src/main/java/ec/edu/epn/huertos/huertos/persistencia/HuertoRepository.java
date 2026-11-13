package ec.edu.epn.huertos.huertos.persistencia;
import ec.edu.epn.huertos.huertos.dominio.Huerto;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HuertoRepository extends JpaRepository<Huerto, Long> {

    Optional<Huerto> findByComunidadId(Long comunidadId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Huerto h where h.id = :id")
    Optional<Huerto> bloquearPorId(@Param("id") Long id);
}
