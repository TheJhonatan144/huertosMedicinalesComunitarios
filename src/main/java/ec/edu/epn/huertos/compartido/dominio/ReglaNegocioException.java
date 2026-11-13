package ec.edu.epn.huertos.compartido.dominio;

/** Violación de una regla de negocio; el mensaje es apto para mostrarse al usuario. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
