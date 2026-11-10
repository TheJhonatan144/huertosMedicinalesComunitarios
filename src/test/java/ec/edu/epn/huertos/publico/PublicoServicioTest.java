package ec.edu.epn.huertos.publico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ec.edu.epn.huertos.comunidades.*;
import ec.edu.epn.huertos.gamificacion.*;
import ec.edu.epn.huertos.huertos.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicoServicioTest {
    ComunidadRepository comunidades = mock(ComunidadRepository.class);
    ComunidadServicio miembros = mock(ComunidadServicio.class);
    HuertoRepository huertos = mock(HuertoRepository.class);
    HuertoServicio plantas = mock(HuertoServicio.class);
    GamificacionServicio puntos = mock(GamificacionServicio.class);
    EspacioHuertoServicio espacios = mock(EspacioHuertoServicio.class);
    HuertoVirtualServicio virtual = mock(HuertoVirtualServicio.class);
    PublicoServicio servicio = new PublicoServicio(comunidades, miembros, huertos, plantas, puntos, espacios, virtual);
    Comunidad c = new Comunidad("Comunidad", "Descripción", "Pichincha", "Quito", -.2, -78.5);

    @BeforeEach void preparar() {
        when(comunidades.findById(1L)).thenReturn(Optional.of(c));
        when(huertos.findByComunidadId(1L)).thenReturn(Optional.empty());
        when(puntos.ranking()).thenReturn(List.of());
        when(virtual.paraComunidad(1L)).thenReturn(Optional.empty());
    }

    @Test void consultaElFragmentoDelDuenoSoloParaUnaComunidadActiva() {
        servicio.ficha(1L);
        verify(virtual).paraComunidad(1L);
        verify(puntos, never()).otorgar(any());
    }

    @Test void fichaHistoricaConservaLecturaPeroNoInvocaElHuertoVirtual() {
        c.archivar(10L);
        assertThat(servicio.ficha(1L).huertoVirtual()).isNull();
        verifyNoInteractions(virtual);
    }

    @Test void eliminacionPendienteNoExponeFragmentoVirtual() {
        c.marcarEliminacionPendiente(true);
        assertThat(servicio.ficha(1L).huertoVirtual()).isNull();
        verifyNoInteractions(virtual);
    }
}
