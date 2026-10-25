package ec.edu.epn.huertos.gamificacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Reglas puras de niveles y etapas con valores de demostración (RB06 no está aprobada). */
class GamificacionReglasTest {

    private final GamificacionServicio servicio = new GamificacionServicio(
            new PropiedadesGamificacion(10, 20, 5, 50, 150, 100, List.of(
                    new PropiedadesGamificacion.Etapa("Semilla", 0),
                    new PropiedadesGamificacion.Etapa("Brote", 10),
                    new PropiedadesGamificacion.Etapa("Planta joven", 30),
                    new PropiedadesGamificacion.Etapa("Huerto floreciente", 60))),
            null, null, null);

    @Test
    void losNivelesIndividualesRespetanLosUmbrales() {
        assertThat(servicio.nivelPara(0)).isEqualTo(Nivel.APRENDIZ);
        assertThat(servicio.nivelPara(49)).isEqualTo(Nivel.APRENDIZ);
        assertThat(servicio.nivelPara(50)).isEqualTo(Nivel.OFICIAL);
        assertThat(servicio.nivelPara(149)).isEqualTo(Nivel.OFICIAL);
        assertThat(servicio.nivelPara(150)).isEqualTo(Nivel.MAESTRO);
    }

    @Test
    void maestroNoTieneTopeNiCupo() {
        assertThat(servicio.nivelPara(1_000_000)).isEqualTo(Nivel.MAESTRO);
    }

    @Test
    void elProgresoDelHuertoAvanzaPorEtapas() {
        ProgresoHuerto inicial = servicio.progresoPara(0);
        assertThat(inicial.etapa()).isEqualTo("Semilla");
        assertThat(inicial.siguienteEtapa()).isEqualTo("Brote");
        assertThat(inicial.faltanParaSiguiente()).isEqualTo(10);
        assertThat(inicial.porcentaje()).isZero();

        ProgresoHuerto medio = servicio.progresoPara(35);
        assertThat(medio.etapa()).isEqualTo("Planta joven");
        assertThat(medio.siguienteEtapa()).isEqualTo("Huerto floreciente");
        assertThat(medio.faltanParaSiguiente()).isEqualTo(25);
        assertThat(medio.porcentaje()).isEqualTo(35);

        ProgresoHuerto completo = servicio.progresoPara(250);
        assertThat(completo.etapa()).isEqualTo("Huerto floreciente");
        assertThat(completo.siguienteEtapa()).isNull();
        assertThat(completo.porcentaje()).isEqualTo(100);
    }
}
