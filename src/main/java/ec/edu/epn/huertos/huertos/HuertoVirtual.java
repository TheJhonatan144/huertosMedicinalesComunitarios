package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.gamificacion.ProgresoHuerto;
import java.util.List;

/**
 * Datos para dibujar el huerto virtual 2D. {@code suelo} y {@code diseno} son nulos si el huerto no los tiene
 * registrados; en ese caso la vista usa una apariencia neutra y muestra {@code camposFaltantes}.
 */
public record HuertoVirtual(
        String nombreHuerto,
        TipoSuelo suelo,
        DisenoHuerto diseno,
        List<PlantaVirtual> plantas,
        int plantasNoMostradas,
        ProgresoHuerto progreso,
        List<String> camposFaltantes,
        boolean puedeRegistrar) {

    /** Planta registrada como plantada: nombre común (etiqueta) y sprite ilustrado (nunca la foto real). */
    public record PlantaVirtual(String nombre, String sprite) {
    }

    /** Sufijo de clase CSS del suelo; «neutro» si no está registrado. */
    public String claveSuelo() {
        return suelo == null ? "neutro" : suelo.name().toLowerCase();
    }

    /** Sufijo de clase CSS del diseño; «predeterminado» si no está registrado. */
    public String claveDiseno() {
        return diseno == null ? "predeterminado" : diseno.name().toLowerCase();
    }

    public String etiquetaSuelo() {
        return suelo == null ? "Suelo sin registrar" : suelo.etiqueta();
    }
}
