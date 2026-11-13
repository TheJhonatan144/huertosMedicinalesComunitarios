package ec.edu.epn.huertos.huertos.aplicacion;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Ilustración 2D del huerto virtual para una especie. La clave es también el nombre del archivo
 * {@code static/img/huerto/plantas/<clave>.svg}. Sin ilustración propia se usa la genérica (no se inventa otra especie).
 *
 * <p>Se elige primero por NOMBRE CIENTÍFICO (sin autor, tildes, «×» ni «sect.»; se compara género y epíteto, o solo el
 * género si la fila lo indica así) y, si no hay o no coincide, por NOMBRE COMÚN normalizado.
 *
 * <p>Agregar una especie = copiar su SVG a {@code frontend/static/img/huerto/plantas/<clave>.svg} y añadir UNA línea a
 * {@link #ILUSTRACIONES}.
 */
public final class SpritePlanta {

    public static final String GENERICA = "generica";

    /** Fila de la tabla: clave/archivo, nombre científico (género y epíteto, o solo género; null = solo por nombre común) y nombres comunes. */
    record Ilustracion(String clave, String cientifico, String... nombresComunes) {
    }

    /** Tabla especie → ilustración. Una línea por especie. */
    static final List<Ilustracion> ILUSTRACIONES = List.of(
            new Ilustracion("achiote", "Bixa orellana", "achiote"),
            new Ilustracion("aguacate", "Persea americana", "aguacate"),
            new Ilustracion("albahaca", "Ocimum basilicum", "albahaca"),
            new Ilustracion("algodon", "Gossypium barbadense", "algodón"),
            new Ilustracion("cafe", "Coffea arabica", "café arábigo", "café"),
            new Ilustracion("canela", "Mespilodaphne quixos", "canela amazónica", "canela"),
            new Ilustracion("ceibo", "Ceiba trischistandra", "ceibo costeño", "ceibo"),
            new Ilustracion("chilca", "Baccharis latifolia", "chilca"),
            new Ilustracion("escancel", "Iresine diffusa", "escancel"),
            new Ilustracion("guayusa", "Ilex guayusa", "guayusa"),
            new Ilustracion("jengibre", "Zingiber officinale", "jengibre"),
            new Ilustracion("manzanilla", "Matricaria chamomilla", "manzanilla"),
            new Ilustracion("matico", "Piper aduncum", "matico"),
            new Ilustracion("menta", "Mentha × piperita", "menta"),
            new Ilustracion("hierbabuena", "Mentha spicata", "hierbabuena"),
            new Ilustracion("llanten-mayor", "Plantago major", "llantén mayor"),
            new Ilustracion("llanten-hoja-estrecha", "Plantago lanceolata", "llantén de hoja estrecha"),
            new Ilustracion("marco", "Ambrosia arborescens", "marco"),
            new Ilustracion("taraxaco", "Taraxacum", "taraxaco", "diente de león"),
            new Ilustracion("hierba-luisa", null, "hierba luisa"));

    private static final Pattern MARCAS = Pattern.compile("\\p{M}+");
    private static final Map<String, String> POR_CIENTIFICO = new HashMap<>();
    private static final Map<String, String> POR_COMUN = new HashMap<>();

    static {
        for (Ilustracion i : ILUSTRACIONES) {
            if (i.cientifico() != null && POR_CIENTIFICO.put(nombreCientifico(i.cientifico()), i.clave()) != null)
                throw new IllegalStateException("Nombre científico repetido en la tabla: " + i.cientifico());
            for (String comun : i.nombresComunes())
                if (POR_COMUN.put(nombreComun(comun), i.clave()) != null)
                    throw new IllegalStateException("Nombre común repetido en la tabla: " + comun);
        }
    }

    private SpritePlanta() {
    }

    /** Claves con archivo SVG propio (todas las de la tabla). */
    public static List<String> clavesConArchivo() {
        return ILUSTRACIONES.stream().map(Ilustracion::clave).toList();
    }

    /** Ilustración para una especie: primero por nombre científico, luego por nombre común; si nada coincide, genérica. */
    public static String paraEspecie(String nombreCientifico, String nombreComun) {
        String binomio = nombreCientifico(nombreCientifico);
        if (!binomio.isEmpty()) {
            String clave = POR_CIENTIFICO.get(binomio);
            if (clave == null) clave = POR_CIENTIFICO.get(binomio.split(" ")[0]);   // fila de solo género
            if (clave != null) return clave;
        }
        return POR_COMUN.getOrDefault(nombreComun(nombreComun), GENERICA);
    }

    /** Solo por nombre común (sin especie del catálogo). */
    public static String paraNombre(String nombreComun) {
        return paraEspecie(null, nombreComun);
    }

    /**
     * «Mentha × piperita L.» → «mentha piperita»; «Taraxacum sect. Taraxacum F.H.Wigg.» → «taraxacum».
     * Género = primera palabra; epíteto = la siguiente si empieza en minúscula (el autor y las secciones empiezan en
     * mayúscula o entre paréntesis). Se ignoran «×», la «x» híbrida y «sect.» (con el nombre de la sección).
     */
    static String nombreCientifico(String texto) {
        if (texto == null) return "";
        String limpio = sinTildes(texto).replace('×', ' ').trim();
        if (limpio.isEmpty()) return "";
        String[] palabras = limpio.split("\\s+");
        List<String> partes = new ArrayList<>();
        for (int i = 0; i < palabras.length; i++) {
            String p = palabras[i];
            if (p.equalsIgnoreCase("x")) continue;
            if (p.equalsIgnoreCase("sect.") || p.equalsIgnoreCase("sect")) { i++; continue; }
            partes.add(p);
        }
        if (partes.isEmpty()) return "";
        String genero = soloLetras(partes.get(0));
        if (genero.isEmpty()) return "";
        if (partes.size() > 1 && Character.isLowerCase(partes.get(1).charAt(0))) {
            String epiteto = soloLetras(partes.get(1));
            if (!epiteto.isEmpty()) return genero + " " + epiteto;
        }
        return genero;
    }

    /** Minúsculas, sin tildes y solo letras (los espacios y signos se ignoran): «Hierba luisa» = «hierbaluisa». */
    static String nombreComun(String texto) {
        return texto == null ? "" : soloLetras(sinTildes(texto));
    }

    private static String sinTildes(String texto) {
        return MARCAS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
    }

    private static String soloLetras(String texto) {
        return texto.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}
