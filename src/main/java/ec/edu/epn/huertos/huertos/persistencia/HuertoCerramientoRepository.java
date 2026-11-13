package ec.edu.epn.huertos.huertos.persistencia;
import ec.edu.epn.huertos.huertos.dominio.HuertoCerramiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HuertoCerramientoRepository extends JpaRepository<HuertoCerramiento, Long> {
    List<HuertoCerramiento> findByHuertoIdOrderByGrupoAsc(Long huertoId);
    void deleteByHuertoId(Long huertoId);
}
