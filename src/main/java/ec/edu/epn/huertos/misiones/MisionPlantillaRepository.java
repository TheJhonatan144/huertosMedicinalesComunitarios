package ec.edu.epn.huertos.misiones;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MisionPlantillaRepository extends JpaRepository<MisionPlantilla, Long> {
    List<MisionPlantilla> findByActivaTrueOrderByOrdenAsc();
}
