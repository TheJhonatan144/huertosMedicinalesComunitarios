package ec.edu.epn.huertos.huertos.dominio;

import java.util.Set;

/** Datos de dominio del catálogo, independientes de formularios HTTP. */
public record DatosEspecie(
        String nombreComun,
        String nombreCientifico,
        String otrosNombres,
        String familiaBotanica,
        Integer altitudMinima,
        Integer altitudMaxima,
        String observacionesAltitud,
        String provincias,
        CicloPlanta ciclo,
        String observacionesCiclo,
        CualidadPlanta cualidad,
        String contextoCualidad,
        String usosMedicinales,
        String saberes,
        String fuenteDocumental,
        String fotoFuente,
        String clima,
        Set<RegionPlanta> regiones) {
    public DatosEspecie {
        regiones = Set.copyOf(regiones);
    }

    public String getNombreComun() {
        return nombreComun;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public String getOtrosNombres() {
        return otrosNombres;
    }

    public String getFamiliaBotanica() {
        return familiaBotanica;
    }

    public Integer getAltitudMinima() {
        return altitudMinima;
    }

    public Integer getAltitudMaxima() {
        return altitudMaxima;
    }

    public String getObservacionesAltitud() {
        return observacionesAltitud;
    }

    public String getProvincias() {
        return provincias;
    }

    public CicloPlanta getCiclo() {
        return ciclo;
    }

    public String getObservacionesCiclo() {
        return observacionesCiclo;
    }

    public CualidadPlanta getCualidad() {
        return cualidad;
    }

    public String getContextoCualidad() {
        return contextoCualidad;
    }

    public String getUsosMedicinales() {
        return usosMedicinales;
    }

    public String getSaberes() {
        return saberes;
    }

    public String getFuenteDocumental() {
        return fuenteDocumental;
    }

    public String getFotoFuente() {
        return fotoFuente;
    }

    public String getClima() {
        return clima;
    }

    public Set<RegionPlanta> getRegiones() {
        return regiones;
    }
}
