package ec.edu.epn.huertos.huertos.aplicacion;

import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;
import ec.edu.epn.huertos.huertos.dominio.Planta;

import java.time.Instant;

/** La especie se resuelve explícitamente en aplicación, dentro de la transacción. */
public record PlantaVista(
        Long id,
        Long huertoId,
        Long especieId,
        String nombreComun,
        String nombreCientifico,
        String descripcion,
        int cantidad,
        boolean activa,
        boolean plantada,
        EspecieVista especie,
        EstadoRevision estadoRevision,
        Instant plantadaEn,
        String observacionesRevision) {
    public static PlantaVista de(Planta p, EspecieVista e) {
        return new PlantaVista(
                p.getId(),
                p.getHuertoId(),
                p.getEspecieId(),
                e == null ? p.getNombreComun() : e.nombreComun(),
                e == null ? p.getNombreCientifico() : e.nombreCientifico(),
                p.getDescripcion(),
                p.getCantidad(),
                p.isActiva(),
                p.isPlantada(),
                e,
                p.getEstadoRevision(),
                p.getPlantadaEn(),
                p.getObservacionesRevision());
    }

    public Long getId() {
        return id;
    }

    public Long getHuertoId() {
        return huertoId;
    }

    public Long getEspecieId() {
        return especieId;
    }

    public EspecieVista getEspecie() {
        return especie;
    }

    public String getNombreComun() {
        return nombreComun;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isActiva() {
        return activa;
    }

    public boolean isPlantada() {
        return plantada;
    }

    public EstadoRevision getEstadoRevision() {
        return estadoRevision;
    }

    public Instant getPlantadaEn() {
        return plantadaEn;
    }

    public String getObservacionesRevision() {
        return observacionesRevision;
    }
}
