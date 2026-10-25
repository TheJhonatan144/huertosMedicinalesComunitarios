package ec.edu.epn.huertos.evidencias;

import ec.edu.epn.huertos.actividades.Actividad;
import ec.edu.epn.huertos.actividades.ActividadRepository;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.IntegranteRepository;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Acceso controlado a las fotografías: solo el gestor de la comunidad y el revisor asignado. */
@Service
public class EvidenciaServicio {

    private final UsuarioRepository usuarios;
    private final EnvioEvidenciaRepository envios;
    private final ActividadRepository actividades;
    private final IntegranteRepository integrantes;
    private final ComunidadServicio comunidades;
    private final AlmacenamientoArchivos almacenamiento;

    public EvidenciaServicio(UsuarioRepository usuarios, EnvioEvidenciaRepository envios,
                             ActividadRepository actividades, IntegranteRepository integrantes,
                             ComunidadServicio comunidades, AlmacenamientoArchivos almacenamiento) {
        this.usuarios = usuarios;
        this.envios = envios;
        this.actividades = actividades;
        this.integrantes = integrantes;
        this.comunidades = comunidades;
        this.almacenamiento = almacenamiento;
    }

    public record FotoAutorizada(Resource recurso, String tipoContenido) {
    }

    @Transactional(readOnly = true)
    public FotoAutorizada cargarFoto(Long usuarioId, Long envioId) {
        Usuario usuario = usuarioId == null ? null : usuarios.findById(usuarioId).orElse(null);
        if (usuario == null || !usuario.isActivo()) {
            throw new AccessDeniedException("Sesión no válida");
        }
        EnvioEvidencia envio = envios.findById(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La fotografía no existe."));
        Actividad actividad = actividades.findById(envio.getActividadId()).orElseThrow();
        boolean permitido = switch (usuario.getRol()) {
            case GESTOR_HUERTO -> integrantes.findByUsuarioId(usuarioId)
                    .filter(i -> i.isEsGestor() && i.isActivo() && i.getComunidadId().equals(actividad.getComunidadId()))
                    .isPresent();
            case ADMIN_EVIDENCIAS -> comunidades.revisorAsignado(usuarioId, actividad.getComunidadId())
                    && !actividad.getRegistradaPor().equals(usuarioId);
            case PARTICIPANTE -> false;
        };
        if (!permitido) {
            throw new AccessDeniedException("Sin acceso a esta evidencia");
        }
        Resource recurso = almacenamiento.cargar(envio.getClaveAlmacenamiento());
        if (!recurso.exists()) {
            throw new RecursoNoEncontradoException("El archivo de la fotografía no está disponible.");
        }
        return new FotoAutorizada(recurso, envio.getTipoContenido());
    }
}
