package ec.edu.epn.huertos.publico.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.dominio.Comunidad;
import ec.edu.epn.huertos.comunidades.persistencia.ComunidadRepository;
import ec.edu.epn.huertos.gamificacion.aplicacion.GamificacionServicio;
import ec.edu.epn.huertos.huertos.aplicacion.EspacioHuertoServicio;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoServicio;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoVirtualServicio;
import ec.edu.epn.huertos.huertos.persistencia.HuertoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

@org.junit.jupiter.api.Tag("unitaria")
class PublicoServicioTest {
    private ComunidadRepository comunidades;
    private ComunidadServicio miembros;
    private HuertoRepository huertos;
    private HuertoServicio plantas;
    private GamificacionServicio puntos;
    private EspacioHuertoServicio espacios;
    private HuertoVirtualServicio virtual;
    private PublicoServicio servicio;
    private Comunidad c;

    @BeforeEach
    void preparar() {
        comunidades = mock(ComunidadRepository.class);
        miembros = mock(ComunidadServicio.class);
        huertos = mock(HuertoRepository.class);
        plantas = mock(HuertoServicio.class);
        puntos = mock(GamificacionServicio.class);
        espacios = mock(EspacioHuertoServicio.class);
        virtual = mock(HuertoVirtualServicio.class);
        servicio =
                new PublicoServicio(
                        comunidades,
                        miembros,
                        huertos,
                        plantas,
                        puntos,
                        espacios,
                        virtual,
                        mock(ec.edu.epn.huertos.huertos.aplicacion.HuertosConsultaServicio.class));
        c = new Comunidad("Comunidad", "Descripción", "Pichincha", "Quito", -.2, -78.5);

        when(comunidades.findById(1L)).thenReturn(Optional.of(c));
        when(huertos.findByComunidadId(1L)).thenReturn(Optional.empty());
        when(puntos.ranking()).thenReturn(List.of());
        when(virtual.paraComunidad(1L)).thenReturn(Optional.empty());
    }

    @Test
    void consultaElFragmentoDelDuenoSoloParaUnaComunidadActiva() {
        servicio.ficha(1L);
        verify(virtual).paraComunidad(1L);
        verify(puntos, never()).otorgar(any());
    }

    @Test
    void fichaHistoricaConservaLecturaPeroNoInvocaElHuertoVirtual() {
        c.archivar(10L);
        assertThat(servicio.ficha(1L).huertoVirtual()).isNull();
        verifyNoInteractions(virtual);
    }

    @Test
    void eliminacionPendienteNoExponeFragmentoVirtual() {
        c.marcarEliminacionPendiente(true);
        assertThat(servicio.ficha(1L).huertoVirtual()).isNull();
        verifyNoInteractions(virtual);
    }
}
