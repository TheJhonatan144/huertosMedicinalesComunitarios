package ec.edu.epn.huertos.huertos;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EspecieRepository extends JpaRepository<Especie, Long> {
    Optional<Especie> findFirstByNombreCientificoIgnoreCaseOrderById(String nombreCientifico);
}
