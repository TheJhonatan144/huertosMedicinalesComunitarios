package ec.edu.epn.huertos.gamificacion;

/** Puntos individuales con su nivel; {@code siguiente} y {@code faltan} son nulos en el nivel máximo. */
public record ResumenPuntos(long puntos, Nivel nivel, Nivel siguiente, Integer faltan) {
}
