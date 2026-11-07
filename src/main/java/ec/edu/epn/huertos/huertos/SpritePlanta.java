package ec.edu.epn.huertos.huertos;

import java.text.Normalizer;

/** Ilustración 2D disponible para una planta. Sin ilustración propia se usa la genérica (no se inventa otra especie). */
public final class SpritePlanta {

    public static final String MANZANILLA = "manzanilla";
    public static final String HIERBALUISA = "hierbaluisa";
    public static final String MENTA = "menta";
    public static final String GENERICA = "generica";

    private SpritePlanta() {
    }

    /** Elige el sprite por el nombre común, sin distinguir mayúsculas, tildes ni espacios. */
    public static String paraNombre(String nombreComun) {
        if (nombreComun == null) {
            return GENERICA;
        }
        String n = Normalizer.normalize(nombreComun, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase().replaceAll("[^a-z]", "");
        if (n.contains("manzanilla")) {
            return MANZANILLA;
        }
        if (n.contains("hierbaluisa") || n.contains("hierbaluiza") || n.equals("luisa")) {
            return HIERBALUISA;
        }
        if (n.contains("menta") || n.contains("hierbabuena")) {
            return MENTA;
        }
        return GENERICA;
    }
}
