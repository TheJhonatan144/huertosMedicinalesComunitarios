package ec.edu.epn.huertos.huertos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Códigos vacíos o desconocidos (no insertables en la BD por los CHECK): apariencia y composición
 * neutras.
 */
@org.junit.jupiter.api.Tag("unitaria")
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
        for (String diseno :
                new String[] {null, "", "TIERRA", "PISO_JARDIN", "VERTICAL", "HIDROPONICO"}) {
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
        List<HuertoVirtual.PlantaVirtual> dos =
                List.of(
                        new HuertoVirtual.PlantaVirtual("A", "generica"),
                        new HuertoVirtual.PlantaVirtual("B", "generica"));
        assertThat(
                        new HuertoVirtual("H", "LIMOSO", "PISO_MACETAS", dos, 2, null, false)
                                .hayMasPlantas())
                .isFalse();
        HuertoVirtual cinco = new HuertoVirtual("H", "LIMOSO", "PISO_MACETAS", dos, 5, null, false);
        assertThat(cinco.hayMasPlantas()).isTrue();
        assertThat(cinco.descripcionTextual())
                .contains("2 plantas: A y B")
                .contains("de 5 plantadas");
    }

    @Test
    void laCuadriculaCrecePorColumnasParaMantenerLaParcelaCuadrada() {
        int[][] casos = {{0, 1}, {1, 1}, {2, 2}, {4, 2}, {5, 3}, {9, 3}};
        for (int[] caso : casos) {
            List<HuertoVirtual.PlantaVirtual> ps =
                    java.util.stream.IntStream.range(0, caso[0])
                            .mapToObj(i -> new HuertoVirtual.PlantaVirtual("P" + i, "generica"))
                            .toList();
            assertThat(
                            new HuertoVirtual(
                                            "H", "LIMOSO", "PISO_MACETAS", ps, caso[0], null, false)
                                    .columnas())
                    .as(caso[0] + " plantas")
                    .isEqualTo(caso[1]);
        }
    }

    @Test
    void lasPlantasSeAcomodanEnMaximoDosHilerasDeLaEscena() {
        int[][] casos = {
            {0, 1}, {1, 1}, {2, 2}, {3, 2}, {4, 2}, {5, 3}, {6, 3}, {7, 4}, {8, 4}, {9, 5}
        };
        for (int[] caso : casos) {
            List<HuertoVirtual.PlantaVirtual> ps =
                    java.util.stream.IntStream.range(0, caso[0])
                            .mapToObj(i -> new HuertoVirtual.PlantaVirtual("P" + i, "generica"))
                            .toList();
            assertThat(
                            new HuertoVirtual(
                                            "H",
                                            "LIMOSO",
                                            "TIERRA_SURCOS",
                                            ps,
                                            caso[0],
                                            null,
                                            false)
                                    .columnasEscena())
                    .as(caso[0] + " plantas")
                    .isEqualTo(caso[1]);
        }
    }

    @Test
    void laEtapaDeLaEscenaSigueElProgresoDelHuerto() {
        record Caso(String etapa, int porcentaje, int esperado) {}
        for (Caso c :
                List.of(
                        new Caso("Semilla", 0, 0),
                        new Caso("Brote", 15, 1),
                        new Caso("Planta joven", 35, 2),
                        new Caso("Huerto floreciente", 70, 3),
                        new Caso("Etapa nueva configurada", 5, 0),
                        new Caso("Etapa nueva configurada", 20, 1),
                        new Caso("Etapa nueva configurada", 45, 2),
                        new Caso("Etapa nueva configurada", 90, 3))) {
            HuertoVirtual v =
                    new HuertoVirtual(
                            "H",
                            "LIMOSO",
                            "TIERRA_SURCOS",
                            List.of(),
                            0,
                            new ec.edu.epn.huertos.gamificacion.aplicacion.ProgresoHuerto(
                                    c.porcentaje(), 100, c.porcentaje(), c.etapa(), null, null),
                            false);
            assertThat(v.etapaEscena())
                    .as(c.etapa() + " " + c.porcentaje())
                    .isEqualTo(c.esperado());
        }
        assertThat(con("LIMOSO", "TIERRA_SURCOS").etapaEscena()).as("sin progreso").isZero();
    }
}
