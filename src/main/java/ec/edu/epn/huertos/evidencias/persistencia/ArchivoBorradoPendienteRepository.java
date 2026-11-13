package ec.edu.epn.huertos.evidencias.persistencia;
import ec.edu.epn.huertos.evidencias.dominio.ArchivoBorradoPendiente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivoBorradoPendienteRepository extends JpaRepository<ArchivoBorradoPendiente, Long> {
    List<ArchivoBorradoPendiente> findTop100ByOrderByCreadaEnAsc();
}
