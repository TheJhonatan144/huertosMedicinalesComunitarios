package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.evidencias.EstadoRevision;
import java.time.LocalDate;
import java.util.List;

/**
 * Vista completa de una actividad con su historial. Los puntos son nulos mientras la actividad no esté aprobada.
 */
public record DetalleActividad(
        Long id,
        String comunidad,
        String huerto,
        TipoActividad tipo,
        String descripcion,
        String fecha,
        LocalDate fechaIso,
        EstadoRevision estado,
        String registradaPor,
        String creadaEn,
        List<ParticipanteVista> participantes,
        List<EnvioVista> envios,
        Integer puntosPorParticipante,
        Integer puntosComunitarios,
        Integer progreso) {

    public record EnvioVista(Long id, int numero, String enviadoEn, EstadoRevision estado, String comentario,
                             String archivo, String revisor, String revisadoEn, String observaciones) {
    }
}
