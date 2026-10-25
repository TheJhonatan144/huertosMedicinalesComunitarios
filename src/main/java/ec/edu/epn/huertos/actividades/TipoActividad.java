package ec.edu.epn.huertos.actividades;

/** Tipos de actividad del huerto. El riego es siempre una tarea manual registrada por personas. */
public enum TipoActividad {
    RIEGO("Riego manual"),
    SIEMBRA("Siembra"),
    DESHIERBE("Deshierbe"),
    ABONADO_ORGANICO("Abonado orgánico"),
    COSECHA("Cosecha"),
    MANTENIMIENTO("Mantenimiento del huerto");

    private final String etiqueta;

    TipoActividad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
