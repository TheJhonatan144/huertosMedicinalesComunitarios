package ec.edu.epn.huertos.autenticacion.aplicacion;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.autenticacion.dominio.Usuario;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Autorización por rol en la capa de servicios: verifica contra la base de datos, no contra la sesión,
 * de modo que no depende de que la petición llegue por la interfaz web.
 */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AutorizacionServicio {

    private final UsuarioRepository usuarios;

    public AutorizacionServicio(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario exigirRol(Long usuarioId, Rol rol) {
        return exigirAlgunRol(usuarioId, rol);
    }

    public Usuario exigirAlgunRol(Long usuarioId, Rol... roles) {
        Usuario usuario = usuarioId == null ? null : usuarios.findById(usuarioId).orElse(null);
        if (usuario == null || !usuario.isActivo() || !java.util.Arrays.asList(roles).contains(usuario.getRol())) {
            throw new AccessDeniedException("Tu cuenta no tiene permiso para esta acción.");
        }
        return usuario;
    }
}
