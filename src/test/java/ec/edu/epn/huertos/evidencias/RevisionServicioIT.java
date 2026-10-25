package ec.edu.epn.huertos.evidencias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.PruebaIntegracion;
import ec.edu.epn.huertos.actividades.ActividadRepository;
import ec.edu.epn.huertos.actividades.ActividadServicio;
import ec.edu.epn.huertos.actividades.FormularioActividad;
import ec.edu.epn.huertos.actividades.TipoActividad;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.comunidades.AsignacionRevisor;
import ec.edu.epn.huertos.comunidades.AsignacionRevisorRepository;
import ec.edu.epn.huertos.comunidades.Integrante;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import ec.edu.epn.huertos.gamificacion.FilaRanking;
import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import ec.edu.epn.huertos.gamificacion.RecompensaComunitaria;
import ec.edu.epn.huertos.gamificacion.RecompensaComunitariaRepository;
import ec.edu.epn.huertos.gamificacion.RecompensaIndividual;
import ec.edu.epn.huertos.gamificacion.RecompensaIndividualRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

/** Revisión, recompensas y concurrencia con PostgreSQL real. Valores de recompensa: configuración de demostración (10/20/5). */
class RevisionServicioIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired RevisionServicio revision;
    @Autowired ActividadServicio actividadServicio;
    @Autowired ActividadRepository actividades;
    @Autowired EnvioEvidenciaRepository envios;
    @Autowired RevisionRepository revisiones;
    @Autowired RecompensaIndividualRepository individuales;
    @Autowired RecompensaComunitariaRepository comunitarias;
    @Autowired GamificacionServicio gamificacion;
    @Autowired AsignacionRevisorRepository asignaciones;
    @Autowired JdbcTemplate jdbc;

    private Long envioPendiente(Long actividadId) {
        return envios.findByActividadIdOrderByNumeroAsc(actividadId).stream()
                .filter(e -> e.getEstado() == EstadoRevision.PENDIENTE).findFirst().orElseThrow().getId();
    }

    @Test
    void aprobarOtorgaLaMismaRecompensaATodosLosSeleccionadosYNadaAlResto() {
        Escenario e = fabrica.escenario(4);
        List<Integrante> seleccion = List.of(e.participantes().get(0), e.participantes().get(1), e.participantes().get(2));
        Long actividad = fabrica.registrarActividad(e, seleccion);

        RevisionServicio.Resultado r = revision.decidir(e.revisor().getId(), envioPendiente(actividad), EstadoRevision.APROBADA, null);

        assertThat(r.recompensaGenerada()).isTrue();
        List<RecompensaIndividual> filas = individuales.findByActividadId(actividad);
        assertThat(filas).hasSize(3);
        assertThat(filas).extracting(RecompensaIndividual::getPuntos).containsOnly(10);
        assertThat(individuales.totalPuntos(e.participantes().get(3).getId())).isZero();
        assertThat(individuales.totalPuntos(e.participantes().get(0).getId())).isEqualTo(10);
        assertThat(actividades.findById(actividad).orElseThrow().getEstado()).isEqualTo(EstadoRevision.APROBADA);
    }

    @Test
    void puntosIndividualesYComunitariosSonIndependientes() {
        Escenario e = fabrica.escenario(3);
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        revision.decidir(e.revisor().getId(), envioPendiente(actividad), EstadoRevision.APROBADA, null);

        RecompensaComunitaria comunitaria = comunitarias.findByActividadId(actividad).orElseThrow();
        assertThat(comunitaria.getPuntos()).isEqualTo(20);   // no es 10 x 3 participantes
        assertThat(comunitaria.getProgreso()).isEqualTo(5);
        assertThat(gamificacion.puntosComunitarios(e.comunidad().getId())).isEqualTo(20);
        assertThat(gamificacion.resumenIndividual(e.participantes().get(0).getId()).puntos()).isEqualTo(10);
        assertThat(gamificacion.progreso(e.comunidad().getId()).progreso()).isEqualTo(5);
    }

    @Test
    void rechazarExigeObservacionesYNoGeneraPuntosNiProgreso() {
        Escenario e = fabrica.escenario(2);
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        Long envio = envioPendiente(actividad);

        assertThatThrownBy(() -> revision.decidir(e.revisor().getId(), envio, EstadoRevision.RECHAZADA, "   "))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("observaciones");
        assertThat(envios.findById(envio).orElseThrow().getEstado()).isEqualTo(EstadoRevision.PENDIENTE);

        revision.decidir(e.revisor().getId(), envio, EstadoRevision.RECHAZADA, "La foto no muestra el huerto");

        assertThat(revisiones.findByEnvioId(envio).orElseThrow().getObservaciones()).isEqualTo("La foto no muestra el huerto");
        assertThat(individuales.findByActividadId(actividad)).isEmpty();
        assertThat(comunitarias.existsByActividadId(actividad)).isFalse();
        assertThat(actividades.findById(actividad).orElseThrow().getEstado()).isEqualTo(EstadoRevision.RECHAZADA);
    }

    @Test
    void rechazoYReenvioConservanElHistorialYAprobarPagaUnaSolaVez() {
        Escenario e = fabrica.escenario(2);
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        Long envio1 = envioPendiente(actividad);
        revision.decidir(e.revisor().getId(), envio1, EstadoRevision.RECHAZADA, "Foto borrosa");

        FormularioActividad correccion = new FormularioActividad();
        correccion.setTipo(TipoActividad.RIEGO);
        correccion.setDescripcion("Riego manual (corregido)");
        correccion.setFecha(Fechas.hoy());
        actividadServicio.reenviar(e.gestorUsuario().getId(), actividad, correccion, "Foto más clara", FabricaDatos.foto());
        Long envio2 = envioPendiente(actividad);
        assertThat(envio2).isNotEqualTo(envio1);

        revision.decidir(e.revisor().getId(), envio2, EstadoRevision.APROBADA, null);

        assertThat(envios.findByActividadIdOrderByNumeroAsc(actividad)).extracting(EnvioEvidencia::getEstado)
                .containsExactly(EstadoRevision.RECHAZADA, EstadoRevision.APROBADA);
        assertThat(revisiones.findByEnvioIdIn(List.of(envio1, envio2))).hasSize(2);
        assertThat(revisiones.findByEnvioId(envio1).orElseThrow().getObservaciones()).isEqualTo("Foto borrosa");
        assertThat(individuales.findByActividadId(actividad)).hasSize(2);
        assertThat(comunitarias.totalPuntos(e.comunidad().getId())).isEqualTo(20);
    }

    @Test
    void unaDecisionRepetidaNoDuplicaNiCambiaNada() {
        Escenario e = fabrica.escenario(2);
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        Long envio = envioPendiente(actividad);
        revision.decidir(e.revisor().getId(), envio, EstadoRevision.APROBADA, null);

        assertThatThrownBy(() -> revision.decidir(e.revisor().getId(), envio, EstadoRevision.APROBADA, null))
                .isInstanceOf(ConflictoException.class);
        assertThatThrownBy(() -> revision.decidir(e.revisor().getId(), envio, EstadoRevision.RECHAZADA, "Cambié de opinión"))
                .isInstanceOf(ConflictoException.class);

        assertThat(individuales.findByActividadId(actividad)).hasSize(2);
        assertThat(comunitarias.totalPuntos(e.comunidad().getId())).isEqualTo(20);
        assertThat(actividades.findById(actividad).orElseThrow().getEstado()).isEqualTo(EstadoRevision.APROBADA);
        assertThat(revisiones.findByEnvioIdIn(List.of(envio))).hasSize(1);
    }

    @Test
    void aprobacionesConcurrentesDeVariosRevisoresGeneranUnaSolaRecompensa() throws Exception {
        Escenario e = fabrica.escenario(3);
        Usuario segundo = fabrica.usuario("segundo-revisor-" + e.comunidad().getId(), Rol.ADMIN_EVIDENCIAS);
        asignaciones.save(new AsignacionRevisor(segundo.getId(), e.comunidad().getId()));
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        Long envio = envioPendiente(actividad);

        int hilos = 8;
        List<Callable<Boolean>> tareas = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            Long revisor = i % 2 == 0 ? e.revisor().getId() : segundo.getId();
            tareas.add(() -> {
                try {
                    revision.decidir(revisor, envio, EstadoRevision.APROBADA, null);
                    return true;
                } catch (ConflictoException yaRevisado) {
                    return false;
                }
            });
        }
        assertThat(ejecutarEnParalelo(tareas)).isEqualTo(1);

        assertThat(revisiones.findByEnvioIdIn(List.of(envio))).hasSize(1);
        assertThat(individuales.findByActividadId(actividad)).hasSize(3);
        assertThat(comunitarias.totalPuntos(e.comunidad().getId())).isEqualTo(20);
        assertThat(comunitarias.totalProgreso(e.comunidad().getId())).isEqualTo(5);
    }

    @Test
    void aprobarYRechazarAlMismoTiempoDejaUnaSolaDecisionCoherente() throws Exception {
        for (int intento = 0; intento < 5; intento++) {
            Escenario e = fabrica.escenario(2);
            Long actividad = fabrica.registrarActividad(e, e.participantes());
            Long envio = envioPendiente(actividad);
            List<Callable<Boolean>> tareas = List.of(
                    () -> intentar(() -> revision.decidir(e.revisor().getId(), envio, EstadoRevision.APROBADA, null)),
                    () -> intentar(() -> revision.decidir(e.revisor().getId(), envio, EstadoRevision.RECHAZADA, "Rechazo simultáneo")));
            assertThat(ejecutarEnParalelo(tareas)).isEqualTo(1);

            EstadoRevision estado = revisiones.findByEnvioId(envio).orElseThrow().getDecision();
            assertThat(actividades.findById(actividad).orElseThrow().getEstado()).isEqualTo(estado);
            assertThat(envios.findById(envio).orElseThrow().getEstado()).isEqualTo(estado);
            int esperadas = estado == EstadoRevision.APROBADA ? 2 : 0;
            assertThat(individuales.findByActividadId(actividad)).hasSize(esperadas);
            assertThat(comunitarias.existsByActividadId(actividad)).isEqualTo(estado == EstadoRevision.APROBADA);
        }
    }

    @Test
    void reenviosConcurrentesCreanUnSoloEnvio() throws Exception {
        Escenario e = fabrica.escenario(2);
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        revision.decidir(e.revisor().getId(), envioPendiente(actividad), EstadoRevision.RECHAZADA, "Otra foto, por favor");

        List<Callable<Boolean>> tareas = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tareas.add(() -> intentar(() -> {
                FormularioActividad f = new FormularioActividad();
                f.setTipo(TipoActividad.RIEGO);
                f.setDescripcion("Corrección");
                f.setFecha(Fechas.hoy());
                actividadServicio.reenviar(e.gestorUsuario().getId(), actividad, f, null, FabricaDatos.foto());
            }));
        }
        assertThat(ejecutarEnParalelo(tareas)).isEqualTo(1);
        assertThat(envios.findByActividadIdOrderByNumeroAsc(actividad)).hasSize(2);
    }

    @Test
    void soloUnRevisorAsignadoConRolPuedeDecidir() {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        Long actividad = fabrica.registrarActividad(a, a.participantes());
        Long envio = envioPendiente(actividad);

        assertThatThrownBy(() -> revision.decidir(b.revisor().getId(), envio, EstadoRevision.APROBADA, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> revision.decidir(a.gestorUsuario().getId(), envio, EstadoRevision.APROBADA, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> revision.decidir(a.participanteUsuarios().get(0).getId(), envio, EstadoRevision.APROBADA, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> revision.detalle(b.revisor().getId(), envio)).isInstanceOf(AccessDeniedException.class);
        assertThat(revision.bandeja(b.revisor().getId())).noneMatch(i -> i.envioId().equals(envio));
        assertThat(revision.bandeja(a.revisor().getId())).anyMatch(i -> i.envioId().equals(envio));
        assertThat(individuales.findByActividadId(actividad)).isEmpty();
    }

    @Test
    void nadiePuedeRevisarUnaActividadRegistradaPorSiMismo() {
        Escenario e = fabrica.escenario(1);
        // Caso límite: el revisor figura como registrante (insertado directamente; el servicio de actividades no lo permite).
        Long actividad = jdbc.queryForObject("""
                insert into actividad (comunidad_id, huerto_id, registrada_por, tipo, descripcion, fecha_actividad, participantes_fijados)
                values (?, ?, ?, 'RIEGO', 'x', current_date, false) returning id
                """, Long.class, e.comunidad().getId(), e.huerto().getId(), e.revisor().getId());
        jdbc.update("insert into actividad_participante (actividad_id, integrante_id, comunidad_id) values (?, ?, ?)",
                actividad, e.participantes().get(0).getId(), e.comunidad().getId());
        jdbc.update("update actividad set participantes_fijados = true where id = ?", actividad);
        Long envio = jdbc.queryForObject("""
                insert into envio_evidencia (actividad_id, numero, clave_almacenamiento, nombre_original, tipo_contenido, tamano_bytes, sha256)
                values (?, 1, ?, 'x.png', 'image/png', 10, 'abc') returning id
                """, Long.class, actividad, "2026/01/00000000-0000-0000-0000-" + String.format("%012d", actividad) + ".png");

        assertThatThrownBy(() -> revision.decidir(e.revisor().getId(), envio, EstadoRevision.APROBADA, null))
                .isInstanceOf(AccessDeniedException.class).hasMessageContaining("él mismo");
        // Y la base de datos lo impide aunque el servicio se evadiera.
        assertThatThrownBy(() -> jdbc.update(
                "insert into revision (envio_id, revisor_id, decision) values (?, ?, 'APROBADA')", envio, e.revisor().getId()))
                .hasMessageContaining("autorrevision_prohibida");
    }

    @Test
    void elRankingUsaSoloPuntosComunitariosYLosEmpatesComparten() {
        Escenario a = fabrica.escenario(5);
        Escenario b = fabrica.escenario(1);
        Escenario c = fabrica.escenario(1);
        // A: dos actividades aprobadas con 5 participantes (50 pts individuales por actividad, pero solo 20 comunitarios).
        for (int i = 0; i < 2; i++) {
            Long act = fabrica.registrarActividad(a, a.participantes());
            revision.decidir(a.revisor().getId(), envioPendiente(act), EstadoRevision.APROBADA, null);
        }
        // B y C: una actividad cada una.
        for (Escenario otro : List.of(b, c)) {
            Long act = fabrica.registrarActividad(otro, otro.participantes());
            revision.decidir(otro.revisor().getId(), envioPendiente(act), EstadoRevision.APROBADA, null);
        }

        List<FilaRanking> ranking = gamificacion.ranking();
        FilaRanking filaA = ranking.stream().filter(f -> f.comunidadId().equals(a.comunidad().getId())).findFirst().orElseThrow();
        FilaRanking filaB = ranking.stream().filter(f -> f.comunidadId().equals(b.comunidad().getId())).findFirst().orElseThrow();
        FilaRanking filaC = ranking.stream().filter(f -> f.comunidadId().equals(c.comunidad().getId())).findFirst().orElseThrow();
        assertThat(filaA.puntos()).isEqualTo(40);
        assertThat(filaB.puntos()).isEqualTo(20);
        assertThat(filaA.posicion()).isLessThan(filaB.posicion());
        assertThat(filaB.posicion()).isEqualTo(filaC.posicion());
        assertThat(ranking).isSortedAccordingTo((x, y) -> Long.compare(y.puntos(), x.puntos()));
    }

    @Test
    void laBaseDeDatosRechazaRecompensasDeActividadesNoAprobadasOParticipantesAjenos() {
        Escenario e = fabrica.escenario(2);
        Long actividad = fabrica.registrarActividad(e, List.of(e.participantes().get(0)));
        Long comunidad = e.comunidad().getId();

        assertThatThrownBy(() -> jdbc.update(
                "insert into recompensa_individual (actividad_id, integrante_id, comunidad_id, puntos) values (?, ?, ?, 10)",
                actividad, e.participantes().get(0).getId(), comunidad)).hasMessageContaining("actividad_no_aprobada");
        assertThatThrownBy(() -> jdbc.update(
                "insert into recompensa_comunitaria (actividad_id, comunidad_id, puntos, progreso) values (?, ?, 20, 5)",
                actividad, comunidad)).hasMessageContaining("actividad_no_aprobada");

        revision.decidir(e.revisor().getId(), envioPendiente(actividad), EstadoRevision.APROBADA, null);

        // Aprobada: un integrante que no estaba en la lista no puede recibir puntos, y no se puede duplicar.
        assertThatThrownBy(() -> jdbc.update(
                "insert into recompensa_individual (actividad_id, integrante_id, comunidad_id, puntos) values (?, ?, ?, 10)",
                actividad, e.participantes().get(1).getId(), comunidad)).hasMessageContaining("no_participante");
        assertThatThrownBy(() -> jdbc.update(
                "insert into recompensa_individual (actividad_id, integrante_id, comunidad_id, puntos) values (?, ?, ?, 10)",
                actividad, e.participantes().get(0).getId(), comunidad)).hasMessageContaining("recompensa_individual_actividad_id_integrante_id_key");
        assertThatThrownBy(() -> jdbc.update(
                "insert into recompensa_comunitaria (actividad_id, comunidad_id, puntos, progreso) values (?, ?, 20, 5)",
                actividad, comunidad)).hasMessageContaining("recompensa_comunitaria_actividad_id_key");
        assertThatThrownBy(() -> jdbc.update("update recompensa_comunitaria set puntos = 999 where actividad_id = ?", actividad))
                .hasMessageContaining("registro_inmutable");
    }

    @Test
    void elHistorialDeRevisionesEsInmutableYUnaSolaDecisionPorEnvio() {
        Escenario e = fabrica.escenario(1);
        Long actividad = fabrica.registrarActividad(e, e.participantes());
        Long envio = envioPendiente(actividad);
        revision.decidir(e.revisor().getId(), envio, EstadoRevision.RECHAZADA, "Sin foto clara");

        assertThatThrownBy(() -> jdbc.update("update revision set decision = 'APROBADA' where envio_id = ?", envio))
                .hasMessageContaining("registro_inmutable");
        assertThatThrownBy(() -> jdbc.update("delete from revision where envio_id = ?", envio))
                .hasMessageContaining("registro_inmutable");
        assertThatThrownBy(() -> jdbc.update("delete from envio_evidencia where id = ?", envio))
                .hasMessageContaining("registro_inmutable");
        assertThatThrownBy(() -> jdbc.update(
                "insert into revision (envio_id, revisor_id, decision) values (?, ?, 'APROBADA')", envio, e.revisor().getId()))
                .hasMessageContaining("revision_envio_id_key");
        // Rechazo sin observaciones también lo impide la base de datos.
        Long actividad2 = fabrica.registrarActividad(e, e.participantes());
        assertThatThrownBy(() -> jdbc.update(
                "insert into revision (envio_id, revisor_id, decision, observaciones) values (?, ?, 'RECHAZADA', '  ')",
                envioPendiente(actividad2), e.revisor().getId())).hasMessageContaining("revision_check");
    }

    private boolean intentar(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (ConflictoException | AccessDeniedException perdedor) {
            return false;
        }
    }

    /** Lanza las tareas a la vez y devuelve cuántas tuvieron éxito. */
    private long ejecutarEnParalelo(List<Callable<Boolean>> tareas) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tareas.size());
        CountDownLatch listos = new CountDownLatch(tareas.size());
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger errores = new AtomicInteger();
        List<Future<Boolean>> futuros = new ArrayList<>();
        for (Callable<Boolean> tarea : tareas) {
            futuros.add(pool.submit(() -> {
                listos.countDown();
                salida.await();
                try {
                    return tarea.call();
                } catch (Exception inesperada) {
                    errores.incrementAndGet();
                    throw inesperada;
                }
            }));
        }
        listos.await();
        salida.countDown();
        long exitos = 0;
        for (Future<Boolean> f : futuros) {
            if (f.get()) {
                exitos++;
            }
        }
        pool.shutdown();
        assertThat(errores.get()).as("excepciones inesperadas en los hilos").isZero();
        return exitos;
    }
}
