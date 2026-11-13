package ec.edu.epn.huertos.comunidades.aplicacion;

import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.autenticacion.dominio.Usuario;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import ec.edu.epn.huertos.comunidades.dominio.Integrante;
import ec.edu.epn.huertos.comunidades.persistencia.EliminacionComunidadRepository;
import ec.edu.epn.huertos.comunidades.persistencia.IntegranteRepository;
import ec.edu.epn.huertos.evidencias.aplicacion.LimpiezaArchivosBorrados;
import ec.edu.epn.huertos.evidencias.dominio.ArchivoBorradoPendiente;
import ec.edu.epn.huertos.evidencias.persistencia.ArchivoBorradoPendienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/** Borra los datos operativos de una comunidad solo dentro de la aprobación ministerial. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class EliminacionComunidadServicio {
    private final EliminacionComunidadRepository datos;
    private final IntegranteRepository integrantes;
    private final UsuarioRepository usuarios;
    private final ArchivoBorradoPendienteRepository archivos;
    private final LimpiezaArchivosBorrados limpieza;

    public EliminacionComunidadServicio(
            EliminacionComunidadRepository datos,
            IntegranteRepository integrantes,
            UsuarioRepository usuarios,
            ArchivoBorradoPendienteRepository archivos,
            LimpiezaArchivosBorrados limpieza) {
        this.datos = datos;
        this.integrantes = integrantes;
        this.usuarios = usuarios;
        this.archivos = archivos;
        this.limpieza = limpieza;
    }

    @Transactional
    public void eliminarAprobada(Long solicitudId, Long comunidadId) {
        List<String> claves = datos.clavesEvidencias(comunidadId);
        claves.forEach(clave -> archivos.save(new ArchivoBorradoPendiente(solicitudId, clave)));

        for (Integrante integrante : integrantes.findByComunidadIdAndActivoTrue(comunidadId)) {
            Usuario cuenta = usuarios.findById(integrante.getUsuarioId()).orElse(null);
            if (cuenta != null && cuenta.getRol() != Rol.ADMIN_MINISTERIO_SALUD)
                cuenta.cambiarRol(Rol.SIN_ASIGNAR);
        }

        datos.borrarDatos(solicitudId, comunidadId);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        limpieza.procesarPendientes();
                    }
                });
    }
}
