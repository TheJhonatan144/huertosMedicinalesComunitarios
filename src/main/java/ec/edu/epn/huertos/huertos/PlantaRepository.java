package ec.edu.epn.huertos.huertos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantaRepository extends JpaRepository<Planta, Long> {

    List<Planta> findByHuertoIdOrderByActivaDescNombreComunAsc(Long huertoId);

    List<Planta> findByHuertoIdAndActivaTrueOrderByNombreComunAsc(Long huertoId);
}
