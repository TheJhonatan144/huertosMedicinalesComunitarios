package ec.edu.epn.huertos.huertos;

/** Tipos de suelo del huerto. Lista PROPUESTA (pendiente de aprobación del equipo). */
public enum TipoSuelo {
    FRANCO("Suelo franco"),
    ARCILLOSO("Suelo arcilloso"),
    ARENOSO("Suelo arenoso"),
    LIMOSO("Suelo limoso"),
    HUMIFERO("Suelo rico en humus");

    private final String etiqueta;

    TipoSuelo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
