package ec.edu.epn.huertos.comunidades.aplicacion;
import ec.edu.epn.huertos.comunidades.dominio.EstadoSolicitudComunidad;
import ec.edu.epn.huertos.comunidades.dominio.TipoSolicitudComunidad;
import java.time.Instant;

public record SolicitudComunidadVista(Long id, TipoSolicitudComunidad tipo, EstadoSolicitudComunidad estado,
        Long solicitanteId, String solicitante, Long comunidadId, String nombreComunidad,
        String motivoSolicitud, String motivoRechazo, String detalleCreacion, Instant creadaEn) { }
