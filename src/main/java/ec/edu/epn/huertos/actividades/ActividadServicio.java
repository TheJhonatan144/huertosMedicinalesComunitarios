package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.Integrante;
import ec.edu.epn.huertos.comunidades.IntegranteRepository;
import ec.edu.epn.huertos.comunidades.IntegranteVista;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import ec.edu.epn.huertos.evidencias.AlmacenamientoArchivos;
import ec.edu.epn.huertos.evidencias.EnvioEvidencia;
import ec.edu.epn.huertos.evidencias.EnvioEvidenciaRepository;
import ec.edu.epn.huertos.evidencias.EstadoRevision;
import ec.edu.epn.huertos.evidencias.ImagenValidada;
import ec.edu.epn.huertos.evidencias.ValidadorImagen;
import ec.edu.epn.huertos.huertos.Huerto;
import ec.edu.epn.huertos.huertos.HuertoRepository;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Casos de uso del gestor sobre actividades: registrar, editar mientras está pendiente y reenviar tras un rechazo. */
@Service
public class ActividadServicio {

    private final ComunidadServicio comunidades;
    private final HuertoRepository huertos;
    private final ActividadRepository actividades;
    private final ActividadParticipanteRepository participantes;
    private final IntegranteRepository integrantes;
    private final EnvioEvidenciaRepository envios;
    private final ValidadorImagen validador;
    private final AlmacenamientoArchivos almacenamiento;

    public ActividadServicio(ComunidadServicio comunidades, HuertoRepository huertos, ActividadRepository actividades,
                             ActividadParticipanteRepository participantes, IntegranteRepository integrantes,
                             EnvioEvidenciaRepository envios, ValidadorImagen validador,
                             AlmacenamientoArchivos almacenamiento) {
        this.comunidades = comunidades;
        this.huertos = huertos;
        this.actividades = actividades;
        this.participantes = participantes;
        this.integrantes = integrantes;
        this.envios = envios;
        this.validador = validador;
        this.almacenamiento = almacenamiento;
    }

    @Transactional
    public Long registrar(Long gestorUsuarioId, FormularioActividad form, MultipartFile foto) {
        Integrante gestor = comunidades.gestorDe(gestorUsuarioId);
        Huerto huerto = huertos.findByComunidadId(gestor.getComunidadId())
                .orElseThrow(() -> new ReglaNegocioException("Tu comunidad aún no tiene un huerto registrado."));
        validarFecha(form.getFecha());
        List<Integrante> seleccion = validarParticipantes(gestor, form.getParticipanteIds());
        ImagenValidada imagen = validador.validar(foto);

        Actividad actividad = actividades.save(new Actividad(gestor.getComunidadId(), huerto.getId(), gestorUsuarioId,
                form.getTipo(), form.getDescripcion().trim(), form.getFecha()));
        for (Integrante i : seleccion) {
            participantes.save(new ActividadParticipante(actividad.getId(), i.getId(), gestor.getComunidadId()));
        }
        participantes.flush();
        actividad.fijarParticipantes();
        envios.save(new EnvioEvidencia(actividad.getId(), 1, guardarConLimpieza(imagen), imagen, null));
        return actividad.getId();
    }

    /** Edición permitida solo mientras la actividad está pendiente; nunca toca los participantes. */
    @Transactional
    public void editar(Long gestorUsuarioId, Long actividadId, FormularioActividad form) {
        Integrante gestor = comunidades.gestorDe(gestorUsuarioId);
        Actividad actividad = actividadDe(gestor, actividadId);
        if (actividad.getEstado() != EstadoRevision.PENDIENTE) {
            throw new ConflictoException("Solo se puede editar una actividad mientras está pendiente de revisión.");
        }
        validarFecha(form.getFecha());
        actividad.editar(form.getTipo(), form.getDescripcion().trim(), form.getFecha());
    }

    /** Reenvío de una actividad rechazada: crea un nuevo envío y conserva todos los anteriores. */
    @Transactional
    public void reenviar(Long gestorUsuarioId, Long actividadId, FormularioActividad form, String comentario,
                         MultipartFile foto) {
        Integrante gestor = comunidades.gestorDe(gestorUsuarioId);
        Actividad actividad = actividades.bloquearPorId(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La actividad no existe."));
        if (!actividad.getComunidadId().equals(gestor.getComunidadId())) {
            throw new AccessDeniedException("La actividad pertenece a otra comunidad");
        }
        if (actividad.getEstado() != EstadoRevision.RECHAZADA) {
            throw new ConflictoException("Solo se puede reenviar una actividad que fue rechazada.");
        }
        validarFecha(form.getFecha());
        ImagenValidada imagen = validador.validar(foto);
        int numero = envios.ultimoNumero(actividadId) + 1;
        actividad.editar(form.getTipo(), form.getDescripcion().trim(), form.getFecha());
        actividad.reabrirPorReenvio();
        envios.save(new EnvioEvidencia(actividadId, numero, guardarConLimpieza(imagen), imagen, comentario));
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
        if (unicos.size() > comunidades.cupoMaximo()) {
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
}
