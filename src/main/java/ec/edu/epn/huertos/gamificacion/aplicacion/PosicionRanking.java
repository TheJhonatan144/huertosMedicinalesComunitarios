package ec.edu.epn.huertos.gamificacion.aplicacion;

/**
 * Dónde está una comunidad en el ranking y cuánto le falta para subir. Se deriva del mismo ranking público
 * ({@link GamificacionServicio#ranking()}): se ordena por puntos comunitarios y las comunidades empatadas comparten
 * posición (1, 1, 3). No guarda nada ni otorga nada.
 *
 * @param posicion              puesto actual (empatadas comparten puesto)
 * @param total                 comunidades activas en el ranking
 * @param puntos                puntos comunitarios de esta comunidad
 * @param posicionSiguiente     puesto que se alcanzaría al subir; {@code null} si ya está en el primero
 * @param nombreSiguiente       comunidad inmediatamente por encima; {@code null} si ya está en el primero
 * @param faltanPuntos          puntos que faltan para alcanzar a la comunidad de arriba; {@code null} si va primera
 * @param actividadesParaSubir  actividades aprobadas necesarias para eso según los puntos por actividad vigentes
 * @param ventajaPuntos         si va primera: puntos de ventaja sobre la siguiente; {@code null} si no hay otra o no va primera
 * @param puntosPorActividad    puntos comunitarios que da cada actividad aprobada (configuración vigente)
 */
public record PosicionRanking(int posicion, int total, long puntos, Integer posicionSiguiente, String nombreSiguiente,
                              Long faltanPuntos, Integer actividadesParaSubir, Long ventajaPuntos,
                              int puntosPorActividad) {

    public boolean vaPrimera() {
        return posicion == 1;
    }

    /** Avance hacia la comunidad de arriba (0–100): puntos propios sobre los de quien está arriba. */
    public int porcentajeHaciaSiguiente() {
        if (faltanPuntos == null) {
            return 100;
        }
        long objetivo = puntos + faltanPuntos;
        return objetivo <= 0 ? 0 : (int) Math.min(100, Math.round(puntos * 100.0 / objetivo));
    }

    /** Comunidad sola en el ranking: no hay con quién compararse. */
    public boolean esUnica() {
        return total <= 1;
    }
}
