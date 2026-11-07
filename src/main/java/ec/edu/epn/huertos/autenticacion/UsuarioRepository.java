package ec.edu.epn.huertos.autenticacion;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ec.edu.epn.huertos.comunidades.AsignacionRevisor;
import ec.edu.epn.huertos.comunidades.Comunidad;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("select u.id from Usuario u left join AsignacionRevisor a on a.usuarioId = u.id " +
            "where u.rol = ec.edu.epn.huertos.autenticacion.Rol.ADMIN_MINISTERIO_SALUD and u.activo = true " +
            "group by u.id order by count(a.id), u.id")
    java.util.List<Long> administradoresActivosPorCarga();

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> bloquearPorId(@Param("id") Long id);
}
