package ec.edu.epn.huertos.huertos.persistencia;
import ec.edu.epn.huertos.huertos.dominio.EspacioHuerto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspacioHuertoRepository extends JpaRepository<EspacioHuerto, Long> {
    List<EspacioHuerto> findByHuertoIdAndActivaTrueOrderByNombreAsc(Long huertoId);
    List<EspacioHuerto> findByHuertoIdOrderByNombreAsc(Long huertoId);
    void deleteByHuertoIdAndDisenoNotIn(Long huertoId, List<String> disenos);
}
