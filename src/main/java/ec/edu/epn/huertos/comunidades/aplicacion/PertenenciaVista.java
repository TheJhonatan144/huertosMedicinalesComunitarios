package ec.edu.epn.huertos.comunidades.aplicacion;

import ec.edu.epn.huertos.comunidades.dominio.*;

import java.math.*;
import java.time.*;
import java.util.*;

/** Instantánea inmutable para presentación; no contiene entidades JPA. */
public record PertenenciaVista(
        Long id, Long comunidadId, Long usuarioId, boolean esGestor, boolean activo) {
    public static PertenenciaVista de(Integrante e) {
        return e == null
                ? null
                : new PertenenciaVista(
                        e.getId(),
                        e.getComunidadId(),
                        e.getUsuarioId(),
                        e.isEsGestor(),
                        e.isActivo());
    }

    public Long getId() {
        return id;
    }

    public Long getComunidadId() {
        return comunidadId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public boolean isEsGestor() {
        return esGestor;
    }

    public boolean isActivo() {
        return activo;
    }
}
