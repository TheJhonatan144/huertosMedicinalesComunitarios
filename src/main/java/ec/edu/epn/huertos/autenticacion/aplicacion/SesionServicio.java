package ec.edu.epn.huertos.autenticacion.aplicacion;

import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Recarga la identidad de sesión desde aplicación; la presentación no consulta repositorios. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class SesionServicio {
    private final UsuarioRepository usuarios;

    public SesionServicio(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public UsuarioAutenticado principalActual(Long usuarioId) {
        return new UsuarioAutenticado(usuarios.findById(usuarioId).orElseThrow());
    }
}
