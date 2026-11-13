package ec.edu.epn.huertos.huertos.aplicacion;

import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;

/** Proyección de lectura de una planta sembrada, incluyendo su estado de validación. */
public record PlantaPlantadaVista(
        Long id, String nombreComun, String nombreCientifico, EstadoRevision estadoRevision) {
    public PlantaPlantadaVista(Long id, String nombreComun, String nombreCientifico) {
        this(id, nombreComun, nombreCientifico, EstadoRevision.APROBADA);
    }
}
