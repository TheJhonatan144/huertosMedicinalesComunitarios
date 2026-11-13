package ec.edu.epn.huertos.misiones.persistencia;
import ec.edu.epn.huertos.misiones.dominio.MisionPlantilla;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MisionPlantillaRepository extends JpaRepository<MisionPlantilla, Long> {
    List<MisionPlantilla> findByActivaTrueOrderByOrdenAsc();
}
