package ec.edu.epn.huertos.evidencias;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivoBorradoPendienteRepository extends JpaRepository<ArchivoBorradoPendiente, Long> {
    List<ArchivoBorradoPendiente> findTop100ByOrderByCreadaEnAsc();
}
