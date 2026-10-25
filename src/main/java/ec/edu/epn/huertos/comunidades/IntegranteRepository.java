package ec.edu.epn.huertos.comunidades;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntegranteRepository extends JpaRepository<Integrante, Long> {

    Optional<Integrante> findByUsuarioId(Long usuarioId);

    long countByComunidadIdAndActivoTrue(Long comunidadId);

    List<Integrante> findByComunidadIdAndActivoTrue(Long comunidadId);

    @Query("""
            select new ec.edu.epn.huertos.comunidades.IntegranteVista(i.id, u.nombreCompleto, u.nombreUsuario, i.esGestor, i.activo)
            from Integrante i join Usuario u on u.id = i.usuarioId
            where i.comunidadId = :comunidadId
            order by i.activo desc, i.esGestor desc, u.nombreCompleto
            """)
    List<IntegranteVista> vistaPorComunidad(@Param("comunidadId") Long comunidadId);
}
