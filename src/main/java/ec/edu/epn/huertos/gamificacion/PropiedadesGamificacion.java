package ec.edu.epn.huertos.gamificacion;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * VALORES DE DEMOSTRACIÓN. RB06 (valores de recompensa) no está aprobada: todo es configurable
 * y no debe presentarse como regla definitiva (ver docs/pendientes.md).
 */
@ConfigurationProperties("app.gamificacion")
public record PropiedadesGamificacion(
        int puntosIndividualesPorActividad,
        int puntosComunitariosPorActividad,
        int progresoPorActividad,
        int umbralOficial,
        int umbralMaestro,
        int metaProgresoHuerto,
        List<Etapa> etapas) {

    public record Etapa(String nombre, int desde) {
    }
}
