package ec.edu.epn.huertos.huertos.aplicacion;

import ec.edu.epn.huertos.huertos.dominio.*;

import java.math.*;
import java.time.*;
import java.util.*;

/** Instantánea inmutable para presentación; no contiene entidades JPA. */
public record HuertoVista(
        Long id,
        Long comunidadId,
        String nombre,
        String descripcion,
        String provincia,
        String parroquia,
        String direccion,
        BigDecimal areaM2,
        String diseno,
        String tipoSuelo,
        String fuenteAgua,
        String abonoNatural,
        String cerramiento,
        double latitud,
        double longitud) {
    public static HuertoVista de(Huerto e) {
        return e == null
                ? null
                : new HuertoVista(
                        e.getId(),
                        e.getComunidadId(),
                        e.getNombre(),
                        e.getDescripcion(),
                        e.getProvincia(),
                        e.getParroquia(),
                        e.getDireccion(),
                        e.getAreaM2(),
                        e.getDiseno(),
                        e.getTipoSuelo(),
                        e.getFuenteAgua(),
                        e.getAbonoNatural(),
                        e.getCerramiento(),
                        e.getLatitud(),
                        e.getLongitud());
    }

    public Long getId() {
        return id;
    }

    public Long getComunidadId() {
        return comunidadId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getParroquia() {
        return parroquia;
    }

    public String getDireccion() {
        return direccion;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public String getDiseno() {
        return diseno;
    }

    public String getTipoSuelo() {
        return tipoSuelo;
    }

    public String getFuenteAgua() {
        return fuenteAgua;
    }

    public String getAbonoNatural() {
        return abonoNatural;
    }

    public String getCerramiento() {
        return cerramiento;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }
}
