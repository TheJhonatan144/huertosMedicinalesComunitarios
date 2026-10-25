package ec.edu.epn.huertos.actividades;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActividadParticipanteRepository extends JpaRepository<ActividadParticipante, Long> {

    List<ActividadParticipante> findByActividadId(Long actividadId);

    long countByActividadId(Long actividadId);

    @Query("""
            select new ec.edu.epn.huertos.actividades.ParticipanteVista(i.id, u.nombreCompleto)
            from ActividadParticipante p
              join ec.edu.epn.huertos.comunidades.Integrante i on i.id = p.integranteId
              join ec.edu.epn.huertos.autenticacion.Usuario u on u.id = i.usuarioId
            where p.actividadId = :actividadId
            order by u.nombreCompleto
            """)
    List<ParticipanteVista> vistaPorActividad(@Param("actividadId") Long actividadId);

    @Query("select p.actividadId from ActividadParticipante p where p.integranteId = :integranteId")
    List<Long> actividadesDeIntegrante(@Param("integranteId") Long integranteId);
}
