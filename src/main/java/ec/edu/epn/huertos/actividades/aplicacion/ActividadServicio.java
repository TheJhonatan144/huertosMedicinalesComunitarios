package ec.edu.epn.huertos.actividades.aplicacion;
import ec.edu.epn.huertos.actividades.dominio.Actividad;
import ec.edu.epn.huertos.actividades.dominio.ActividadParticipante;
import ec.edu.epn.huertos.actividades.persistencia.ActividadParticipanteRepository;
import ec.edu.epn.huertos.actividades.persistencia.ActividadRepository;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.compartido.dominio.Fechas;
import ec.edu.epn.huertos.compartido.dominio.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.aplicacion.IntegranteVista;
import ec.edu.epn.huertos.comunidades.dominio.Integrante;
import ec.edu.epn.huertos.comunidades.persistencia.IntegranteRepository;
import ec.edu.epn.huertos.evidencias.aplicacion.ValidadorImagen;
import ec.edu.epn.huertos.evidencias.aplicacion.ValidadorVideo;
import ec.edu.epn.huertos.evidencias.dominio.AlmacenamientoArchivos;
import ec.edu.epn.huertos.evidencias.dominio.EnvioEvidencia;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;
import ec.edu.epn.huertos.evidencias.dominio.ImagenValidada;
import ec.edu.epn.huertos.evidencias.dominio.VideoValidado;
import ec.edu.epn.huertos.evidencias.persistencia.EnvioEvidenciaRepository;
import ec.edu.epn.huertos.huertos.dominio.Huerto;
import ec.edu.epn.huertos.huertos.persistencia.HuertoRepository;
import ec.edu.epn.huertos.misiones.aplicacion.MisionServicio;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/** Casos de uso del gestor sobre actividades: registrar, editar mientras está pendiente y reenviar tras un rechazo. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class ActividadServicio {

    private final ComunidadServicio comunidades;
    private final HuertoRepository huertos;
    private final ActividadRepository actividades;
    private final ActividadParticipanteRepository participantes;
    private final IntegranteRepository integrantes;
    private final EnvioEvidenciaRepository envios;
    private final ValidadorImagen validador;
    private final ValidadorVideo validadorVideo;
    private final AlmacenamientoArchivos almacenamiento;
    private final MisionServicio misiones;

    public ActividadServicio(ComunidadServicio comunidades, HuertoRepository huertos, ActividadRepository actividades,
                             ActividadParticipanteRepository participantes, IntegranteRepository integrantes,
                             EnvioEvidenciaRepository envios, ValidadorImagen validador,
                             AlmacenamientoArchivos almacenamiento, ValidadorVideo validadorVideo,
                             MisionServicio misiones) {
        this.comunidades = comunidades;
        this.huertos = huertos;
        this.actividades = actividades;
        this.participantes = participantes;
        this.integrantes = integrantes;
        this.envios = envios;
        this.validador = validador;
        this.almacenamiento = almacenamiento;
        this.validadorVideo = validadorVideo;
        this.misiones = misiones;
    }

    @Transactional
    public Long registrar(Long gestorUsuarioId, FormularioActividad form, MultipartFile foto) {
        return registrar(gestorUsuarioId, form, foto, null);
    }

    /** Registra una actividad vinculada a una misión disponible del huerto. */
    @Transactional
    public Long registrar(Long gestorUsuarioId, FormularioActividad form, MultipartFile foto, Long misionHuertoId) {
        Integrante gestor = comunidades.gestorDe(gestorUsuarioId);
        comunidades.exigirComunidadOperativa(gestor.getComunidadId());
        Huerto huerto = huertos.findByComunidadId(gestor.getComunidadId())
                .orElseThrow(() -> new ReglaNegocioException("Tu comunidad aún no tiene un huerto registrado."));
        validarFecha(form.getFecha());
        if (misionHuertoId != null) misiones.exigirDisponible(misionHuertoId, huerto.getId(), form.getTipo());
        List<Integrante> seleccion = validarParticipantes(gestor, form.getParticipanteIds());
        Object evidencia = validarArchivo(foto);
        try {
        Actividad actividad = actividades.save(new Actividad(gestor.getComunidadId(), huerto.getId(), gestorUsuarioId,
                form.getTipo(), form.getDescripcion().trim(), form.getFecha(), misionHuertoId));
        if (misionHuertoId != null) misiones.vincularActividad(misionHuertoId, huerto.getId(), actividad.getId());
        for (Integrante i : seleccion) {
            participantes.save(new ActividadParticipante(actividad.getId(), i.getId(), gestor.getComunidadId()));
        }
        participantes.flush();
        actividad.fijarParticipantes();
        guardarEnvio(actividad.getId(), 1, evidencia, null);
        return actividad.getId();
        } finally { limpiarTemporal(evidencia); }
    }

    /** Edición permitida solo mientras la actividad está pendiente; nunca toca los participantes. */
    @Transactional
    public void editar(Long gestorUsuarioId, Long actividadId, FormularioActividad form) {
        Integrante gestor = comunidades.gestorDe(gestorUsuarioId);
        comunidades.exigirComunidadOperativa(gestor.getComunidadId());
        Actividad actividad = actividadDe(gestor, actividadId);
        if (actividad.getEstado() != EstadoRevision.PENDIENTE) {
            throw new ConflictoException("Solo se puede editar una actividad mientras está pendiente de revisión.");
        }
        validarFecha(form.getFecha());
        if (actividad.getMisionHuertoId() != null && form.getTipo() != actividad.getTipo())
            throw new ReglaNegocioException("No puedes cambiar el tipo de una actividad vinculada a una misión.");
        actividad.editar(form.getTipo(), form.getDescripcion().trim(), form.getFecha());
    }

    /** Reenvío de una actividad rechazada: crea un nuevo envío y conserva todos los anteriores. */
    @Transactional
    public void reenviar(Long gestorUsuarioId, Long actividadId, FormularioActividad form, String comentario,
                         MultipartFile foto) {
        Integrante gestor = comunidades.gestorDe(gestorUsuarioId);
        comunidades.exigirComunidadOperativa(gestor.getComunidadId());
        Actividad actividad = actividades.bloquearPorId(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La actividad no existe."));
        if (!actividad.getComunidadId().equals(gestor.getComunidadId())) {
            throw new AccessDeniedException("La actividad pertenece a otra comunidad");
        }
        if (actividad.getEstado() != EstadoRevision.RECHAZADA) {
            throw new ConflictoException("Solo se puede reenviar una actividad que fue rechazada.");
        }
        validarFecha(form.getFecha());
        if (actividad.getMisionHuertoId() != null && form.getTipo() != actividad.getTipo())
            throw new ReglaNegocioException("El tipo de actividad debe conservar el de la misión.");
        Object evidencia = validarArchivo(foto);
        try {
        int numero = envios.ultimoNumero(actividadId) + 1;
        actividad.editar(form.getTipo(), form.getDescripcion().trim(), form.getFecha());
        actividad.reabrirPorReenvio();
        misiones.reenviarActividad(actividadId);
        guardarEnvio(actividadId, numero, evidencia, comentario);
        } finally { limpiarTemporal(evidencia); }
    }

    @Transactional(readOnly = true)
    public List<ActividadResumen> listar(Long gestorUsuarioId) {
        Integrante gestor = comunidades.gestorDe(gestorUsuarioId);
        return actividades.findByComunidadIdOrderByCreadaEnDesc(gestor.getComunidadId()).stream()
                .map(a -> new ActividadResumen(a.getId(), a.getTipo().etiqueta(), Fechas.dia(a.getFechaActividad()),
                        a.getEstado(), (int) participantes.countByActividadId(a.getId()),
                        (int) envios.countByActividadId(a.getId())))
                .toList();
    }

    /** Integrantes activos que el gestor puede seleccionar (incluye al propio gestor). */
    @Transactional(readOnly = true)
    public List<IntegranteVista> participantesElegibles(Long gestorUsuarioId) {
        return comunidades.integrantesDeMiComunidad(gestorUsuarioId).stream().filter(IntegranteVista::activo).toList();
    }

    @Transactional(readOnly = true)
    public Actividad actividadDeGestor(Long gestorUsuarioId, Long actividadId) {
        return actividadDe(comunidades.gestorDe(gestorUsuarioId), actividadId);
    }

    private Actividad actividadDe(Integrante gestor, Long actividadId) {
        Actividad actividad = actividades.findById(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La actividad no existe."));
        if (!actividad.getComunidadId().equals(gestor.getComunidadId())) {
            throw new AccessDeniedException("La actividad pertenece a otra comunidad");
        }
        return actividad;
    }

    private List<Integrante> validarParticipantes(Integrante gestor, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ReglaNegocioException("Selecciona al menos un participante.");
        }
        LinkedHashSet<Long> unicos = new LinkedHashSet<>(ids);
        if (unicos.size() > comunidades.cupoMaximo(gestor.getComunidadId())) {
            throw new ReglaNegocioException("La lista de participantes excede el máximo de la comunidad.");
        }
        List<Integrante> encontrados = integrantes.findAllById(unicos);
        if (encontrados.size() != unicos.size()) {
            throw new ReglaNegocioException("Uno de los participantes seleccionados no existe.");
        }
        for (Integrante i : encontrados) {
            if (!i.getComunidadId().equals(gestor.getComunidadId())) {
                throw new AccessDeniedException("No se puede seleccionar a integrantes de otra comunidad");
            }
            if (!i.isActivo()) {
                throw new ReglaNegocioException("Solo se pueden seleccionar integrantes activos de la comunidad.");
            }
        }
        return encontrados;
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha.isAfter(Fechas.hoy())) {
            throw new ReglaNegocioException("La fecha de la actividad no puede ser futura.");
        }
    }

    /** Guarda el archivo y lo elimina si la transacción de base de datos no se confirma. */
    private String guardarConLimpieza(ImagenValidada imagen) {
        String clave = almacenamiento.guardar(imagen);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado != STATUS_COMMITTED) {
                    almacenamiento.eliminar(clave);
                }
            }
        });
        return clave;
    }

    private Object validarArchivo(MultipartFile archivo) {
        String nombre = archivo == null ? "" : archivo.getOriginalFilename();
        return nombre != null && nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".mp4")
                ? validadorVideo.validar(archivo) : validador.validar(archivo);
    }

    private void guardarEnvio(Long actividadId, int numero, Object evidencia, String comentario) {
        if (evidencia instanceof VideoValidado video) {
            envios.save(new EnvioEvidencia(actividadId, numero, guardarConLimpieza(video), video, comentario));
        } else {
            ImagenValidada imagen = (ImagenValidada) evidencia;
            envios.save(new EnvioEvidencia(actividadId, numero, guardarConLimpieza(imagen), imagen, comentario));
        }
    }

    private String guardarConLimpieza(VideoValidado video) {
        String clave = almacenamiento.guardar(video);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int estado) { if (estado != STATUS_COMMITTED) almacenamiento.eliminar(clave); }
        });
        return clave;
    }

    private void limpiarTemporal(Object evidencia) {
        if (evidencia instanceof VideoValidado video) {
            try { java.nio.file.Files.deleteIfExists(video.temporal()); } catch (java.io.IOException ignored) { }
        }
    }
}
