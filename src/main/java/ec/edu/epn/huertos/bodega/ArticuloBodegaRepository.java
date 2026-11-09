package ec.edu.epn.huertos.bodega;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ArticuloBodegaRepository extends JpaRepository<ArticuloBodega, Long> {
 List<ArticuloBodega> findByHuertoIdOrderByCategoriaAscNombreAsc(Long huertoId);
}
