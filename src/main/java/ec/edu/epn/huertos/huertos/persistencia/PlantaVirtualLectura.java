package ec.edu.epn.huertos.huertos.persistencia;

import ec.edu.epn.huertos.huertos.aplicacion.PlantaPlantadaVista;
import ec.edu.epn.huertos.huertos.dominio.Planta;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Lectura propia del huerto virtual (no modifica {@link PlantaRepository}). Solo consulta; no
 * expone save/delete. Elegible = planta del huerto con {@code plantada = true} y {@code activa =
 * true}. No usa cantidad ni bodega. Nombre, nombre científico (para elegir la ilustración) y orden
 * salen de la especie del catálogo (una sola fuente de verdad), no de planta.nombre_comun. Orden
 * estable: nombre común de la especie ascendente y, a igual nombre, id de la planta ascendente.
 */
public interface PlantaVirtualLectura extends Repository<Planta, Long> {

    @Query(
            """
            select new ec.edu.epn.huertos.huertos.aplicacion.PlantaPlantadaVista(p.id, e.nombreComun, e.nombreCientifico, p.estadoRevision)
            from Planta p join Especie e on e.id = p.especieId
            where p.huertoId = :huertoId and p.plantada = true and p.activa = true
                and p.estadoRevision <> ec.edu.epn.huertos.evidencias.dominio.EstadoRevision.RECHAZADA
            order by e.nombreComun asc, p.id asc
            """)
    List<PlantaPlantadaVista> plantadasActivas(@Param("huertoId") Long huertoId);
}
