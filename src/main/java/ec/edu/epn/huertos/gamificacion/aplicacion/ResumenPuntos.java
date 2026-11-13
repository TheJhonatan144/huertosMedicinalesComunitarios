package ec.edu.epn.huertos.gamificacion.aplicacion;
import ec.edu.epn.huertos.gamificacion.dominio.Nivel;

/** Puntos individuales con su nivel; {@code siguiente} y {@code faltan} son nulos en el nivel máximo. */
public record ResumenPuntos(long puntos, Nivel nivel, Nivel siguiente, Integer faltan, int porcentajeNivel) {
}
