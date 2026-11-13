package ec.edu.epn.huertos.huertos.aplicacion;

import ec.edu.epn.huertos.huertos.dominio.*;

import java.math.*;
import java.time.*;
import java.util.*;

/** Instantánea inmutable para presentación; no contiene entidades JPA. */
public record EspecieVista(
        Long id,
        long version,
        Set<RegionPlanta> regiones,
        String clima,
        String fotoClave,
        String fotoTipo,
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
        String fotoFuente) {
    public static EspecieVista de(Especie e) {
        return e == null
                ? null
                : new EspecieVista(
                        e.getId(),
                        e.getVersion(),
                        e.getRegiones(),
                        e.getClima(),
                        e.getFotoClave(),
                        e.getFotoTipo(),
                        e.getNombreComun(),
                        e.getNombreCientifico(),
                        e.getOtrosNombres(),
                        e.getFamiliaBotanica(),
                        e.getAltitudMinima(),
                        e.getAltitudMaxima(),
                        e.getObservacionesAltitud(),
                        e.getProvincias(),
                        e.getCiclo(),
                        e.getObservacionesCiclo(),
                        e.getCualidad(),
                        e.getContextoCualidad(),
                        e.getUsosMedicinales(),
                        e.getSaberes(),
                        e.getFuenteDocumental(),
                        e.getFotoFuente());
    }

    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public Set<RegionPlanta> getRegiones() {
        return regiones;
    }

    public String getClima() {
        return clima;
    }

    public String getFotoClave() {
        return fotoClave;
    }

    public String getFotoTipo() {
        return fotoTipo;
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
}
