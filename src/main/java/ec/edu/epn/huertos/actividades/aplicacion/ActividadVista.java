package ec.edu.epn.huertos.actividades.aplicacion;

import ec.edu.epn.huertos.actividades.dominio.*;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;

import java.math.*;
import java.time.*;
import java.util.*;

/** Instantánea inmutable para presentación; no contiene entidades JPA. */
public record ActividadVista(
        Long id,
        Long comunidadId,
        Long huertoId,
        Long registradaPor,
        Long misionHuertoId,
        TipoActividad tipo,
        String descripcion,
        LocalDate fechaActividad,
        EstadoRevision estado,
        boolean participantesFijados,
        Instant creadaEn) {
    public static ActividadVista de(Actividad e) {
        return e == null
                ? null
                : new ActividadVista(
                        e.getId(),
                        e.getComunidadId(),
                        e.getHuertoId(),
                        e.getRegistradaPor(),
                        e.getMisionHuertoId(),
                        e.getTipo(),
                        e.getDescripcion(),
                        e.getFechaActividad(),
                        e.getEstado(),
                        e.isParticipantesFijados(),
                        e.getCreadaEn());
    }

    public Long getId() {
        return id;
    }

    public Long getComunidadId() {
        return comunidadId;
    }

    public Long getHuertoId() {
        return huertoId;
    }

    public Long getRegistradaPor() {
        return registradaPor;
    }

    public Long getMisionHuertoId() {
        return misionHuertoId;
    }

    public TipoActividad getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFechaActividad() {
        return fechaActividad;
    }

    public EstadoRevision getEstado() {
        return estado;
    }

    public boolean isParticipantesFijados() {
        return participantesFijados;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }
}
