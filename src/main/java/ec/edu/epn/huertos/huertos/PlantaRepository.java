package ec.edu.epn.huertos.huertos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantaRepository extends JpaRepository<Planta, Long> {

    List<Planta> findByHuertoIdOrderByActivaDescNombreComunAsc(Long huertoId);

    List<Planta> findByHuertoIdAndActivaTrueOrderByNombreComunAsc(Long huertoId);

    List<Planta> findByHuertoIdAndPlantadaTrueOrderByNombreComunAsc(Long huertoId);

    boolean existsByHuertoIdAndEspecieId(Long huertoId, Long especieId);

    @org.springframework.data.jpa.repository.Query(value = "select id from huerto where id = :id for update", nativeQuery = true)
    Long bloquearHuerto(@org.springframework.data.repository.query.Param("id") Long id);
}
