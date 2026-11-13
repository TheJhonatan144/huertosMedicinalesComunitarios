package ec.edu.epn.huertos.autenticacion.aplicacion;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.autenticacion.dominio.Usuario;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class RegistroCuentaServicio {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    public RegistroCuentaServicio(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios; this.encoder = encoder;
    }

    @Transactional
    public void registrar(FormularioRegistroCuenta f) {
        if (f.getNombreCompleto() == null || f.getNombreCompleto().isBlank()
                || f.getNombreUsuario() == null || !f.getNombreUsuario().matches("[A-Za-z0-9._-]{3,50}")
                || f.getNombreCompleto().trim().length() > 120
                || f.getCorreo() == null || f.getCorreo().length() > 254 || !f.getCorreo().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || f.getTelefono() == null || !f.getTelefono().matches("[+0-9() -]{7,30}")
                || f.getTelefono().chars().filter(Character::isDigit).count() < 7
                || f.getFechaNacimiento() == null || f.getFechaNacimiento().isAfter(LocalDate.now())
                || f.getGenero() == null || !List.of("FEMENINO", "MASCULINO", "NO_BINARIO", "OTRO", "PREFIERO_NO_DECIR").contains(f.getGenero())
                || f.getNivelExperiencia() == null || !List.of("APRENDIZ", "OFICIAL", "MAESTRO").contains(f.getNivelExperiencia())
                || f.getTalentosCertificados() != null && f.getTalentosCertificados().length() > 2000
                || f.getClave() == null || f.getClave().length() < 8 || f.getClave().length() > 72
                || f.getClave().getBytes(StandardCharsets.UTF_8).length > 72
                || !f.getClave().equals(f.getConfirmarClave())) {
            throw new IllegalArgumentException("Revisa los campos, el correo, la fecha y la contraseña (8 caracteres mínimo, sin superar 72 bytes) con su confirmación.");
        }
        String usuario = f.getNombreUsuario().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarios.findByNombreUsuarioIgnoreCase(usuario).isPresent())
            throw new IllegalArgumentException("Ese nombre de usuario ya está registrado.");
        usuarios.save(new Usuario(usuario, f.getNombreCompleto().trim(), encoder.encode(f.getClave()), Rol.SIN_ASIGNAR,
                f.getCorreo().trim().toLowerCase(java.util.Locale.ROOT), f.getTelefono().trim(), f.getFechaNacimiento(), f.getGenero(),
                f.getTalentosCertificados() == null ? "" : f.getTalentosCertificados().trim(), f.getNivelExperiencia()));
    }
}
