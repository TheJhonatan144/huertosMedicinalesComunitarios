package ec.edu.epn.huertos.huertos;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Lectura propia del huerto virtual (no modifica {@link PlantaRepository}). Solo consulta; no expone save/delete.
 * Elegible = planta del huerto con {@code plantada = true} y {@code activa = true}. No usa cantidad ni bodega.
 * Orden estable: nombre común ascendente y, a igual nombre, id ascendente.
 */
public interface PlantaVirtualLectura extends Repository<Planta, Long> {

    @Query("""
            select new ec.edu.epn.huertos.huertos.PlantaPlantadaVista(p.id, p.nombreComun)
            from Planta p
            where p.huertoId = :huertoId and p.plantada = true and p.activa = true
            order by p.nombreComun asc, p.id asc
            """)
    List<PlantaPlantadaVista> plantadasActivas(@Param("huertoId") Long huertoId);
}
