package ec.edu.epn.huertos.gamificacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

/** «Cómo subo en el ranking»: se deriva del mismo orden del ranking público (empates comparten puesto). */
class PosicionRankingTest {

    private static final PropiedadesGamificacion CONFIG = new PropiedadesGamificacion(10, 20, 5, 50, 150, 100,
            List.of(new PropiedadesGamificacion.Etapa("Semilla", 0)));

    private GamificacionServicio con(PuntosComunidad... filas) {
        RecompensaComunitariaRepository repo = mock(RecompensaComunitariaRepository.class);
        when(repo.puntosPorComunidad()).thenReturn(List.of(filas));
        return new GamificacionServicio(CONFIG, null, null, repo, null);
    }

    private static PuntosComunidad c(long id, String nombre, long puntos) {
        return new PuntosComunidad(id, nombre, "Pichincha", puntos);
    }

    @Test
    void laQueVaDetrasDiceCuantoLeFaltaYEnCuantasActividades() {
        GamificacionServicio s = con(c(1, "Sumak", 100), c(2, "Pacha", 60), c(3, "Wasi", 0));
        PosicionRanking r = s.posicionEnRanking(3L).orElseThrow();
        assertThat(r.posicion()).isEqualTo(3);
        assertThat(r.total()).isEqualTo(3);
        assertThat(r.nombreSiguiente()).isEqualTo("Pacha");
        assertThat(r.posicionSiguiente()).isEqualTo(2);
        assertThat(r.faltanPuntos()).isEqualTo(60);
        assertThat(r.actividadesParaSubir()).isEqualTo(3);              // 60 pts / 20 por actividad
        assertThat(r.vaPrimera()).isFalse();
        assertThat(r.porcentajeHaciaSiguiente()).isZero();

        PosicionRanking segunda = s.posicionEnRanking(2L).orElseThrow();
        assertThat(segunda.faltanPuntos()).isEqualTo(40);
        assertThat(segunda.actividadesParaSubir()).isEqualTo(2);
        assertThat(segunda.porcentajeHaciaSiguiente()).isEqualTo(60);   // 60 de 100
    }

    @Test
    void faltarMenosDeUnaActividadCuentaComoUna() {
        PosicionRanking r = con(c(1, "A", 25), c(2, "B", 20)).posicionEnRanking(2L).orElseThrow();
        assertThat(r.faltanPuntos()).isEqualTo(5);
        assertThat(r.actividadesParaSubir()).isEqualTo(1);
    }

    @Test
    void laPrimeraMuestraSuVentajaYNoTienePuestoSiguiente() {
        PosicionRanking r = con(c(1, "A", 100), c(2, "B", 60)).posicionEnRanking(1L).orElseThrow();
        assertThat(r.vaPrimera()).isTrue();
        assertThat(r.ventajaPuntos()).isEqualTo(40);
        assertThat(r.faltanPuntos()).isNull();
        assertThat(r.nombreSiguiente()).isNull();
        assertThat(r.porcentajeHaciaSiguiente()).isEqualTo(100);
    }

    @Test
    void lasEmpatadasComparteneElPuestoYElSiguienteEsElGrupoDeArriba() {
        // 1, 1, 3, 3: las comunidades 3 y 4 empatan con 40 y alcanzan el puesto 1 al llegar a 100.
        GamificacionServicio s = con(c(1, "A", 100), c(2, "B", 100), c(3, "C", 40), c(4, "D", 40));
        PosicionRanking tercera = s.posicionEnRanking(4L).orElseThrow();
        assertThat(tercera.posicion()).isEqualTo(3);
        assertThat(tercera.posicionSiguiente()).isEqualTo(1);
        assertThat(tercera.faltanPuntos()).isEqualTo(60);
        assertThat(tercera.nombreSiguiente()).isEqualTo("A");           // primera del grupo, por nombre

        PosicionRanking lider = s.posicionEnRanking(2L).orElseThrow();
        assertThat(lider.vaPrimera()).isTrue();
        assertThat(lider.ventajaPuntos()).isEqualTo(60);                // sobre el siguiente puntaje distinto (40)
    }

    @Test
    void unaSolaComunidadEsLaUnicaYNoSeCompara() {
        PosicionRanking r = con(c(1, "A", 0)).posicionEnRanking(1L).orElseThrow();
        assertThat(r.esUnica()).isTrue();
        assertThat(r.vaPrimera()).isTrue();
        assertThat(r.ventajaPuntos()).isNull();
    }

    @Test
    void todasEnCeroComparteElPrimerPuestoSinVentaja() {
        PosicionRanking r = con(c(1, "A", 0), c(2, "B", 0)).posicionEnRanking(2L).orElseThrow();
        assertThat(r.posicion()).isEqualTo(1);
        assertThat(r.ventajaPuntos()).isNull();
        assertThat(r.esUnica()).isFalse();
    }

    @Test
    void unaComunidadFueraDelRankingNoTienePosicion() {
        assertThat(con(c(1, "A", 10)).posicionEnRanking(99L)).isEmpty();
    }
}
