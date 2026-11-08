package ec.edu.epn.huertos.autenticacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Provisión opcional de primer arranque. Configurar secretos por entorno y retirarlos tras usarlo. */
@Component
@Order(2)
public class ProvisionAdminTecnicoInicial implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String nombreUsuario;
    private final String nombreCompleto;
    private final String clave;

    public ProvisionAdminTecnicoInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${app.admin-tecnico.usuario:}") String nombreUsuario,
            @Value("${app.admin-tecnico.nombre:}") String nombreCompleto,
            @Value("${app.admin-tecnico.clave:}") String clave) {
        this.usuarios = usuarios; this.encoder = encoder;
        this.nombreUsuario = nombreUsuario; this.nombreCompleto = nombreCompleto; this.clave = clave;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (nombreUsuario.isBlank() && nombreCompleto.isBlank() && clave.isBlank()) return;
        if (nombreUsuario.isBlank() || nombreCompleto.isBlank() || clave.length() < 12)
            throw new IllegalStateException("Para provisionar ADMIN_TECNICO define usuario, nombre y una clave de al menos 12 caracteres.");
        if (usuarios.findByRolOrderByNombreCompleto(Rol.ADMIN_TECNICO).stream().findAny().isPresent()) return;
        if (usuarios.findByNombreUsuarioIgnoreCase(nombreUsuario).isPresent())
            throw new IllegalStateException("El usuario configurado para ADMIN_TECNICO ya existe con otro rol.");
        usuarios.save(new Usuario(nombreUsuario.toLowerCase(java.util.Locale.ROOT), nombreCompleto,
                encoder.encode(clave), Rol.ADMIN_TECNICO));
    }
}
