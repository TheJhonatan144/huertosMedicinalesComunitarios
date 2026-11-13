package ec.edu.epn.huertos.comunidades.persistencia;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

/** Bloqueo de BD de la política de asignación automática. */
@Repository
public class AsignacionBloqueo {
    private final EntityManager em;

    public AsignacionBloqueo(EntityManager em) {
        this.em = em;
    }

    public void adquirir(long clave) {
        em.createNativeQuery("select pg_advisory_xact_lock(:clave)")
                .setParameter("clave", clave)
                .getSingleResult();
    }
}
