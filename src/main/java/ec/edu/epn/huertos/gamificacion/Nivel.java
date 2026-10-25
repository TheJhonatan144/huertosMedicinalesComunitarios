package ec.edu.epn.huertos.gamificacion;

/** Niveles individuales. Maestro es el nivel superior y no tiene cupo máximo. */
public enum Nivel {
    APRENDIZ("Aprendiz"),
    OFICIAL("Oficial"),
    MAESTRO("Maestro");

    private final String etiqueta;

    Nivel(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
