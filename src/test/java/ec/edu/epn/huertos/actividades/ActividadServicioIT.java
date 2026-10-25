package ec.edu.epn.huertos.actividades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.PruebaIntegracion;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import ec.edu.epn.huertos.comunidades.Integrante;
import ec.edu.epn.huertos.evidencias.AlmacenamientoArchivos;
import ec.edu.epn.huertos.evidencias.EnvioEvidencia;
import ec.edu.epn.huertos.evidencias.EnvioEvidenciaRepository;
import ec.edu.epn.huertos.evidencias.EstadoRevision;
import ec.edu.epn.huertos.evidencias.RevisionServicio;
import ec.edu.epn.huertos.gamificacion.RecompensaComunitariaRepository;
import ec.edu.epn.huertos.gamificacion.RecompensaIndividualRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

class ActividadServicioIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired ActividadServicio servicio;
    @Autowired ActividadRepository actividades;
    @Autowired ActividadParticipanteRepository participantes;
    @Autowired EnvioEvidenciaRepository envios;
    @Autowired RecompensaIndividualRepository individuales;
    @Autowired RecompensaComunitariaRepository comunitarias;
    @Autowired AlmacenamientoArchivos almacenamiento;
    @Autowired RevisionServicio revision;
    @Autowired JdbcTemplate jdbc;

    private FormularioActividad form(List<Integrante> seleccion) {
        FormularioActividad f = new FormularioActividad();
        f.setTipo(TipoActividad.RIEGO);
        f.setDescripcion("Riego manual");
        f.setFecha(Fechas.hoy());
        f.setParticipanteIds(seleccion.stream().map(Integrante::getId).toList());
        return f;
    }

    @Test
    void registraUnaActividadPendienteConParticipantesYFotografiaPersistente() {
        Escenario e = fabrica.escenario(3);
        List<Integrante> seleccion = List.of(e.participantes().get(0), e.participantes().get(1), e.gestor());

        Long id = servicio.registrar(e.gestorUsuario().getId(), form(seleccion), FabricaDatos.foto());

        Actividad a = actividades.findById(id).orElseThrow();
        assertThat(a.getEstado()).isEqualTo(EstadoRevision.PENDIENTE);
        assertThat(a.isParticipantesFijados()).isTrue();
        assertThat(participantes.findByActividadId(id)).extracting(ActividadParticipante::getIntegranteId)
                .containsExactlyInAnyOrder(seleccion.stream().map(Integrante::getId).toArray(Long[]::new));
        List<EnvioEvidencia> historial = envios.findByActividadIdOrderByNumeroAsc(id);
        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).getNumero()).isEqualTo(1);
        assertThat(historial.get(0).getEstado()).isEqualTo(EstadoRevision.PENDIENTE);
        assertThat(almacenamiento.cargar(historial.get(0).getClaveAlmacenamiento()).exists()).isTrue();
        // El nombre original del cliente nunca forma parte de la clave interna.
        assertThat(historial.get(0).getClaveAlmacenamiento()).doesNotContain("foto");
    }

    @Test
    void unaActividadPendienteNoGeneraPuntosNiProgreso() {
        Escenario e = fabrica.escenario(2);
        Long id = fabrica.registrarActividad(e, e.participantes());

        assertThat(individuales.findByActividadId(id)).isEmpty();
        assertThat(comunitarias.existsByActividadId(id)).isFalse();
        assertThat(comunitarias.totalPuntos(e.comunidad().getId())).isZero();
        assertThat(comunitarias.totalProgreso(e.comunidad().getId())).isZero();
    }

    @Test
    void noPermiteSeleccionarIntegrantesDeOtraComunidad() {
        Escenario a = fabrica.escenario(2);
        Escenario b = fabrica.escenario(2);
        assertThatThrownBy(() -> servicio.registrar(a.gestorUsuario().getId(),
                form(List.of(a.participantes().get(0), b.participantes().get(0))), FabricaDatos.foto()))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(actividades.findByComunidadIdOrderByCreadaEnDesc(a.comunidad().getId())).isEmpty();
    }

    @Test
    void noPermiteSeleccionarIntegrantesDeBaja() {
        Escenario e = fabrica.escenario(2);
        Integrante deBaja = fabrica.participanteNuevo(e.comunidad(), false);
        assertThatThrownBy(() -> servicio.registrar(e.gestorUsuario().getId(),
                form(List.of(e.participantes().get(0), deBaja)), FabricaDatos.foto()))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("activos");
    }

    @Test
    void exigeAlMenosUnParticipanteYUnaFotografiaValida() {
        Escenario e = fabrica.escenario(1);
        assertThatThrownBy(() -> servicio.registrar(e.gestorUsuario().getId(), form(List.of()), FabricaDatos.foto()))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("al menos un participante");
        var falsa = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", "texto plano".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> servicio.registrar(e.gestorUsuario().getId(), form(e.participantes()), falsa))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("JPEG o PNG");
        assertThatThrownBy(() -> servicio.registrar(e.gestorUsuario().getId(), form(e.participantes()), null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(actividades.findByComunidadIdOrderByCreadaEnDesc(e.comunidad().getId())).isEmpty();
    }

    @Test
    void rechazaFechasFuturas() {
        Escenario e = fabrica.escenario(1);
        FormularioActividad f = form(e.participantes());
        f.setFecha(Fechas.hoy().plusDays(2));
        assertThatThrownBy(() -> servicio.registrar(e.gestorUsuario().getId(), f, FabricaDatos.foto()))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("futura");
    }

    @Test
    void soloElGestorPuedeRegistrar() {
        Escenario e = fabrica.escenario(2);
        assertThatThrownBy(() -> servicio.registrar(e.participanteUsuarios().get(0).getId(), form(e.participantes()), FabricaDatos.foto()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicio.registrar(e.revisor().getId(), form(e.participantes()), FabricaDatos.foto()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void laListaDeParticipantesEsInmutableTambienEnLaBaseDeDatos() {
        Escenario e = fabrica.escenario(3);
        Long id = fabrica.registrarActividad(e, List.of(e.participantes().get(0)));
        Long otro = e.participantes().get(1).getId();
        Long comunidad = e.comunidad().getId();

        assertThatThrownBy(() -> jdbc.update(
                "insert into actividad_participante (actividad_id, integrante_id, comunidad_id) values (?, ?, ?)", id, otro, comunidad))
                .hasMessageContaining("participantes_fijados");
        assertThatThrownBy(() -> jdbc.update("delete from actividad_participante where actividad_id = ?", id))
                .hasMessageContaining("registro_inmutable");
        assertThatThrownBy(() -> jdbc.update("update actividad_participante set integrante_id = ? where actividad_id = ?", otro, id))
                .hasMessageContaining("registro_inmutable");
        assertThat(participantes.findByActividadId(id)).hasSize(1);
    }

    @Test
    void labaseDeDatosImpideParticipantesDeOtraComunidad() {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        // Actividad "abierta" insertada a mano (sin fijar) para probar la clave compuesta.
        Long actividadId = jdbc.queryForObject("""
                insert into actividad (comunidad_id, huerto_id, registrada_por, tipo, descripcion, fecha_actividad)
                values (?, ?, ?, 'RIEGO', 'x', current_date) returning id
                """, Long.class, a.comunidad().getId(), a.huerto().getId(), a.gestorUsuario().getId());
        assertThatThrownBy(() -> jdbc.update(
                "insert into actividad_participante (actividad_id, integrante_id, comunidad_id) values (?, ?, ?)",
                actividadId, b.participantes().get(0).getId(), a.comunidad().getId()))
                .hasMessageContaining("foreign key");
    }

    @Test
    void puedeEditarseMientrasEstaPendienteSinAlterarSusParticipantes() {
        Escenario e = fabrica.escenario(2);
        Long id = fabrica.registrarActividad(e, List.of(e.participantes().get(0)));
        FormularioActividad cambios = form(e.participantes()); // intenta "colar" más participantes
        cambios.setTipo(TipoActividad.DESHIERBE);
        cambios.setDescripcion("Deshierbe corregido");

        servicio.editar(e.gestorUsuario().getId(), id, cambios);

        Actividad a = actividades.findById(id).orElseThrow();
        assertThat(a.getTipo()).isEqualTo(TipoActividad.DESHIERBE);
        assertThat(a.getDescripcion()).isEqualTo("Deshierbe corregido");
        assertThat(participantes.findByActividadId(id)).hasSize(1);
    }

    @Test
    void noPuedeEditarActividadesAjenasNiYaRevisadas() {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        Long id = fabrica.registrarActividad(a, a.participantes());

        assertThatThrownBy(() -> servicio.editar(b.gestorUsuario().getId(), id, form(a.participantes())))
                .isInstanceOf(AccessDeniedException.class);

        Long envio = envios.findByActividadIdOrderByNumeroAsc(id).get(0).getId();
        revision.decidir(a.revisor().getId(), envio, EstadoRevision.RECHAZADA, "Foto borrosa");
        assertThatThrownBy(() -> servicio.editar(a.gestorUsuario().getId(), id, form(a.participantes())))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void elReenvioSoloAplicaATrasUnRechazoYConservaElHistorial() {
        Escenario e = fabrica.escenario(2);
        Long id = fabrica.registrarActividad(e, e.participantes());

        assertThatThrownBy(() -> servicio.reenviar(e.gestorUsuario().getId(), id, form(e.participantes()), null, FabricaDatos.foto()))
                .isInstanceOf(ConflictoException.class);

        Long envio1 = envios.findByActividadIdOrderByNumeroAsc(id).get(0).getId();
        revision.decidir(e.revisor().getId(), envio1, EstadoRevision.RECHAZADA, "No se ve el riego");
        servicio.reenviar(e.gestorUsuario().getId(), id, form(e.participantes()), "Nueva foto más clara", FabricaDatos.foto());

        List<EnvioEvidencia> historial = envios.findByActividadIdOrderByNumeroAsc(id);
        assertThat(historial).extracting(EnvioEvidencia::getNumero).containsExactly(1, 2);
        assertThat(historial).extracting(EnvioEvidencia::getEstado)
                .containsExactly(EstadoRevision.RECHAZADA, EstadoRevision.PENDIENTE);
        assertThat(historial.get(1).getComentario()).isEqualTo("Nueva foto más clara");
        assertThat(actividades.findById(id).orElseThrow().getEstado()).isEqualTo(EstadoRevision.PENDIENTE);
        // Los dos archivos físicos siguen disponibles (historial completo).
        historial.forEach(h -> assertThat(almacenamiento.cargar(h.getClaveAlmacenamiento()).exists()).isTrue());
        assertThat(participantes.findByActividadId(id)).hasSize(2);
    }
}
