package ec.edu.epn.huertos.autenticacion;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Principal de sesión: identificador, nombre visible y rol único del usuario. */
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String nombreUsuario;
    private final String nombreCompleto;
    private final String claveHash;
    private final Rol rol;
    private final boolean activo;

    public UsuarioAutenticado(Usuario usuario) {
        this.id = usuario.getId();
        this.nombreUsuario = usuario.getNombreUsuario();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.claveHash = usuario.getClaveHash();
        this.rol = usuario.getRol();
        this.activo = usuario.isActivo();
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.autoridad()));
    }

    @Override
    public String getPassword() {
        return claveHash;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
