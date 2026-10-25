package ec.edu.epn.huertos.gamificacion;

/** Progreso del huerto virtual: valor acumulado, meta, porcentaje y etapa actual/siguiente. */
public record ProgresoHuerto(int progreso, int meta, int porcentaje, String etapa, String siguienteEtapa,
                             Integer faltanParaSiguiente) {
}
