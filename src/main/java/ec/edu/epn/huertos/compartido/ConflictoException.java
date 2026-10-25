package ec.edu.epn.huertos.compartido;

/** El estado actual del recurso impide la operación (p. ej. envío ya revisado, cupo agotado). */
public class ConflictoException extends ReglaNegocioException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
