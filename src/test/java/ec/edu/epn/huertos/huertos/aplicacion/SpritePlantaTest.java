package ec.edu.epn.huertos.huertos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Elección de la ilustración: nombre científico primero (género + epíteto), luego nombre común; si
 * no, genérica.
 */
@org.junit.jupiter.api.Tag("unitaria")
class SpritePlantaTest {

    /**
     * Las 29 especies del catálogo con ilustración, con el nombre científico EXACTO de
     * docs/catalogo/carga-bloque-0*.sql.
     */
    static final Map<String, String> CATALOGO = new LinkedHashMap<>();

    static {
        CATALOGO.put("Bixa orellana L.", "achiote");
        CATALOGO.put("Persea americana Mill.", "aguacate");
        CATALOGO.put("Ocimum basilicum L.", "albahaca");
        CATALOGO.put("Gossypium barbadense L.", "algodon");
        CATALOGO.put("Coffea arabica L.", "cafe");
        CATALOGO.put("Mespilodaphne quixos (Lam.) Rohwer", "canela");
        CATALOGO.put("Ceiba trischistandra (A.Gray) Bakh.", "ceibo");
        CATALOGO.put("Baccharis latifolia (Ruiz & Pav.) Pers.", "chilca");
        CATALOGO.put("Iresine diffusa Humb. & Bonpl. ex Willd.", "escancel");
        CATALOGO.put("Ilex guayusa Loes.", "guayusa");
        CATALOGO.put("Zingiber officinale Roscoe", "jengibre");
        CATALOGO.put("Matricaria chamomilla L.", "manzanilla");
        CATALOGO.put("Piper aduncum L.", "matico");
        CATALOGO.put("Mentha × piperita L.", "menta");
        CATALOGO.put("Mentha spicata L.", "hierbabuena");
        CATALOGO.put("Plantago major L.", "llanten-mayor");
        CATALOGO.put("Plantago lanceolata L.", "llanten-hoja-estrecha");
        CATALOGO.put("Ambrosia arborescens Mill.", "marco");
        CATALOGO.put("Taraxacum sect. Taraxacum F.H.Wigg.", "taraxaco");
        CATALOGO.put("Origanum vulgare L.", "oregano");
        CATALOGO.put("Salvia rosmarinus Spenn.", "romero");
        CATALOGO.put("Epidendrum quitensium Rchb.f.", "orquidea");
        CATALOGO.put("Thymus vulgaris L.", "tomillo");
        CATALOGO.put("Melissa officinalis L.", "toronjil");
        CATALOGO.put("Urtica urens L.", "ortiga");
        CATALOGO.put("Viola odorata L.", "violeta");
        CATALOGO.put("Dysphania ambrosioides (L.) Mosyakin & Clemants", "paico");
        CATALOGO.put("Manihot esculenta Crantz", "yuca");
        CATALOGO.put("Uncaria tomentosa (Willd. ex Schult.) DC.", "una-de-gato");
    }

    @Test
    void cadaEspecieDelCatalogoConArchivoDaSuClavePorNombreCientifico() {
        assertThat(CATALOGO).hasSize(29);
        // Nombre común deliberadamente ajeno: la clave sale SOLO del nombre científico.
        CATALOGO.forEach(
                (cientifico, clave) ->
                        assertThat(SpritePlanta.paraEspecie(cientifico, "Nombre local sin tabla"))
                                .as(cientifico)
                                .isEqualTo(clave));
        // 29 del catálogo + hierba luisa y ruda (solo por nombre común); el sinónimo de romero no
        // agrega clave.
        assertThat(SpritePlanta.clavesConArchivo())
                .hasSize(31)
                .doesNotHaveDuplicates()
                .containsAll(CATALOGO.values())
                .contains("hierba-luisa", "ruda")
                .doesNotContain("sin-identificar-hojas-corazon");
    }

    @Test
    void normalizaHibridosSeccionesTildesYAutores() {
        assertThat(SpritePlanta.nombreCientifico("Mentha × piperita L."))
                .isEqualTo("mentha piperita");
        assertThat(SpritePlanta.nombreCientifico("Mentha ×piperita")).isEqualTo("mentha piperita");
        assertThat(SpritePlanta.nombreCientifico("Mentha x piperita L."))
                .isEqualTo("mentha piperita");
        assertThat(SpritePlanta.nombreCientifico("Taraxacum sect. Taraxacum F.H.Wigg."))
                .isEqualTo("taraxacum");
        assertThat(SpritePlanta.nombreCientifico("  ocimum   basilicum  "))
                .isEqualTo("ocimum basilicum");
        assertThat(SpritePlanta.nombreCientifico("Bixa orellana L.")).isEqualTo("bixa orellana");
        assertThat(SpritePlanta.paraEspecie("MENTHA SPICATA", null))
                .as("sin minúscula no hay epíteto")
                .isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraEspecie("Mentha x piperita", null)).isEqualTo("menta");
        // Romero acepta el nombre aceptado actual y el sinónimo clásico.
        assertThat(SpritePlanta.paraEspecie("Rosmarinus officinalis L.", "Nombre local"))
                .isEqualTo("romero");
        assertThat(SpritePlanta.paraEspecie("Salvia rosmarinus Spenn.", "Nombre local"))
                .isEqualTo("romero");
        // Taraxacum es fila de solo género: cualquier especie del género es taraxaco.
        assertThat(SpritePlanta.paraEspecie("Taraxacum officinale F.H.Wigg.", null))
                .isEqualTo("taraxaco");
    }

    @Test
    void elNombreCientificoManda() {
        // En el catálogo «Menta» es Mentha × piperita y «Hierbabuena» es Mentha spicata: el
        // científico decide.
        assertThat(SpritePlanta.paraEspecie("Mentha spicata L.", "Menta")).isEqualTo("hierbabuena");
        assertThat(SpritePlanta.paraEspecie("Mentha × piperita L.", "Hierbabuena"))
                .isEqualTo("menta");
        // El mismo género con otro epíteto no hereda la ilustración (no se inventa la especie).
        assertThat(SpritePlanta.paraEspecie("Mentha arvensis L.", "Menta de campo"))
                .isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraEspecie("Plantago media L.", null))
                .isEqualTo(SpritePlanta.GENERICA);
    }

    @Test
    void respaldoPorNombreComunSinEspecieONombreCientificoQueNoCoincide() {
        assertThat(SpritePlanta.paraNombre("Hierba luisa")).isEqualTo("hierba-luisa");
        assertThat(SpritePlanta.paraNombre("HIERBALUISA")).isEqualTo("hierba-luisa");
        assertThat(SpritePlanta.paraNombre("  Manzanilla ")).isEqualTo("manzanilla");
        assertThat(SpritePlanta.paraNombre("Llantén mayor")).isEqualTo("llanten-mayor");
        assertThat(SpritePlanta.paraNombre("llanten de hoja estrecha"))
                .isEqualTo("llanten-hoja-estrecha");
        assertThat(SpritePlanta.paraNombre("Café arábigo")).isEqualTo("cafe");
        assertThat(SpritePlanta.paraNombre("Diente de león")).isEqualTo("taraxaco");
        assertThat(SpritePlanta.paraNombre("Ruda"))
                .as("no está en el catálogo: solo por nombre común")
                .isEqualTo("ruda");
        assertThat(SpritePlanta.paraNombre("Uña de gato")).isEqualTo("una-de-gato");
        assertThat(SpritePlanta.paraNombre("Orquídea ecuatoriana")).isEqualTo("orquidea");
        assertThat(SpritePlanta.paraNombre("Romero")).isEqualTo("romero");
        assertThat(SpritePlanta.paraEspecie("", "Menta")).isEqualTo("menta");
        assertThat(SpritePlanta.paraEspecie("Cymbopogon citratus (DC.) Stapf", "Hierba luisa"))
                .isEqualTo("hierba-luisa");
    }

    @Test
    void especieDesconocidaUsaLaGenerica() {
        // Ajo sacha es la única especie del catálogo aún sin ilustración.
        assertThat(SpritePlanta.paraEspecie("Mansoa alliacea (Lam.) A.H.Gentry", "Ajo sacha"))
                .isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraEspecie("Salvia officinalis L.", "Salvia"))
                .as("mismo género que romero, otra especie")
                .isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraNombre("Anís")).isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraNombre("Manzanilla común"))
                .as("coincidencia exacta, no parcial")
                .isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraEspecie(null, null)).isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraEspecie("", "")).isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraEspecie("L.", "¿?")).isEqualTo(SpritePlanta.GENERICA);
    }
}
