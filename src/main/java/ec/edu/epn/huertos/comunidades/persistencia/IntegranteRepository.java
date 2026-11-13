package ec.edu.epn.huertos.comunidades.persistencia;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadArchivadaVista;
import ec.edu.epn.huertos.comunidades.aplicacion.IntegranteVista;
import ec.edu.epn.huertos.comunidades.dominio.Integrante;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntegranteRepository extends JpaRepository<Integrante, Long> {

    Optional<Integrante> findByUsuarioIdAndActivoTrue(Long usuarioId);

    Optional<Integrante> findFirstByUsuarioIdOrderByAltaEnDesc(Long usuarioId);

    List<Integrante> findByUsuarioIdOrderByAltaEnDesc(Long usuarioId);

    Optional<Integrante> findByComunidadIdAndUsuarioIdAndActivoTrue(Long comunidadId, Long usuarioId);

    long countByComunidadIdAndActivoTrue(Long comunidadId);

    List<Integrante> findByComunidadIdAndActivoTrue(Long comunidadId);

    @Query("""
            select distinct new ec.edu.epn.huertos.comunidades.aplicacion.ComunidadArchivadaVista(c.id, c.nombre, h.nombre, c.provincia, c.canton, c.archivadaPor, c.eliminacionPendiente)
            from Integrante i join Comunidad c on c.id = i.comunidadId
            left join Huerto h on h.comunidadId = c.id
            where i.usuarioId = :usuarioId and c.activa = false
            order by c.nombre
            """)
    List<ComunidadArchivadaVista> archivadasPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
            select new ec.edu.epn.huertos.comunidades.aplicacion.IntegranteVista(i.id, u.nombreCompleto, u.nombreUsuario, i.esGestor, i.activo)
            from Integrante i join Usuario u on u.id = i.usuarioId
            where i.comunidadId = :comunidadId
            order by i.activo desc, i.esGestor desc, u.nombreCompleto
            """)
    List<IntegranteVista> vistaPorComunidad(@Param("comunidadId") Long comunidadId);
}
