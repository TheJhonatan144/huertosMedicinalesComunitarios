package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.AutorizacionServicio;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComunidadServicio {

    private final AutorizacionServicio autorizacion;
    private final ComunidadRepository comunidades;
    private final IntegranteRepository integrantes;
    private final AsignacionRevisorRepository asignaciones;
    private final int cupoMaximo;
    private final UsuarioRepository usuarios;
    private final InvitacionRepository invitaciones;
    private final SolicitudComunidadRepository solicitudes;

    public ComunidadServicio(AutorizacionServicio autorizacion, ComunidadRepository comunidades,
                             IntegranteRepository integrantes, AsignacionRevisorRepository asignaciones,
                             @Value("${app.comunidades.cupo-maximo}") int cupoMaximo,
                             UsuarioRepository usuarios, InvitacionRepository invitaciones,
                             SolicitudComunidadRepository solicitudes) {
        this.autorizacion = autorizacion;
        this.comunidades = comunidades;
        this.integrantes = integrantes;
        this.asignaciones = asignaciones;
        this.cupoMaximo = cupoMaximo;
        this.usuarios = usuarios;
        this.invitaciones = invitaciones;
        this.solicitudes = solicitudes;
    }

    /** Integrante gestor (activo) del usuario indicado; cualquier otro caso es acceso denegado. */
    @Transactional(readOnly = true)
    public Integrante gestorDe(Long usuarioId) {
        autorizacion.exigirRol(usuarioId, Rol.GESTOR_HUERTO);
        return integrantes.findByUsuarioIdAndActivoTrue(usuarioId)
                .filter(i -> i.isEsGestor() && i.isActivo())
                .orElseThrow(() -> new AccessDeniedException("El usuario no gestiona ninguna comunidad"));
    }

    @Transactional(readOnly = true)
    public Integrante participanteDe(Long usuarioId) {
        autorizacion.exigirRol(usuarioId, Rol.PARTICIPANTE);
        return integrantes.findByUsuarioIdAndActivoTrue(usuarioId)
                .or(() -> integrantes.findFirstByUsuarioIdOrderByAltaEnDesc(usuarioId))
                .orElseThrow(() -> new AccessDeniedException("El usuario no pertenece a ninguna comunidad"));
    }

    @Transactional(readOnly = true)
    public Comunidad comunidad(Long id) {
        return comunidades.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
    }

    @Transactional(readOnly = true)
    public List<IntegranteVista> integrantesDeMiComunidad(Long gestorUsuarioId) {
        return integrantes.vistaPorComunidad(gestorDe(gestorUsuarioId).getComunidadId());
    }

    @Transactional(readOnly = true)
    public long activos(Long comunidadId) {
        return integrantes.countByComunidadIdAndActivoTrue(comunidadId);
    }

    @Transactional(readOnly = true)
    public List<ComunidadArchivadaVista> archivadasDe(Long usuarioId) {
        return integrantes.archivadasPorUsuario(usuarioId);
    }

    public int cupoMaximo() {
        return cupoMaximo;
    }

    public int cupoMaximo(Long comunidadId) { return cupoMaximo(comunidad(comunidadId)); }

    public int cupoMaximo(Comunidad comunidad) {
        return switch (comunidad.getTipoComunidad()) {
            case "PERSONA" -> 1;
            case "FAMILIA" -> Math.min(10, cupoMaximo);
            case "CONDOMINIO" -> Math.min(20, cupoMaximo);
            case "BARRIO" -> cupoMaximo;
            default -> cupoMaximo;
        };
    }

    public long cuposDisponibles(Long comunidadId) {
        return Math.max(0, cupoMaximo(comunidadId) - activos(comunidadId));
    }

    public boolean existeNombre(String nombre) { return comunidades.existsByNombreIgnoreCase(nombre); }

    @Transactional
    public void darDeBaja(Long gestorUsuarioId, Long integranteId) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        exigirSinEliminacionPendiente(comunidades.bloquearPorId(gestor.getComunidadId()).orElseThrow());
        Integrante objetivo = integrantePropio(gestor, integranteId);
        if (objetivo.isEsGestor()) {
            throw new ReglaNegocioException("El gestor de la comunidad no puede darse de baja.");
        }
        objetivo.darDeBaja();
        usuarios.findById(objetivo.getUsuarioId()).ifPresent(u -> u.cambiarRol(Rol.SIN_ASIGNAR));
    }

    @Transactional
    public void invitarIntegrante(Long gestorUsuarioId, FormularioInvitacion f) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        Comunidad comunidad = comunidades.bloquearPorId(gestor.getComunidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
        if (!comunidad.isActiva()) throw new ConflictoException("La comunidad está archivada y no recibe invitaciones.");
        exigirSinEliminacionPendiente(comunidad);
        int capacidad = cupoMaximo(comunidad);
        if (integrantes.countByComunidadIdAndActivoTrue(gestor.getComunidadId()) >= capacidad)
            throw new ConflictoException("La comunidad alcanzó el límite de integrantes activos y no puede aceptar más invitaciones.");
        if (f.getNombreUsuario() == null || f.getNombreUsuario().isBlank())
            throw new ReglaNegocioException("Escribe el nombre de usuario de la persona invitada.");
        Usuario destino = usuarios.findByNombreUsuarioIgnoreCase(f.getNombreUsuario().trim())
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ReglaNegocioException("No existe una cuenta activa con ese nombre de usuario."));
        if (destino.getId().equals(gestorUsuarioId))
            throw new ReglaNegocioException("No puedes invitarte a tu propia comunidad.");
        if (destino.getRol() == Rol.ADMIN_MINISTERIO_SALUD)
            throw new ReglaNegocioException("Las cuentas institucionales del Ministerio no son integrantes comunitarios.");
        if (integrantes.findByComunidadIdAndUsuarioIdAndActivoTrue(gestor.getComunidadId(), destino.getId()).isPresent())
            throw new ConflictoException("Esa persona ya pertenece a esta comunidad.");
        if (invitaciones.existsByComunidadIdAndUsuarioDestinoIdAndTipoAndEstado(gestor.getComunidadId(), destino.getId(),
                TipoInvitacion.PARTICIPANTE, EstadoInvitacion.PENDIENTE))
            throw new ConflictoException("Ya existe una invitación pendiente para esa cuenta.");
        invitaciones.save(new InvitacionComunidad(gestor.getComunidadId(), gestorUsuarioId, destino.getId(), TipoInvitacion.PARTICIPANTE));
    }

    @Transactional
    public void invitarTraspasoGestor(Long gestorUsuarioId, Long integranteDestinoId) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        Comunidad comunidad = comunidades.bloquearPorId(gestor.getComunidadId()).orElseThrow();
        exigirSinEliminacionPendiente(comunidad);
        Integrante destino = integrantePropio(gestor, integranteDestinoId);
        if (!destino.isActivo() || destino.isEsGestor())
            throw new ReglaNegocioException("El sucesor debe ser un participante activo de tu comunidad.");
        if (invitaciones.existsByComunidadIdAndUsuarioDestinoIdAndTipoAndEstado(gestor.getComunidadId(), destino.getUsuarioId(),
                TipoInvitacion.TRASPASO_GESTOR, EstadoInvitacion.PENDIENTE))
            throw new ConflictoException("Ya existe una solicitud de traspaso pendiente para esa persona.");
        invitaciones.save(new InvitacionComunidad(gestor.getComunidadId(), gestorUsuarioId,
                destino.getUsuarioId(), TipoInvitacion.TRASPASO_GESTOR));
    }

    @Transactional(readOnly = true)
    public List<InvitacionVista> invitacionesRecibidas(Long usuarioId) {
        return invitaciones.findByUsuarioDestinoIdAndEstadoOrderByCreadaEnDesc(usuarioId, EstadoInvitacion.PENDIENTE)
                .stream().map(i -> new InvitacionVista(i.getId(), comunidad(i.getComunidadId()).getNombre(),
                        usuarios.findById(i.getInvitadaPor()).map(Usuario::getNombreCompleto).orElse("Gestor"), i.getTipo(), i.getCreadaEn())).toList();
    }

    @Transactional(readOnly = true)
    public List<InvitacionVista> invitacionesEnviadas(Long gestorUsuarioId) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        return invitaciones.findByInvitadaPorAndEstadoOrderByCreadaEnDesc(gestorUsuarioId, EstadoInvitacion.PENDIENTE)
                .stream().filter(i -> Objects.equals(i.getComunidadId(), gestor.getComunidadId()))
                .map(i -> new InvitacionVista(i.getId(), comunidad(i.getComunidadId()).getNombre(),
                        usuarios.findById(i.getUsuarioDestinoId()).map(Usuario::getNombreCompleto).orElse("Cuenta"), i.getTipo(), i.getCreadaEn())).toList();
    }

    @Transactional
    public void aceptarInvitacion(Long usuarioId, Long invitacionId) {
        InvitacionComunidad invitacion = invitaciones.findById(invitacionId)
                .filter(i -> i.getUsuarioDestinoId().equals(usuarioId) && i.getEstado() == EstadoInvitacion.PENDIENTE)
                .orElseThrow(() -> new AccessDeniedException("La invitación no está disponible para esta cuenta."));
        Comunidad comunidadBloqueada = comunidades.bloquearPorId(invitacion.getComunidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
        Comunidad comunidad = comunidad(invitacion.getComunidadId());
        if (!comunidad.isActiva()) throw new ConflictoException("La comunidad está archivada y no acepta invitaciones.");
        exigirSinEliminacionPendiente(comunidadBloqueada);
        Usuario usuarioBloqueado = usuarios.bloquearPorId(usuarioId)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
        if (solicitudes.existsBySolicitanteIdAndTipoAndEstado(usuarioId, TipoSolicitudComunidad.CREAR,
                EstadoSolicitudComunidad.PENDIENTE))
            throw new ConflictoException("Primero espera la decisión sobre tu solicitud de creación.");
        if (invitacion.getTipo() == TipoInvitacion.TRASPASO_GESTOR) {
            aceptarTraspaso(usuarioId, invitacion);
            return;
        }
        Usuario usuario = usuarioBloqueado;
        if (usuario.getRol() == Rol.ADMIN_MINISTERIO_SALUD || integrantes.findByUsuarioIdAndActivoTrue(usuarioId).isPresent())
            throw new ConflictoException("Primero debes dejar tu comunidad actual. La invitación seguirá pendiente.");
        int capacidad = cupoMaximo(comunidad);
        if (integrantes.countByComunidadIdAndActivoTrue(invitacion.getComunidadId()) >= capacidad)
            throw new ConflictoException("La comunidad ya alcanzó el límite de " + capacidad + " integrantes activos.");
        integrantes.save(new Integrante(invitacion.getComunidadId(), usuarioId, false, usuario.getFechaNacimiento(),
                usuario.getGenero(), usuario.getTalentosCertificados(),
                usuario.getNivelExperiencia() == null ? "APRENDIZ" : usuario.getNivelExperiencia()));
        usuario.cambiarRol(Rol.PARTICIPANTE);
        invitacion.responder(EstadoInvitacion.ACEPTADA);
    }

    private void aceptarTraspaso(Long nuevoGestorId, InvitacionComunidad invitacion) {
        Integrante gestorSaliente = integrantes.findByComunidadIdAndUsuarioIdAndActivoTrue(
                        invitacion.getComunidadId(), invitacion.getInvitadaPor())
                .filter(Integrante::isEsGestor)
                .orElseThrow(() -> new ConflictoException("El gestor que propuso el traspaso ya no administra esta comunidad."));
        Usuario nuevoGestor = usuarios.findById(nuevoGestorId).filter(u -> u.isActivo() && u.getRol() == Rol.PARTICIPANTE)
                .orElseThrow(() -> new ConflictoException("Solo un participante activo de la comunidad puede aceptar la gestión."));
        Integrante integranteNuevoGestor = integrantes.findByComunidadIdAndUsuarioIdAndActivoTrue(
                        invitacion.getComunidadId(), nuevoGestorId)
                .filter(i -> !i.isEsGestor())
                .orElseThrow(() -> new ConflictoException("Debes ser participante activo de esta comunidad para aceptar."));
        Usuario usuarioSaliente = usuarios.findById(gestorSaliente.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La cuenta del gestor ya no existe."));
        gestorSaliente.darDeBaja();
        integrantes.flush();
        integranteNuevoGestor.asumirGestion();
        nuevoGestor.cambiarRol(Rol.GESTOR_HUERTO);
        usuarioSaliente.cambiarRol(Rol.SIN_ASIGNAR);
        invitacion.responder(EstadoInvitacion.ACEPTADA);
        invitaciones.findByComunidadIdAndEstado(invitacion.getComunidadId(), EstadoInvitacion.PENDIENTE).stream()
                .filter(i -> i.getTipo() == TipoInvitacion.TRASPASO_GESTOR && !i.getId().equals(invitacion.getId()))
                .forEach(i -> i.responder(EstadoInvitacion.CANCELADA));
    }

    @Transactional
    public void rechazarInvitacion(Long usuarioId, Long invitacionId) {
        InvitacionComunidad invitacion = invitaciones.findById(invitacionId)
                .filter(i -> i.getUsuarioDestinoId().equals(usuarioId) && i.getEstado() == EstadoInvitacion.PENDIENTE)
                .orElseThrow(() -> new AccessDeniedException("La invitación no está disponible para esta cuenta."));
        invitacion.responder(EstadoInvitacion.RECHAZADA);
    }

    @Transactional
    public void cancelarInvitacion(Long gestorUsuarioId, Long invitacionId) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        exigirSinEliminacionPendiente(comunidades.bloquearPorId(gestor.getComunidadId()).orElseThrow());
        InvitacionComunidad i = invitaciones.findById(invitacionId)
                .filter(x -> x.getComunidadId().equals(gestor.getComunidadId()) && x.getEstado() == EstadoInvitacion.PENDIENTE)
                .orElseThrow(() -> new AccessDeniedException("La invitación no pertenece a tu comunidad."));
        i.responder(EstadoInvitacion.CANCELADA);
    }

    @Transactional
    public void dejarComunidad(Long usuarioId) {
        autorizacion.exigirRol(usuarioId, Rol.PARTICIPANTE);
        Integrante integrante = integrantes.findByUsuarioIdAndActivoTrue(usuarioId)
                .orElseThrow(() -> new AccessDeniedException("No perteneces a una comunidad activa."));
        if (integrante.isEsGestor())
            throw new ReglaNegocioException("Como gestor debes transferir la gestión o archivar la comunidad.");
        comunidades.bloquearPorId(integrante.getComunidadId());
        integrante.darDeBaja();
        usuarios.findById(usuarioId).orElseThrow().cambiarRol(Rol.SIN_ASIGNAR);
    }

    @Transactional
    public void archivarComunidad(Long gestorUsuarioId) {
        Integrante gestor = gestorDe(gestorUsuarioId);
        Comunidad comunidad = comunidades.bloquearPorId(gestor.getComunidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
        if (!comunidad.isActiva()) return;
        exigirSinEliminacionPendiente(comunidad);
        comunidad.archivar(gestorUsuarioId);
        for (Integrante integrante : integrantes.findByComunidadIdAndActivoTrue(comunidad.getId())) {
            integrante.darDeBaja();
            usuarios.findById(integrante.getUsuarioId()).ifPresent(u -> u.cambiarRol(Rol.SIN_ASIGNAR));
        }
        invitaciones.findByComunidadIdAndEstado(comunidad.getId(), EstadoInvitacion.PENDIENTE)
                .forEach(i -> i.responder(EstadoInvitacion.CANCELADA));
    }

    @Transactional
    public void restaurarComunidad(Long usuarioId, Long comunidadId) {
        Usuario usuario = usuarios.bloquearPorId(usuarioId).filter(Usuario::isActivo)
                .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
        Comunidad comunidad = comunidades.bloquearPorId(comunidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
        if (comunidad.isActiva()) throw new ConflictoException("La comunidad ya está activa.");
        if (comunidad.isEliminacionPendiente()) throw new ConflictoException("Hay una solicitud de eliminación pendiente.");
        if (!Objects.equals(comunidad.getArchivadaPor(), usuarioId))
            throw new AccessDeniedException("Solo quien archivó la comunidad puede restaurarla.");
        if (usuario.getRol() != Rol.SIN_ASIGNAR || integrantes.findByUsuarioIdAndActivoTrue(usuarioId).isPresent())
            throw new ConflictoException("Para restaurar, primero debes dejar tu comunidad activa actual.");
        comunidad.restaurar();
        integrantes.save(new Integrante(comunidadId, usuarioId, true, usuario.getFechaNacimiento(), usuario.getGenero(),
                usuario.getTalentosCertificados(), usuario.getNivelExperiencia() == null ? "APRENDIZ" : usuario.getNivelExperiencia()));
        usuario.cambiarRol(Rol.GESTOR_HUERTO);
    }

    @Transactional(readOnly = true)
    public void exigirComunidadOperativa(Long comunidadId) {
        Comunidad c = comunidad(comunidadId);
        if (!c.isActiva() || c.isEliminacionPendiente())
            throw new ConflictoException("La comunidad está archivada o tiene una eliminación pendiente.");
    }

    private void exigirSinEliminacionPendiente(Comunidad comunidad) {
        if (comunidad.isEliminacionPendiente())
            throw new ConflictoException("La comunidad está bloqueada mientras se revisa su solicitud de eliminación.");
    }

    @Transactional(readOnly = true)
    public boolean revisorAsignado(Long revisorUsuarioId, Long comunidadId) {
        return asignaciones.existsByUsuarioIdAndComunidadId(revisorUsuarioId, comunidadId);
    }

    @Transactional(readOnly = true)
    public List<Long> comunidadesAsignadas(Long revisorUsuarioId) {
        return asignaciones.findByUsuarioId(revisorUsuarioId).stream().map(AsignacionRevisor::getComunidadId).toList();
    }

    private Integrante integrantePropio(Integrante gestor, Long integranteId) {
        // Un integrante de otra comunidad se trata como acceso denegado, no como "no encontrado".
        return integrantes.findById(integranteId)
                .filter(i -> i.getComunidadId().equals(gestor.getComunidadId()))
                .orElseThrow(() -> new AccessDeniedException("El integrante no pertenece a tu comunidad"));
    }
}
