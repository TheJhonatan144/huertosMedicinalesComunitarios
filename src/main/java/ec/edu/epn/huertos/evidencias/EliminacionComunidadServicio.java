package ec.edu.epn.huertos.evidencias;

import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.comunidades.Integrante;
import ec.edu.epn.huertos.comunidades.IntegranteRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Borra los datos operativos de una comunidad solo dentro de la aprobación ministerial. */
@Service
public class EliminacionComunidadServicio {
    private final EntityManager em;
    private final IntegranteRepository integrantes;
    private final UsuarioRepository usuarios;
    private final ArchivoBorradoPendienteRepository archivos;
    private final LimpiezaArchivosBorrados limpieza;

    public EliminacionComunidadServicio(EntityManager em, IntegranteRepository integrantes,
            UsuarioRepository usuarios, ArchivoBorradoPendienteRepository archivos, LimpiezaArchivosBorrados limpieza) {
        this.em = em; this.integrantes = integrantes; this.usuarios = usuarios; this.archivos = archivos; this.limpieza = limpieza;
    }

    @Transactional
    public void eliminarAprobada(Long solicitudId, Long comunidadId) {
        List<String> claves = em.createQuery("select e.claveAlmacenamiento from EnvioEvidencia e join Actividad a on a.id = e.actividadId where a.comunidadId = :comunidadId", String.class)
                .setParameter("comunidadId", comunidadId).getResultList();
        claves.forEach(clave -> archivos.save(new ArchivoBorradoPendiente(solicitudId, clave)));

        for (Integrante integrante : integrantes.findByComunidadIdAndActivoTrue(comunidadId)) {
            Usuario cuenta = usuarios.findById(integrante.getUsuarioId()).orElse(null);
            if (cuenta != null && cuenta.getRol() != Rol.ADMIN_MINISTERIO_SALUD) cuenta.cambiarRol(Rol.SIN_ASIGNAR);
        }

        em.createNativeQuery("select set_config('app.solicitud_comunidad_borrado', :solicitud, true)")
                .setParameter("solicitud", solicitudId.toString()).getSingleResult();
        borrar("delete from revision r using envio_evidencia e, actividad a where r.envio_id=e.id and e.actividad_id=a.id and a.comunidad_id=:id", comunidadId);
        borrar("delete from recompensa_individual where comunidad_id=:id", comunidadId);
        borrar("delete from recompensa_comunitaria where comunidad_id=:id", comunidadId);
        borrar("delete from actividad_participante where comunidad_id=:id", comunidadId);
        borrar("delete from envio_evidencia e using actividad a where e.actividad_id=a.id and a.comunidad_id=:id", comunidadId);
        borrar("delete from invitacion_comunidad where comunidad_id=:id", comunidadId);
        borrar("delete from asignacion_revisor where comunidad_id=:id", comunidadId);
        borrar("delete from actividad where comunidad_id=:id", comunidadId);
        borrar("delete from planta p using huerto h where p.huerto_id=h.id and h.comunidad_id=:id", comunidadId);
        borrar("delete from integrante where comunidad_id=:id", comunidadId);
        borrar("delete from huerto where comunidad_id=:id", comunidadId);
        borrar("delete from comunidad where id=:id", comunidadId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { limpieza.procesarPendientes(); }
        });
    }

    private void borrar(String sql, Long id) {
        em.createNativeQuery(sql).setParameter("id", id).executeUpdate();
    }
}
