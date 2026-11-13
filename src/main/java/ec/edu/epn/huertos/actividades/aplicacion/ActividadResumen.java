package ec.edu.epn.huertos.actividades.aplicacion;

import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;

public record ActividadResumen(Long id, String tipo, String fecha, EstadoRevision estado, int participantes, int envios) {
}
