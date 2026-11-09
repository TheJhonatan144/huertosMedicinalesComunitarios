package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Códigos vacíos o desconocidos (no insertables en la BD por los CHECK): apariencia y composición neutras. */
class HuertoVirtualTest {

    private static HuertoVirtual con(String suelo, String diseno) {
        return new HuertoVirtual("Huerto", suelo, diseno, List.of(), 0, null, false);
    }

    @Test
    void sueloDesconocidoOVacioUsaAparienciaNeutra() {
        for (String suelo : new String[] {null, "", "FRANCO", "HUMIFERO", "arena"}) {
            HuertoVirtual v = con(suelo, "TIERRA_SURCOS");
            assertThat(v.sueloRegistrado()).as(String.valueOf(suelo)).isFalse();
            assertThat(v.claveSuelo()).isEqualTo("neutro");
            assertThat(v.etiquetaSuelo()).isEqualTo("Suelo sin registrar");
        }
        assertThat(con("margoso", null).claveSuelo()).isEqualTo("margoso");
    }

    @Test
    void disenoDesconocidoUsaComposicionNeutraAunqueTengaPrefijoConocido() {
        for (String diseno : new String[] {null, "", "TIERRA", "PISO_JARDIN", "VERTICAL", "HIDROPONICO"}) {
            HuertoVirtual v = con("LIMOSO", diseno);
            assertThat(v.composicion()).as(String.valueOf(diseno)).isEqualTo("neutra");
            assertThat(v.etiquetaDiseno()).isEqualTo("Diseño sin registrar");
        }
        assertThat(con("LIMOSO", "PISO_GRADAS").composicion()).isEqualTo("piso");
        assertThat(HuertoVirtual.DISENOS).hasSize(13);
        assertThat(HuertoVirtual.SUELOS).hasSize(6);
    }

    @Test
    void indicaCuandoHayMasPlantasQueLasDibujadas() {
        List<HuertoVirtual.PlantaVirtual> dos = List.of(new HuertoVirtual.PlantaVirtual("A", "generica"),
                new HuertoVirtual.PlantaVirtual("B", "generica"));
        assertThat(new HuertoVirtual("H", "LIMOSO", "PISO_MACETAS", dos, 2, null, false).hayMasPlantas()).isFalse();
        HuertoVirtual cinco = new HuertoVirtual("H", "LIMOSO", "PISO_MACETAS", dos, 5, null, false);
        assertThat(cinco.hayMasPlantas()).isTrue();
        assertThat(cinco.descripcionTextual()).contains("2 plantas: A y B").contains("de 5 plantadas");
    }

    @Test
    void laCuadriculaCrecePorColumnasParaMantenerLaParcelaCuadrada() {
        int[][] casos = {{0, 1}, {1, 1}, {2, 2}, {4, 2}, {5, 3}, {9, 3}};
        for (int[] caso : casos) {
            List<HuertoVirtual.PlantaVirtual> ps = java.util.stream.IntStream.range(0, caso[0])
                    .mapToObj(i -> new HuertoVirtual.PlantaVirtual("P" + i, "generica")).toList();
            assertThat(new HuertoVirtual("H", "LIMOSO", "PISO_MACETAS", ps, caso[0], null, false).columnas())
                    .as(caso[0] + " plantas").isEqualTo(caso[1]);
        }
    }
}
