package ec.edu.epn.huertos.autenticacion;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Autorización por rol en la capa de servicios: verifica contra la base de datos, no contra la sesión,
 * de modo que no depende de que la petición llegue por la interfaz web.
 */
@Service
public class AutorizacionServicio {

    private final UsuarioRepository usuarios;

    public AutorizacionServicio(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario exigirRol(Long usuarioId, Rol rol) {
        Usuario usuario = usuarioId == null ? null : usuarios.findById(usuarioId).orElse(null);
        if (usuario == null || !usuario.isActivo() || usuario.getRol() != rol) {
            throw new AccessDeniedException("Se requiere el rol " + rol.etiqueta());
        }
        return usuario;
    }
}
