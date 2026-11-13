package ec.edu.epn.huertos.bodega.persistencia;
import ec.edu.epn.huertos.bodega.dominio.ArticuloBodega;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticuloBodegaRepository extends JpaRepository<ArticuloBodega, Long> {
 List<ArticuloBodega> findByHuertoIdOrderByCategoriaAscNombreAsc(Long huertoId);
}
