package ec.edu.epn.huertos.comunidades;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {

    /** Bloquea la fila para serializar cambios de cupo de una misma comunidad. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comunidad c where c.id = :id")
    Optional<Comunidad> bloquearPorId(@Param("id") Long id);
}
