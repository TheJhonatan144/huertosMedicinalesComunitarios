package ec.edu.epn.huertos.comunidades.aplicacion;

import ec.edu.epn.huertos.comunidades.dominio.*;

import java.math.*;
import java.time.*;
import java.util.*;

/** Instantánea inmutable para presentación; no contiene entidades JPA. */
public record ComunidadVista(
        Long id,
        String nombre,
        String descripcion,
        String tipoComunidad,
        String provincia,
        String canton,
        String direccionExacta,
        String contactoTelefono,
        String contactoCorreo,
        double latitud,
        double longitud,
        boolean activa,
        Long archivadaPor,
        Instant archivadaEn,
        boolean eliminacionPendiente) {
    public static ComunidadVista de(Comunidad e) {
        return e == null
                ? null
                : new ComunidadVista(
                        e.getId(),
                        e.getNombre(),
                        e.getDescripcion(),
                        e.getTipoComunidad(),
                        e.getProvincia(),
                        e.getCanton(),
                        e.getDireccionExacta(),
                        e.getContactoTelefono(),
                        e.getContactoCorreo(),
                        e.getLatitud(),
                        e.getLongitud(),
                        e.isActiva(),
                        e.getArchivadaPor(),
                        e.getArchivadaEn(),
                        e.isEliminacionPendiente());
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipoComunidad() {
        return tipoComunidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getCanton() {
        return canton;
    }

    public String getDireccionExacta() {
        return direccionExacta;
    }

    public String getContactoTelefono() {
        return contactoTelefono;
    }

    public String getContactoCorreo() {
        return contactoCorreo;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public boolean isActiva() {
        return activa;
    }

    public Long getArchivadaPor() {
        return archivadaPor;
    }

    public Instant getArchivadaEn() {
        return archivadaEn;
    }

    public boolean isEliminacionPendiente() {
        return eliminacionPendiente;
    }
}
