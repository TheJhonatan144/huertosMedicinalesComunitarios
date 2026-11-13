package ec.edu.epn.huertos.bodega.aplicacion;

import ec.edu.epn.huertos.bodega.dominio.*;

import java.math.*;
import java.time.*;
import java.util.*;

/** Instantánea inmutable para presentación; no contiene entidades JPA. */
public record ArticuloVista(
        Long id, Long huertoId, String nombre, CategoriaArticulo categoria, int cantidad) {
    public static ArticuloVista de(ArticuloBodega e) {
        return e == null
                ? null
                : new ArticuloVista(
                        e.getId(),
                        e.getHuertoId(),
                        e.getNombre(),
                        e.getCategoria(),
                        e.getCantidad());
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

    public CategoriaArticulo getCategoria() {
        return categoria;
    }

    public int getCantidad() {
        return cantidad;
    }
}
