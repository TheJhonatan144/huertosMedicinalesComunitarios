package ec.edu.epn.huertos.comunidades.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.autenticacion.dominio.Usuario;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.comunidades.dominio.Comunidad;
import ec.edu.epn.huertos.comunidades.dominio.EstadoSolicitudComunidad;
import ec.edu.epn.huertos.comunidades.dominio.SolicitudComunidad;
import ec.edu.epn.huertos.comunidades.dominio.TipoSolicitudComunidad;
import ec.edu.epn.huertos.comunidades.persistencia.ComunidadRepository;
import ec.edu.epn.huertos.comunidades.persistencia.IntegranteRepository;
import ec.edu.epn.huertos.comunidades.persistencia.SolicitudComunidadRepository;
import ec.edu.epn.huertos.evidencias.aplicacion.RevisionServicio;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;
import ec.edu.epn.huertos.evidencias.persistencia.EnvioEvidenciaRepository;
import ec.edu.epn.huertos.gamificacion.persistencia.RecompensaComunitariaRepository;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;
import ec.edu.epn.huertos.soporte.integracion.PruebaIntegracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@AutoConfigureMockMvc
class SolicitudesComunidadIT extends PruebaIntegracion {
    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    @Autowired SolicitudComunidadServicio solicitudes;
    @Autowired SolicitudComunidadRepository solicitudesRepo;
    @Autowired ComunidadRepository comunidades;
    @Autowired IntegranteRepository integrantes;
    @Autowired UsuarioRepository usuarios;
    @Autowired FabricaDatos fabrica;
    @Autowired RevisionServicio revisiones;
    @Autowired EnvioEvidenciaRepository envios;
    @Autowired ComunidadServicio servicioComunidades;
    @Autowired JdbcTemplate jdbc;
    @Autowired RecompensaComunitariaRepository ranking;
    @Autowired MockMvc mvc;

    @Test
    void creacionEsPendienteHastaAprobacionYDespuesAsignaGestor() {
        int n = SECUENCIA.incrementAndGet();
        Usuario solicitante = cuentaSinComunidad(n);
        Usuario ministerio = fabrica.usuario("msp-creacion-" + n, Rol.ADMIN_MINISTERIO_SALUD);
        FormularioAltaComunidad formulario = alta("Comunidad solicitada " + n);

        solicitudes.solicitarCreacion(solicitante.getId(), formulario);
        SolicitudComunidad solicitud =
                solicitudesRepo
                        .findBySolicitanteIdOrderByCreadaEnDesc(solicitante.getId())
                        .getFirst();
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitudComunidad.PENDIENTE);
        assertThat(comunidades.existsByNombreIgnoreCase(formulario.getNombreComunidad())).isFalse();

        solicitudes.aprobar(ministerio.getId(), solicitud.getId());

        Comunidad creada =
                comunidades.findAll().stream()
                        .filter(c -> c.getNombre().equals(formulario.getNombreComunidad()))
                        .findFirst()
                        .orElseThrow();
        assertThat(usuarios.findById(solicitante.getId()).orElseThrow().getRol())
                .isEqualTo(Rol.GESTOR_HUERTO);
        assertThat(
                        integrantes
                                .findByUsuarioIdAndActivoTrue(solicitante.getId())
                                .orElseThrow()
                                .isEsGestor())
                .isTrue();
        assertThat(solicitudesRepo.findById(solicitud.getId()).orElseThrow().getDatosJson())
                .isNull();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from huerto where comunidad_id = ?",
                                Long.class,
                                creada.getId()))
                .isEqualTo(1L);
    }

    @Test
    void rechazoRequiereMotivoYLoConservaParaLaCuentaSolicitante() {
        int n = SECUENCIA.incrementAndGet();
        Usuario solicitante = cuentaSinComunidad(n);
        Usuario ministerio = fabrica.usuario("msp-rechazo-" + n, Rol.ADMIN_MINISTERIO_SALUD);
        solicitudes.solicitarCreacion(solicitante.getId(), alta("Comunidad rechazada " + n));
        SolicitudComunidad solicitud =
                solicitudesRepo
                        .findBySolicitanteIdOrderByCreadaEnDesc(solicitante.getId())
                        .getFirst();

        solicitudes.rechazar(
                ministerio.getId(), solicitud.getId(), "Falta confirmar la ubicación.");

        SolicitudComunidad resuelta = solicitudesRepo.findById(solicitud.getId()).orElseThrow();
        assertThat(resuelta.getEstado()).isEqualTo(EstadoSolicitudComunidad.RECHAZADA);
        assertThat(resuelta.getMotivoRechazo()).isEqualTo("Falta confirmar la ubicación.");
        assertThat(resuelta.getDatosJson()).isNull();
        assertThat(usuarios.findById(solicitante.getId()).orElseThrow().getRol())
                .isEqualTo(Rol.SIN_ASIGNAR);
    }

    @Test
    void eliminarAprobadaBorraHistorialComunitarioPeroConservaCuentasYAuditoria() {
        int n = SECUENCIA.incrementAndGet();
        FabricaDatos.Escenario e = fabrica.escenario(1);
        Long comunidadId = e.comunidad().getId();
        Long actividadId = fabrica.registrarActividad(e, List.of(e.gestor()));
        Long envioId = envios.findByActividadIdOrderByNumeroAsc(actividadId).getFirst().getId();
        revisiones.decidir(e.revisor().getId(), envioId, EstadoRevision.APROBADA, null);

        solicitudes.solicitarEliminacion(
                e.gestorUsuario().getId(), comunidadId, "La comunidad dejó de funcionar.");
        assertThat(comunidades.findById(comunidadId).orElseThrow().isEliminacionPendiente())
                .isTrue();
        solicitudes.aprobar(
                e.revisor().getId(),
                solicitudesRepo
                        .findBySolicitanteIdOrderByCreadaEnDesc(e.gestorUsuario().getId())
                        .getFirst()
                        .getId());

        assertThat(comunidades.findById(comunidadId)).isEmpty();
        assertThat(usuarios.findById(e.gestorUsuario().getId()).orElseThrow().getRol())
                .isEqualTo(Rol.SIN_ASIGNAR);
        assertThat(usuarios.findById(e.participanteUsuarios().getFirst().getId())).isPresent();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from actividad where comunidad_id = ?",
                                Long.class,
                                comunidadId))
                .isZero();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from envio_evidencia e join actividad a on"
                                    + " a.id=e.actividad_id where a.comunidad_id = ?",
                                Long.class,
                                comunidadId))
                .isZero();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from revision r join envio_evidencia e on"
                                    + " e.id=r.envio_id join actividad a on a.id=e.actividad_id"
                                    + " where a.comunidad_id = ?",
                                Long.class,
                                comunidadId))
                .isZero();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from recompensa_comunitaria where comunidad_id ="
                                    + " ?",
                                Long.class,
                                comunidadId))
                .isZero();
        SolicitudComunidad auditoria =
                solicitudesRepo
                        .findBySolicitanteIdOrderByCreadaEnDesc(e.gestorUsuario().getId())
                        .getFirst();
        assertThat(auditoria.getTipo()).isEqualTo(TipoSolicitudComunidad.ELIMINAR);
        assertThat(auditoria.getEstado()).isEqualTo(EstadoSolicitudComunidad.APROBADA);
        assertThat(auditoria.getMotivoSolicitud()).isEqualTo("La comunidad dejó de funcionar.");
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from archivo_borrado_pendiente where solicitud_id"
                                    + " = ?",
                                Long.class,
                                auditoria.getId()))
                .isZero();
    }

    @Test
    void restaurarReactivaSoloComunidadYGestor() {
        FabricaDatos.Escenario e = fabrica.escenario(1);
        servicioComunidades.archivarComunidad(e.gestorUsuario().getId());
        assertThat(ranking.puntosPorComunidad())
                .noneMatch(fila -> fila.comunidadId().equals(e.comunidad().getId()));
        servicioComunidades.restaurarComunidad(e.gestorUsuario().getId(), e.comunidad().getId());

        assertThat(comunidades.findById(e.comunidad().getId()).orElseThrow().isActiva()).isTrue();
        assertThat(
                        integrantes.findByComunidadIdAndUsuarioIdAndActivoTrue(
                                e.comunidad().getId(), e.gestorUsuario().getId()))
                .isPresent();
        assertThat(
                        integrantes.findByComunidadIdAndUsuarioIdAndActivoTrue(
                                e.comunidad().getId(), e.participanteUsuarios().getFirst().getId()))
                .isEmpty();
        assertThat(
                        usuarios.findById(e.participanteUsuarios().getFirst().getId())
                                .orElseThrow()
                                .getRol())
                .isEqualTo(Rol.SIN_ASIGNAR);
        assertThat(ranking.puntosPorComunidad())
                .anyMatch(fila -> fila.comunidadId().equals(e.comunidad().getId()));
    }

    @Test
    void rechazoDeEliminacionLiberaLaComunidadYConservaLaGestion() {
        FabricaDatos.Escenario e = fabrica.escenario(0);
        solicitudes.solicitarEliminacion(
                e.gestorUsuario().getId(), e.comunidad().getId(), "Cerramos temporalmente.");
        SolicitudComunidad solicitud =
                solicitudesRepo
                        .findBySolicitanteIdOrderByCreadaEnDesc(e.gestorUsuario().getId())
                        .getFirst();
        assertThat(
                        comunidades
                                .findById(e.comunidad().getId())
                                .orElseThrow()
                                .isEliminacionPendiente())
                .isTrue();
        assertThat(ranking.puntosPorComunidad())
                .noneMatch(fila -> fila.comunidadId().equals(e.comunidad().getId()));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> servicioComunidades.archivarComunidad(e.gestorUsuario().getId()))
                .isInstanceOf(ConflictoException.class);
        solicitudes.rechazar(
                e.revisor().getId(),
                solicitud.getId(),
                "La comunidad debe mantener su registro activo.");

        Comunidad actualizada = comunidades.findById(e.comunidad().getId()).orElseThrow();
        assertThat(actualizada.isActiva()).isTrue();
        assertThat(actualizada.isEliminacionPendiente()).isFalse();
        assertThat(usuarios.findById(e.gestorUsuario().getId()).orElseThrow().getRol())
                .isEqualTo(Rol.GESTOR_HUERTO);
        assertThat(solicitudesRepo.findById(solicitud.getId()).orElseThrow().getMotivoRechazo())
                .isEqualTo("La comunidad debe mantener su registro activo.");
    }

    @Test
    void bandejaMinisterialYHistorialDeCuentaRenderizanLasSolicitudes() throws Exception {
        int n = SECUENCIA.incrementAndGet();
        Usuario solicitante = cuentaSinComunidad(n);
        Usuario ministerio = fabrica.usuario("msp-vista-" + n, Rol.ADMIN_MINISTERIO_SALUD);
        solicitudes.solicitarCreacion(solicitante.getId(), alta("Comunidad para revisar " + n));

        mvc.perform(
                        get("/revision/solicitudes-comunidad")
                                .with(user(new UsuarioAutenticado(ministerio))))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Comunidad para revisar " + n)))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Calle de prueba 123")));
        mvc.perform(
                        get("/cuenta/solicitudes-comunidad")
                                .with(user(new UsuarioAutenticado(solicitante))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pendiente")));
    }

    private Usuario cuentaSinComunidad(int n) {
        return usuarios.save(
                new Usuario(
                        "solicitante" + n,
                        "Solicitante " + n,
                        "{noop}Clave-Prueba-21!",
                        Rol.SIN_ASIGNAR,
                        "solicitante" + n + "@example.org",
                        "+593991234567",
                        LocalDate.of(1995, 1, 1),
                        "PREFIERO_NO_DECIR",
                        "",
                        "APRENDIZ"));
    }

    private FormularioAltaComunidad alta(String nombre) {
        FormularioAltaComunidad f = new FormularioAltaComunidad();
        f.setNombreComunidad(nombre);
        f.setDescripcionComunidad("Comunidad de prueba");
        f.setTipoComunidad("BARRIO");
        f.setCanton("Ibarra");
        f.setDireccionComunidad("Calle de prueba 123");
        f.setNombre("Huerto de prueba");
        f.setDescripcion("Huerto medicinal de prueba");
        f.setProvincia("Imbabura");
        f.setParroquia("San Francisco");
        f.setDireccion("Calle de prueba 123");
        f.setAreaM2(new BigDecimal("12.5"));
        f.setDiseno("TIERRA_SURCOS");
        f.setTipoSuelo("LIMOSO");
        f.setFuenteAgua("Lluvia");
        f.setAbonoNatural("COMPOST");
        f.setCerramiento("RECICLADO_TABLAS");
        f.setLatitud(0.23);
        f.setLongitud(-78.26);
        return f;
    }
}
