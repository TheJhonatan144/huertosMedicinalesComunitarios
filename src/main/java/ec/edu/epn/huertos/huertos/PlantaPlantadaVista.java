package ec.edu.epn.huertos.huertos;

/**
 * Proyección de solo lectura de una planta elegible para el huerto virtual (plantada y activa). Nombre común y
 * científico salen de la especie del catálogo; el científico (puede ser null) elige la ilustración en {@link SpritePlanta}.
 */
public record PlantaPlantadaVista(Long id, String nombreComun, String nombreCientifico) {
}
