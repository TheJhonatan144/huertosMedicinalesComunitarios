package ec.edu.epn.huertos.huertos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HuertoCerramientoRepository extends JpaRepository<HuertoCerramiento, Long> {
    List<HuertoCerramiento> findByHuertoIdOrderByGrupoAsc(Long huertoId);
    void deleteByHuertoId(Long huertoId);
}
