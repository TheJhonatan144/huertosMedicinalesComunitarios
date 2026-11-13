package ec.edu.epn.huertos.huertos.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Regresión de la revisión de siembras conservando la referencia escalar a especie. */
@Tag("unitaria")
class PlantaTest {
    private Planta planta;

    @BeforeEach
    void prepararSiembra() {
        planta = new Planta(1L, 2L, "Menta", "Mentha piperita", 3, true);
    }

    @Test
    void sembrarDesdeCatalogoIniciaRevisionYFecha() {
        assertThat(planta.getEspecieId()).isEqualTo(2L);
        assertThat(planta.isPlantada()).isTrue();
        assertThat(planta.getEstadoRevision()).isEqualTo(EstadoRevision.PENDIENTE);
        assertThat(planta.getPlantadaEn()).isNotNull();
    }

    @Test
    void cambiarCantidadDeUnaSiembraNoReiniciaSuFechaNiRevision() {
        var fecha = planta.getPlantadaEn();
        planta.aplicarRevision(EstadoRevision.APROBADA, null);
        planta.cambiarSiembra(7, true);
        assertThat(planta.getCantidad()).isEqualTo(7);
        assertThat(planta.getPlantadaEn()).isEqualTo(fecha);
        assertThat(planta.getEstadoRevision()).isEqualTo(EstadoRevision.APROBADA);
    }

    @Test
    void aprobarConservaLaSiembraYSuFechaOriginal() {
        var fecha = planta.getPlantadaEn();
        planta.aplicarRevision(EstadoRevision.APROBADA, " Correcta ");
        assertThat(planta.isPlantada()).isTrue();
        assertThat(planta.getPlantadaEn()).isEqualTo(fecha);
        assertThat(planta.getObservacionesRevision()).isEqualTo("Correcta");
    }

    @Test
    void rechazarRetiraLaSiembraPeroConservaLaFicha() {
        planta.aplicarRevision(EstadoRevision.RECHAZADA, " Falta corregir ");
        assertThat(planta.isPlantada()).isFalse();
        assertThat(planta.isActiva()).isTrue();
        assertThat(planta.getEspecieId()).isEqualTo(2L);
        assertThat(planta.getObservacionesRevision()).isEqualTo("Falta corregir");
    }

    @Test
    void volverASembrarUnaRechazadaAbreRevisionYLimpiaObservaciones() {
        var fecha = planta.getPlantadaEn();
        planta.aplicarRevision(EstadoRevision.RECHAZADA, "Corregir");
        planta.cambiarPlantada(true);
        assertThat(planta.getEstadoRevision()).isEqualTo(EstadoRevision.PENDIENTE);
        assertThat(planta.getObservacionesRevision()).isNull();
        assertThat(planta.getPlantadaEn()).isAfterOrEqualTo(fecha);
    }

    @Test
    void noPermiteDecisionPendienteNiRevisarDosVeces() {
        assertThatThrownBy(() -> planta.aplicarRevision(EstadoRevision.PENDIENTE, null))
                .isInstanceOf(ConflictoException.class);
        planta.aplicarRevision(EstadoRevision.APROBADA, null);
        assertThatThrownBy(() -> planta.aplicarRevision(EstadoRevision.RECHAZADA, "Otra"))
                .isInstanceOf(ConflictoException.class);
        assertThat(planta.getEstadoRevision()).isEqualTo(EstadoRevision.APROBADA);
    }
}
