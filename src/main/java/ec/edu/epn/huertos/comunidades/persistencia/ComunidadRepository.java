package ec.edu.epn.huertos.comunidades.persistencia;
import ec.edu.epn.huertos.comunidades.dominio.Comunidad;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Comunidad> findByActivaTrueAndEliminacionPendienteFalseOrderByNombreAsc();

    /** Bloquea la fila para serializar cambios de cupo de una misma comunidad. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comunidad c where c.id = :id")
    Optional<Comunidad> bloquearPorId(@Param("id") Long id);
}
