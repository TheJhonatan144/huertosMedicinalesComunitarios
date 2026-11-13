package ec.edu.epn.huertos.misiones.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.compartido.dominio.ConflictoException;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("unitaria")
class MisionHuertoTest {
    private MisionHuerto mision;

    @org.junit.jupiter.api.BeforeEach
    void prepararMisionDisponible() {
        mision = new MisionHuerto(10L, 20L, EstadoMision.DISPONIBLE);
    }

    @Test
    void unaMisionPasaDeDisponibleARevisionYCompletada() {
        mision.iniciar(30L);
        assertThat(mision.getEstado()).isEqualTo(EstadoMision.EN_REVISION);
        assertThat(mision.getActividadId()).isEqualTo(30L);
        mision.completar();
        assertThat(mision.getEstado()).isEqualTo(EstadoMision.COMPLETADA);
    }

    @Test
    void unRechazoPermiteReenviarLaMismaActividad() {
        mision.iniciar(30L);
        mision.rechazar();
        assertThat(mision.getEstado()).isEqualTo(EstadoMision.DISPONIBLE);
        assertThat(mision.getActividadId()).isEqualTo(30L);
        mision.reenviar();
        assertThat(mision.getEstado()).isEqualTo(EstadoMision.EN_REVISION);
    }

    @Test
    void noSePuedeIniciarUnaMisionBloqueada() {
        MisionHuerto mision = new MisionHuerto(10L, 20L, EstadoMision.BLOQUEADA);
        assertThatThrownBy(() -> mision.iniciar(30L)).isInstanceOf(ConflictoException.class);
    }
}
