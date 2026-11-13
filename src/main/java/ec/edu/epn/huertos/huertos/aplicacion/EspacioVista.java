package ec.edu.epn.huertos.huertos.aplicacion;

import ec.edu.epn.huertos.huertos.dominio.*;

import java.math.*;
import java.time.*;
import java.util.*;

/** Instantánea inmutable para presentación; no contiene entidades JPA. */
public record EspacioVista(
        Long id,
        Long huertoId,
        String nombre,
        String diseno,
        BigDecimal areaM2,
        String descripcion,
        boolean activa) {
    public static EspacioVista de(EspacioHuerto e) {
        return e == null
                ? null
                : new EspacioVista(
                        e.getId(),
                        e.getHuertoId(),
                        e.getNombre(),
                        e.getDiseno(),
                        e.getAreaM2(),
                        e.getDescripcion(),
                        e.isActiva());
    }

    public Long getId() {
        return id;
    }

    public Long getHuertoId() {
        return huertoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDiseno() {
        return diseno;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActiva() {
        return activa;
    }
}
