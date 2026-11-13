package ec.edu.epn.huertos.huertos.persistencia;
import ec.edu.epn.huertos.huertos.dominio.Especie;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecieRepository extends JpaRepository<Especie, Long> {
    Optional<Especie> findFirstByNombreCientificoIgnoreCaseOrderById(String nombreCientifico);
}
