package ec.edu.epn.huertos.evidencias;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LimpiezaArchivosBorrados {
    private final ArchivoBorradoPendienteRepository pendientes;
    private final AlmacenamientoArchivos almacenamiento;

    public LimpiezaArchivosBorrados(ArchivoBorradoPendienteRepository pendientes, AlmacenamientoArchivos almacenamiento) {
        this.pendientes = pendientes;
        this.almacenamiento = almacenamiento;
    }

    @Scheduled(fixedDelayString = "${app.archivos.reintento-borrado-ms:60000}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void procesarPendientes() {
        for (ArchivoBorradoPendiente pendiente : pendientes.findTop100ByOrderByCreadaEnAsc()) {
            try {
                almacenamiento.eliminar(pendiente.getClaveAlmacenamiento());
                pendientes.delete(pendiente);
            } catch (RuntimeException e) {
                String mensaje = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                pendiente.fallo(mensaje.substring(0, Math.min(2000, mensaje.length())));
            }
        }
    }
}
