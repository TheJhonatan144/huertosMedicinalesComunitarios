package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpritePlantaTest {

    @Test
    void elegeElSpritePorNombreSinImportarTildesNiMayusculas() {
        assertThat(SpritePlanta.paraNombre("Manzanilla")).isEqualTo(SpritePlanta.MANZANILLA);
        assertThat(SpritePlanta.paraNombre("  MANZANILLA común ")).isEqualTo(SpritePlanta.MANZANILLA);
        assertThat(SpritePlanta.paraNombre("Hierbaluisa")).isEqualTo(SpritePlanta.HIERBALUISA);
        assertThat(SpritePlanta.paraNombre("Hierba luisa")).isEqualTo(SpritePlanta.HIERBALUISA);
        assertThat(SpritePlanta.paraNombre("Menta")).isEqualTo(SpritePlanta.MENTA);
        assertThat(SpritePlanta.paraNombre("Hierbabuena")).isEqualTo(SpritePlanta.MENTA);
    }

    @Test
    void sinIlustracionPropiaUsaLaGenericaSinInventarEspecie() {
        assertThat(SpritePlanta.paraNombre("Ruda")).isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraNombre("")).isEqualTo(SpritePlanta.GENERICA);
        assertThat(SpritePlanta.paraNombre(null)).isEqualTo(SpritePlanta.GENERICA);
    }
}
