package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.gamificacion.ProgresoHuerto;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Datos para dibujar el huerto virtual 2D. Guarda los códigos tal como están en {@code huerto.tipo_suelo} y
 * {@code huerto.diseno}; un código vacío o desconocido se dibuja con apariencia/composición neutra.
 * Las etiquetas replican las del formulario del gestor ({@code GestorHuertoController}).
 */
public record HuertoVirtual(
        String nombreHuerto,
        String codigoSuelo,
        String codigoDiseno,
        List<PlantaVirtual> plantas,
        int totalPlantas,
        ProgresoHuerto progreso,
        boolean puedeRegistrar) {

    /** Planta dibujada: nombre común (etiqueta) y sprite ilustrado (nunca una foto real). */
    public record PlantaVirtual(String nombre, String sprite) {
        /** true = se dibuja con su archivo {@code /img/huerto/plantas/<sprite>.svg}; false = sprite genérico incrustado. */
        public boolean tieneArchivo() {
            return sprite != null && !SpritePlanta.GENERICA.equals(sprite);
        }
    }

    /** Los 6 suelos válidos de V8. */
    static final Map<String, String> SUELOS = Map.of(
            "ARCILLOSO", "Suelo arcilloso",
            "LIMOSO", "Suelo limoso",
            "ARENOSO", "Suelo arenoso",
            "MARGOSO", "Suelo margoso",
            "GREDOSO", "Suelo gredoso",
            "PANTANOSO", "Suelo pantanoso");

    /** Los 13 diseños válidos de V12. */
    static final Map<String, String> DISENOS = Map.ofEntries(
            Map.entry("TIERRA_SURCOS", "En tierra · surcos"),
            Map.entry("TIERRA_CAMADAS", "En tierra · camadas"),
            Map.entry("TIERRA_CAMELLONES", "En tierra · camellones"),
            Map.entry("TIERRA_TERRAZAS", "En tierra · terrazas"),
            Map.entry("VERTICAL_PAREDES", "Vertical · paredes"),
            Map.entry("COLGANTE_TECHOS", "Colgante · techos"),
            Map.entry("PISO_MACETAS", "En piso · macetas"),
            Map.entry("PISO_BOTELLAS_PATIO", "En piso · botellas en patio"),
            Map.entry("PISO_LLANTAS_PATIO", "En piso · llantas en patio"),
            Map.entry("PISO_TERRAZA", "En piso · terraza"),
            Map.entry("PISO_GRADAS", "En piso · gradas"),
            Map.entry("PISO_BALCONES", "En piso · balcones"),
            Map.entry("PISO_FILOS_VENTANA", "En piso · filos de ventanas"));

    /** Grupos de composición: uno por prefijo del código de diseño. */
    static final List<String> GRUPOS = List.of("TIERRA", "VERTICAL", "COLGANTE", "PISO");

    public static final String NEUTRO = "neutro";
    public static final String NEUTRA = "neutra";

    private static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
    }

    public boolean sueloRegistrado() {
        return SUELOS.containsKey(normalizar(codigoSuelo));
    }

    public boolean disenoRegistrado() {
        return DISENOS.containsKey(normalizar(codigoDiseno));
    }

    /** Sufijo de clase CSS del suelo; «neutro» si falta o es desconocido. */
    public String claveSuelo() {
        return sueloRegistrado() ? normalizar(codigoSuelo).toLowerCase(Locale.ROOT) : NEUTRO;
    }

    public String etiquetaSuelo() {
        return sueloRegistrado() ? SUELOS.get(normalizar(codigoSuelo)) : "Suelo sin registrar";
    }

    /** Composición base por prefijo (tierra, vertical, colgante, piso); «neutra» si el código no es uno de los 13. */
    public String composicion() {
        if (!disenoRegistrado()) {
            return NEUTRA;
        }
        String codigo = normalizar(codigoDiseno);
        return GRUPOS.stream().filter(g -> codigo.startsWith(g + "_")).findFirst()
                .map(g -> g.toLowerCase(Locale.ROOT)).orElse(NEUTRA);
    }

    public String etiquetaDiseno() {
        return disenoRegistrado() ? DISENOS.get(normalizar(codigoDiseno)) : "Diseño sin registrar";
    }

    /** Columnas de la cuadrícula cuadrada: 1 planta → 1, 2–4 → 2, 5–9 → 3 (raíz cuadrada redondeada hacia arriba). */
    public int columnas() {
        return Math.max(1, (int) Math.ceil(Math.sqrt(plantas.size())));
    }

    /** Hay más plantas elegibles que las dibujadas («Mostrando 2 de N»). */
    public boolean hayMasPlantas() {
        return totalPlantas > plantas.size();
    }

    /** Alternativa textual de toda la parcela para lectores de pantalla. */
    public String descripcionTextual() {
        String base = "Huerto virtual con " + etiquetaSuelo().toLowerCase(Locale.ROOT) + ", diseño "
                + etiquetaDiseno().toLowerCase(Locale.ROOT);
        if (plantas.isEmpty()) {
            return base + ", sin plantas plantadas.";
        }
        return base + ", con " + plantas.size() + (plantas.size() == 1 ? " planta: " : " plantas: ")
                + String.join(" y ", plantas.stream().map(PlantaVirtual::nombre).toList())
                + (hayMasPlantas() ? " (de " + totalPlantas + " plantadas)." : ".");
    }
}
