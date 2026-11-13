package ec.edu.epn.huertos.comunidades.persistencia;
import ec.edu.epn.huertos.comunidades.aplicacion.AsignacionTecnicaVista;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadAsignadaVista;
import ec.edu.epn.huertos.comunidades.dominio.AsignacionRevisor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignacionRevisorRepository extends JpaRepository<AsignacionRevisor, Long> {

    boolean existsByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    List<AsignacionRevisor> findByUsuarioId(Long usuarioId);

    List<AsignacionRevisor> findByComunidadId(Long comunidadId);

    @org.springframework.data.jpa.repository.Query("""
            select new ec.edu.epn.huertos.comunidades.aplicacion.AsignacionTecnicaVista(c.id, c.nombre,
                case when c.activa = true then 'Activa' else 'Archivada' end,
                a.usuarioId, u.nombreCompleto, c.provincia, c.canton)
            from Comunidad c left join AsignacionRevisor a on a.comunidadId = c.id
            left join Usuario u on u.id = a.usuarioId order by c.nombre
            """)
    List<AsignacionTecnicaVista> todasParaAdministrar();

    @org.springframework.data.jpa.repository.Query("""
            select new ec.edu.epn.huertos.comunidades.aplicacion.ComunidadAsignadaVista(c.id, c.nombre,
                case when c.activa = true then 'Activa' else 'Archivada' end,
                c.tipoComunidad, c.provincia, c.canton, h.nombre)
            from AsignacionRevisor a join Comunidad c on c.id = a.comunidadId
            left join Huerto h on h.comunidadId = c.id
            where a.usuarioId = :usuarioId order by c.nombre
            """)
    List<ComunidadAsignadaVista> comunidadesVista(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);
}
