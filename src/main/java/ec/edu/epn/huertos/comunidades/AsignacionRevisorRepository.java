package ec.edu.epn.huertos.comunidades;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignacionRevisorRepository extends JpaRepository<AsignacionRevisor, Long> {

    boolean existsByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    List<AsignacionRevisor> findByUsuarioId(Long usuarioId);
}
