package ec.edu.epn.huertos.gamificacion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecompensaIndividualRepository extends JpaRepository<RecompensaIndividual, Long> {

    @Query("select coalesce(sum(r.puntos), 0L) from RecompensaIndividual r where r.integranteId = :integranteId")
    long totalPuntos(@Param("integranteId") Long integranteId);

    List<RecompensaIndividual> findByActividadId(Long actividadId);

    List<RecompensaIndividual> findByIntegranteIdOrderByIdDesc(Long integranteId);
}
