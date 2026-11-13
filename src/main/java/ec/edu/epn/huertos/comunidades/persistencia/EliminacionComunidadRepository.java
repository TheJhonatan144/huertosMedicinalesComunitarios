package ec.edu.epn.huertos.comunidades.persistencia;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import java.util.List;

/** Persistencia del proceso comunitario: mantiene el orden de borrado existente. */
@Repository
public class EliminacionComunidadRepository {
    private final EntityManager em;

    public EliminacionComunidadRepository(EntityManager em) {
        this.em = em;
    }

    public List<String> clavesEvidencias(Long id) {
        return em.createQuery(
                        "select e.claveAlmacenamiento from EnvioEvidencia e join Actividad a on"
                                + " a.id = e.actividadId where a.comunidadId = :id",
                        String.class)
                .setParameter("id", id)
                .getResultList();
    }

    public void borrarDatos(Long solicitudId, Long comunidadId) {
        em.createNativeQuery(
                        "select set_config('app.solicitud_comunidad_borrado', :solicitud, true)")
                .setParameter("solicitud", solicitudId.toString())
                .getSingleResult();
        em.createNativeQuery(
                        "delete from revision r using envio_evidencia e, actividad a where"
                                + " r.envio_id=e.id and e.actividad_id=a.id and a.comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from recompensa_individual where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from recompensa_comunitaria where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from actividad_participante where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery(
                        "delete from envio_evidencia e using actividad a where e.actividad_id=a.id"
                                + " and a.comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from invitacion_comunidad where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from asignacion_revisor where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from actividad where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery(
                        "delete from planta p using huerto h where p.huerto_id=h.id and"
                                + " h.comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from integrante where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from huerto where comunidad_id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
        em.createNativeQuery("delete from comunidad where id=:id")
                .setParameter("id", comunidadId)
                .executeUpdate();
    }
}
