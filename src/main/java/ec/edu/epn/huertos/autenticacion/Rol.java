package ec.edu.epn.huertos.autenticacion;

/**
 * Constantes técnicas de los roles. Correspondencia con los roles funcionales del proyecto:
 * GESTOR_HUERTO = Gestor de huerto; PARTICIPANTE = Participante;
 * ADMIN_EVIDENCIAS = Administrador de evidencias. El Visitante no tiene cuenta (acceso anónimo).
 */
public enum Rol {
    GESTOR_HUERTO("Gestor de huerto"),
    PARTICIPANTE("Participante"),
    ADMIN_EVIDENCIAS("Administrador de evidencias");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Nombre de la autoridad de Spring Security. */
    public String autoridad() {
        return "ROLE_" + name();
    }
}
