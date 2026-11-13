package ec.edu.epn.huertos.comunidades.aplicacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.AutorizacionServicio;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.autenticacion.dominio.Usuario;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.comunidades.dominio.AsignacionRevisor;
import ec.edu.epn.huertos.comunidades.dominio.HistorialAsignacionRevisor;
import ec.edu.epn.huertos.comunidades.persistencia.AsignacionRevisorRepository;
import ec.edu.epn.huertos.comunidades.persistencia.HistorialAsignacionRevisorRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AdminTecnicoServicio {
    public record RevisorVista(Long id, String usuario, String nombre, String correo, boolean activo, long carga) { }
    private final UsuarioRepository usuarios;
    private final AsignacionRevisorRepository asignaciones;
    private final HistorialAsignacionRevisorRepository historial;
    private final AutorizacionServicio autorizacion;
    private final PasswordEncoder encoder;

    public AdminTecnicoServicio(UsuarioRepository usuarios, AsignacionRevisorRepository asignaciones,
                                HistorialAsignacionRevisorRepository historial, AutorizacionServicio autorizacion,
                                PasswordEncoder encoder) {
        this.usuarios = usuarios; this.asignaciones = asignaciones; this.historial = historial;
        this.autorizacion = autorizacion; this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<RevisorVista> revisores(Long actorId) {
        autorizacion.exigirRol(actorId, Rol.ADMIN_TECNICO);
        return usuarios.findByRolOrderByNombreCompleto(Rol.ADMIN_MINISTERIO_SALUD).stream()
                .map(u -> new RevisorVista(u.getId(), u.getNombreUsuario(), u.getNombreCompleto(), u.getCorreo(),
                        u.isActivo(), asignaciones.findByUsuarioId(u.getId()).size())).toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionTecnicaVista> comunidades(Long actorId) {
        autorizacion.exigirRol(actorId, Rol.ADMIN_TECNICO);
        return asignaciones.todasParaAdministrar();
    }

    @Transactional
    public void crearRevisor(Long actorId, String nombreUsuario, String nombre, String correo, String claveTemporal) {
        autorizacion.exigirRol(actorId, Rol.ADMIN_TECNICO);
        if (nombreUsuario == null || !nombreUsuario.matches("[A-Za-z0-9._-]{3,50}") || nombre == null || nombre.isBlank()
                || nombre.length() > 120 || correo == null || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || claveTemporal == null || claveTemporal.length() < 12 || claveTemporal.length() > 72) {
            throw new ReglaNegocioException("Revisa los datos. La contraseña temporal debe tener al menos 12 caracteres.");
        }
        String login = nombreUsuario.toLowerCase(java.util.Locale.ROOT);
        if (usuarios.findByNombreUsuarioIgnoreCase(login).isPresent()) throw new ConflictoException("Ese usuario ya existe.");
        usuarios.save(new Usuario(login, nombre.trim(), encoder.encode(claveTemporal), Rol.ADMIN_MINISTERIO_SALUD,
                correo.trim().toLowerCase(java.util.Locale.ROOT), null, null, null, null, null));
    }

    @Transactional
    public void reasignar(Long actorId, Long comunidadId, Long nuevoRevisorId, String motivo) {
        autorizacion.exigirRol(actorId, Rol.ADMIN_TECNICO);
        if (motivo == null || motivo.isBlank() || motivo.length() > 2000)
            throw new ReglaNegocioException("Escribe el motivo de la reasignación.");
        Usuario nuevo = usuarios.findById(nuevoRevisorId).filter(u -> u.isActivo() && u.getRol() == Rol.ADMIN_MINISTERIO_SALUD)
                .orElseThrow(() -> new ReglaNegocioException("Selecciona un administrador MSP activo."));
        List<AsignacionRevisor> actual = asignaciones.findByComunidadId(comunidadId);
        Long anterior = actual.isEmpty() ? null : actual.getFirst().getUsuarioId();
        if (nuevoRevisorId.equals(anterior)) throw new ConflictoException("La comunidad ya está asignada a esa cuenta.");
        if (actual.isEmpty()) asignaciones.save(new AsignacionRevisor(nuevo.getId(), comunidadId));
        else actual.getFirst().cambiarResponsable(nuevo.getId());
        historial.save(new HistorialAsignacionRevisor(comunidadId, anterior, nuevo.getId(), actorId, motivo.trim()));
    }
    public record RevisorSeleccion(Long id, String nombreCompleto) { }
    @Transactional(readOnly=true)
    public List<RevisorSeleccion> revisoresActivos(Long actorId) {
        autorizacion.exigirRol(actorId, Rol.ADMIN_TECNICO);
        return usuarios.findByRolOrderByNombreCompleto(Rol.ADMIN_MINISTERIO_SALUD).stream()
            .filter(Usuario::isActivo).map(u -> new RevisorSeleccion(u.getId(),u.getNombreCompleto())).toList();
    }
}
