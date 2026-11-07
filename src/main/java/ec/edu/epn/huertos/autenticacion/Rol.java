package ec.edu.epn.huertos.autenticacion;

/**
 * Constantes técnicas de los roles. Correspondencia con los roles funcionales del proyecto:
 * GESTOR_HUERTO = Gestor de huerto; PARTICIPANTE = Participante;
 * SIN_ASIGNAR es una cuenta registrada que aún no pertenece a una comunidad.
 * ADMIN_MINISTERIO_SALUD = cuenta institucional del Ministerio; no se registra públicamente.
 */
public enum Rol {
    GESTOR_HUERTO("Gestor de huerto"),
    PARTICIPANTE("Participante"),
    ADMIN_MINISTERIO_SALUD("Administrador del Ministerio de Salud"),
    SIN_ASIGNAR("Cuenta sin comunidad");

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
