package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.AutorizacionServicio;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import ec.edu.epn.huertos.evidencias.EliminacionComunidadServicio;
import java.util.List;
import java.util.Properties;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudComunidadServicio {
    private final SolicitudComunidadRepository solicitudes;
    private final UsuarioRepository usuarios;
    private final IntegranteRepository integrantes;
    private final ComunidadRepository comunidades;
    private final AltaComunidadServicio altas;
    private final AutorizacionServicio autorizacion;
    private final EliminacionComunidadServicio eliminaciones;

    public SolicitudComunidadServicio(SolicitudComunidadRepository solicitudes, UsuarioRepository usuarios,
            IntegranteRepository integrantes, ComunidadRepository comunidades, AltaComunidadServicio altas,
            AutorizacionServicio autorizacion,
            EliminacionComunidadServicio eliminaciones) {
        this.solicitudes = solicitudes; this.usuarios = usuarios; this.integrantes = integrantes;
        this.comunidades = comunidades; this.altas = altas;
        this.autorizacion = autorizacion; this.eliminaciones = eliminaciones;
    }

    @Transactional
    public void solicitarCreacion(Long usuarioId, FormularioAltaComunidad formulario) {
        altas.validarFormulario(formulario);
        Usuario usuario = usuarios.bloquearPorId(usuarioId).filter(Usuario::isActivo)
                .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
        if (usuario.getRol() != Rol.SIN_ASIGNAR || integrantes.findByUsuarioIdAndActivoTrue(usuarioId).isPresent())
            throw new ConflictoException("Solo puedes solicitar una huerta si no perteneces a una comunidad activa.");
        if (solicitudes.existsBySolicitanteIdAndTipoAndEstado(usuarioId, TipoSolicitudComunidad.CREAR,
                EstadoSolicitudComunidad.PENDIENTE))
            throw new ConflictoException("Ya tienes una solicitud de creación pendiente.");
        if (comunidades.existsByNombreIgnoreCase(formulario.getNombreComunidad().trim()))
            throw new ConflictoException("Ya existe una comunidad con ese nombre.");
        solicitudes.save(SolicitudComunidad.crear(usuarioId, formulario.getNombreComunidad().trim(), serializar(formulario)));
    }

    @Transactional
    public void solicitarEliminacion(Long usuarioId, Long comunidadId, String motivo) {
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Explica por qué solicitas eliminar la comunidad.");
        usuarios.findById(usuarioId).filter(Usuario::isActivo)
                .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
        Comunidad comunidad = comunidades.bloquearPorId(comunidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
        Integrante integrante = integrantes.findByUsuarioIdAndActivoTrue(usuarioId).orElse(null);
        boolean gestorActivo = integrante != null && integrante.getComunidadId().equals(comunidadId) && integrante.isEsGestor();
        boolean gestorArchivador = !comunidad.isActiva() && usuarioId.equals(comunidad.getArchivadaPor());
        if (!gestorActivo && !gestorArchivador)
            throw new AccessDeniedException("Solo el gestor actual o quien archivó la comunidad puede solicitar su eliminación.");
        if (comunidad.isEliminacionPendiente()) throw new ConflictoException("Ya existe una solicitud de eliminación pendiente.");
        if (solicitudes.existsByComunidadIdAndTipoAndEstado(comunidadId, TipoSolicitudComunidad.ELIMINAR,
                EstadoSolicitudComunidad.PENDIENTE)) throw new ConflictoException("Ya existe una solicitud de eliminación pendiente.");
        solicitudes.save(SolicitudComunidad.eliminar(usuarioId, comunidadId, comunidad.getNombre(), motivo.trim()));
        comunidad.marcarEliminacionPendiente(true);
    }

    @Transactional(readOnly = true)
    public List<SolicitudComunidadVista> pendientes() {
        return solicitudes.findByEstadoOrderByCreadaEnAsc(EstadoSolicitudComunidad.PENDIENTE).stream().map(this::vista).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudComunidadVista> deUsuario(Long usuarioId) {
        return solicitudes.findBySolicitanteIdOrderByCreadaEnDesc(usuarioId).stream().map(this::vista).toList();
    }

    @Transactional
    public void aprobar(Long revisorId, Long solicitudId) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_MINISTERIO_SALUD);
        SolicitudComunidad solicitud = solicitudes.bloquearPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La solicitud no existe."));
        if (solicitud.getEstado() != EstadoSolicitudComunidad.PENDIENTE) throw new ConflictoException("La solicitud ya fue resuelta.");
        if (solicitud.getTipo() == TipoSolicitudComunidad.CREAR) {
            Usuario usuario = usuarios.bloquearPorId(solicitud.getSolicitanteId()).filter(Usuario::isActivo)
                    .orElseThrow(() -> new ConflictoException("La cuenta solicitante ya no está activa."));
            if (usuario.getRol() != Rol.SIN_ASIGNAR || integrantes.findByUsuarioIdAndActivoTrue(usuario.getId()).isPresent())
                throw new ConflictoException("La persona ya pertenece a una comunidad activa; la solicitud no puede aprobarse.");
            if (comunidades.existsByNombreIgnoreCase(solicitud.getNombreComunidad()))
                throw new ConflictoException("Ya existe una comunidad con ese nombre; rechaza esta solicitud e indica el motivo.");
            FormularioAltaComunidad formulario = deserializar(solicitud.getDatosJson());
            altas.crearAprobada(usuario.getId(), formulario);
        } else {
            Comunidad comunidad = comunidades.bloquearPorId(solicitud.getComunidadId())
                    .orElseThrow(() -> new ConflictoException("La comunidad ya no existe."));
            if (!comunidad.isEliminacionPendiente()) throw new ConflictoException("La solicitud ya no está vigente.");
        }
        solicitud.aprobar(revisorId);
        if (solicitud.getTipo() == TipoSolicitudComunidad.CREAR) solicitud.limpiarDatosSolicitud();
        solicitudes.flush();
        if (solicitud.getTipo() == TipoSolicitudComunidad.ELIMINAR)
            eliminaciones.eliminarAprobada(solicitud.getId(), solicitud.getComunidadId());
    }

    @Transactional
    public void rechazar(Long revisorId, Long solicitudId, String motivo) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_MINISTERIO_SALUD);
        SolicitudComunidad solicitud = solicitudes.bloquearPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La solicitud no existe."));
        solicitud.rechazar(revisorId, motivo);
        if (solicitud.getTipo() == TipoSolicitudComunidad.CREAR) solicitud.limpiarDatosSolicitud();
        if (solicitud.getTipo() == TipoSolicitudComunidad.ELIMINAR) {
            comunidades.bloquearPorId(solicitud.getComunidadId()).ifPresent(c -> c.marcarEliminacionPendiente(false));
        }
    }

    private SolicitudComunidadVista vista(SolicitudComunidad solicitud) {
        String nombre = usuarios.findById(solicitud.getSolicitanteId()).map(Usuario::getNombreCompleto).orElse("Cuenta eliminada");
        String detalle = solicitud.getTipo() == TipoSolicitudComunidad.CREAR
                && solicitud.getEstado() == EstadoSolicitudComunidad.PENDIENTE ? detalleCreacion(solicitud.getDatosJson()) : null;
        return new SolicitudComunidadVista(solicitud.getId(), solicitud.getTipo(), solicitud.getEstado(),
                solicitud.getSolicitanteId(), nombre, solicitud.getComunidadId(), solicitud.getNombreComunidad(),
                solicitud.getMotivoSolicitud(), solicitud.getMotivoRechazo(), detalle, solicitud.getCreadaEn());
    }

    private String detalleCreacion(String datos) {
        FormularioAltaComunidad f = deserializar(datos);
        return "Tipo de comunidad: " + f.getTipoComunidad() + "\nDescripción: " + f.getDescripcionComunidad()
                + "\nUbicación: " + f.getCanton() + ", " + f.getProvincia()
                + "\nCoordenadas: " + f.getLatitud() + ", " + f.getLongitud()
                + "\nHuerto: " + f.getNombre() + " — " + f.getDescripcion()
                + "\nDirección del huerto: " + f.getDireccion() + ", " + f.getParroquia() + ", " + f.getProvincia()
                + "\nÁrea: " + f.getAreaM2() + " m² · Diseño: " + f.getDiseno() + " · Suelo: " + f.getTipoSuelo()
                + "\nAgua: " + f.getFuenteAgua() + " · Abono: " + f.getAbonoNatural() + " · Cerramiento: "
                + (f.getGruposCerramiento().isEmpty() ? "No especificado" : String.join(", ", f.getGruposCerramiento()));
    }

    private String serializar(FormularioAltaComunidad f) {
        Properties p = new Properties();
        p.setProperty("nombreComunidad", f.getNombreComunidad()); p.setProperty("descripcionComunidad", f.getDescripcionComunidad());
        p.setProperty("tipoComunidad", f.getTipoComunidad()); p.setProperty("canton", f.getCanton());
        p.setProperty("direccionComunidad", f.getDireccionComunidad()); p.setProperty("nombre", f.getNombre());
        p.setProperty("descripcion", f.getDescripcion()); p.setProperty("provincia", f.getProvincia());
        p.setProperty("parroquia", f.getParroquia()); p.setProperty("direccion", f.getDireccion());
        p.setProperty("areaM2", f.getAreaM2().toPlainString()); p.setProperty("diseno", f.getDiseno());
        p.setProperty("tipoSuelo", f.getTipoSuelo()); p.setProperty("fuenteAgua", f.getFuenteAgua());
        p.setProperty("abonoNatural", f.getAbonoNatural());
        if (f.getCerramiento() != null) p.setProperty("cerramiento", f.getCerramiento());
        p.setProperty("gruposCerramiento", String.join(",", f.getGruposCerramiento()));
        p.setProperty("disenos", String.join(",", f.getDisenos()));
        p.setProperty("latitud", Double.toString(f.getLatitud())); p.setProperty("longitud", Double.toString(f.getLongitud()));
        try { StringWriter writer = new StringWriter(); p.store(writer, null); return writer.toString(); }
        catch (IOException e) { throw new IllegalStateException("No se pudo guardar la solicitud de creación.", e); }
    }

    private FormularioAltaComunidad deserializar(String datos) {
        try {
            Properties p = new Properties(); p.load(new StringReader(datos));
            FormularioAltaComunidad f = new FormularioAltaComunidad();
            f.setNombreComunidad(p.getProperty("nombreComunidad")); f.setDescripcionComunidad(p.getProperty("descripcionComunidad"));
            f.setTipoComunidad(p.getProperty("tipoComunidad")); f.setCanton(p.getProperty("canton"));
            f.setDireccionComunidad(p.getProperty("direccionComunidad")); f.setNombre(p.getProperty("nombre"));
            f.setDescripcion(p.getProperty("descripcion")); f.setProvincia(p.getProperty("provincia"));
            f.setParroquia(p.getProperty("parroquia")); f.setDireccion(p.getProperty("direccion"));
            f.setAreaM2(new java.math.BigDecimal(requerida(p, "areaM2"))); f.setDiseno(requerida(p, "diseno"));
            f.setTipoSuelo(requerida(p, "tipoSuelo")); f.setFuenteAgua(requerida(p, "fuenteAgua"));
            f.setAbonoNatural(requerida(p, "abonoNatural"));
            String cerramientoAnterior = p.getProperty("cerramiento");
            f.setCerramiento(cerramientoAnterior);
            String gruposCerramiento = p.getProperty("gruposCerramiento");
            if (gruposCerramiento != null && !gruposCerramiento.isBlank())
                f.setGruposCerramiento(List.of(gruposCerramiento.split(",")));
            else if (cerramientoAnterior != null) {
                String grupoAnterior = grupoCerramientoAnterior(cerramientoAnterior);
                if (grupoAnterior != null) f.setGruposCerramiento(List.of(grupoAnterior));
            }
            String listaDisenos = p.getProperty("disenos");
            if (listaDisenos != null && !listaDisenos.isBlank()) f.setDisenos(List.of(listaDisenos.split(",")));
            f.setLatitud(Double.parseDouble(requerida(p, "latitud"))); f.setLongitud(Double.parseDouble(requerida(p, "longitud")));
            altas.validarFormulario(f);
            return f;
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Los datos de la solicitud no se pueden leer o no son válidos.", e);
        }
    }

    private String requerida(Properties p, String clave) {
        String valor = p.getProperty(clave);
        if (valor == null) throw new IllegalStateException("Falta el campo " + clave);
        return valor;
    }

    private String grupoCerramientoAnterior(String codigo) {
        if (codigo.equals("VIVO") || codigo.startsWith("VIVO_")) return "VIVA";
        if (codigo.equals("NATURAL") || codigo.startsWith("NATURAL_")) return "NATURAL";
        if (codigo.equals("RECICLADO") || codigo.startsWith("RECICLADO_")) return "RECICLADO";
        if (codigo.equals("METALICO") || codigo.startsWith("METALICO_")) return "METALICO";
        if (codigo.equals("CONVENCIONAL") || codigo.startsWith("CONVENCIONAL_")) return "CONVENCIONAL";
        if (codigo.equals("REUTILIZADO")) return "RECICLADO";
        if (codigo.equals("CONSTRUIDO")) return "CONVENCIONAL";
        return null;
    }
}
