package ec.edu.epn.huertos.huertos;

/** Diseño físico del huerto. Lista PROPUESTA (pendiente de aprobación del equipo). */
public enum DisenoHuerto {
    TIERRA("En tierra"),
    VERTICAL("Vertical"),
    COLGANTE("Colgante"),
    PISO("En piso (macetas)");

    private final String etiqueta;

    DisenoHuerto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
