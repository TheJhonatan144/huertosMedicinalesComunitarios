package ec.edu.epn.huertos.gamificacion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecompensaComunitariaRepository extends JpaRepository<RecompensaComunitaria, Long> {

    boolean existsByActividadId(Long actividadId);

    Optional<RecompensaComunitaria> findByActividadId(Long actividadId);

    @Query("select coalesce(sum(r.puntos), 0L) from RecompensaComunitaria r where r.comunidadId = :comunidadId")
    long totalPuntos(@Param("comunidadId") Long comunidadId);

    @Query("select coalesce(sum(r.progreso), 0L) from RecompensaComunitaria r where r.comunidadId = :comunidadId")
    long totalProgreso(@Param("comunidadId") Long comunidadId);

    /** Ranking comunitario: solo puntos comunitarios, incluyendo comunidades con cero puntos. */
    @Query("""
            select new ec.edu.epn.huertos.gamificacion.PuntosComunidad(c.id, c.nombre, c.provincia, coalesce(sum(r.puntos), 0L))
            from ec.edu.epn.huertos.comunidades.Comunidad c
              left join RecompensaComunitaria r on r.comunidadId = c.id
            group by c.id, c.nombre, c.provincia
            order by coalesce(sum(r.puntos), 0L) desc, c.nombre asc
            """)
    List<PuntosComunidad> puntosPorComunidad();
}
