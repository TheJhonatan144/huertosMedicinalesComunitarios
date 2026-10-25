package ec.edu.epn.huertos.huertos;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HuertoRepository extends JpaRepository<Huerto, Long> {

    Optional<Huerto> findByComunidadId(Long comunidadId);
}
