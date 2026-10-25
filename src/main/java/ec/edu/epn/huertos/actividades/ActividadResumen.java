package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.evidencias.EstadoRevision;

public record ActividadResumen(Long id, String tipo, String fecha, EstadoRevision estado, int participantes, int envios) {
}
