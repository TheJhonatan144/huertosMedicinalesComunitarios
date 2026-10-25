package ec.edu.epn.huertos.publico;

import ec.edu.epn.huertos.comunidades.Comunidad;
import ec.edu.epn.huertos.comunidades.ComunidadRepository;
import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import ec.edu.epn.huertos.gamificacion.FilaRanking;
import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import ec.edu.epn.huertos.gamificacion.ProgresoHuerto;
import ec.edu.epn.huertos.huertos.Huerto;
import ec.edu.epn.huertos.huertos.HuertoRepository;
import ec.edu.epn.huertos.huertos.HuertoServicio;
import ec.edu.epn.huertos.huertos.Planta;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas públicas (visitante): sin datos personales ni evidencias fotográficas. */
@Service
public class PublicoServicio {

    private final ComunidadRepository comunidades;
    private final ComunidadServicio comunidadServicio;
    private final HuertoRepository huertos;
    private final HuertoServicio huertoServicio;
    private final GamificacionServicio gamificacion;

    public PublicoServicio(ComunidadRepository comunidades, ComunidadServicio comunidadServicio,
                           HuertoRepository huertos, HuertoServicio huertoServicio, GamificacionServicio gamificacion) {
        this.comunidades = comunidades;
        this.comunidadServicio = comunidadServicio;
        this.huertos = huertos;
        this.huertoServicio = huertoServicio;
        this.gamificacion = gamificacion;
    }

    /** Elemento del mapa y de su alternativa en lista. */
    public record HuertoMapa(Long comunidadId, String comunidad, String huerto, String provincia, String canton,
                             double latitud, double longitud, long puntosComunitarios, String etapa, int porcentaje) {
    }

    public record Ficha(Comunidad comunidad, Huerto huerto, List<Planta> plantas, long integrantesActivos,
                        long puntosComunitarios, ProgresoHuerto progreso, Integer posicionRanking) {
    }

    @Transactional(readOnly = true)
    public List<HuertoMapa> huertosParaMapa() {
        return comunidades.findAll(Sort.by("nombre")).stream().map(c -> {
            Huerto h = huertos.findByComunidadId(c.getId()).orElse(null);
            ProgresoHuerto p = gamificacion.progreso(c.getId());
            return new HuertoMapa(c.getId(), c.getNombre(), h == null ? null : h.getNombre(), c.getProvincia(),
                    c.getCanton(), h == null ? c.getLatitud() : h.getLatitud(),
                    h == null ? c.getLongitud() : h.getLongitud(), gamificacion.puntosComunitarios(c.getId()),
                    p.etapa(), p.porcentaje());
        }).toList();
    }

    @Transactional(readOnly = true)
    public Ficha ficha(Long comunidadId) {
        Comunidad comunidad = comunidades.findById(comunidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no existe."));
        Huerto huerto = huertos.findByComunidadId(comunidadId).orElse(null);
        List<Planta> plantas = huerto == null ? List.of() : huertoServicio.plantasActivasPublicas(huerto.getId());
        Integer posicion = gamificacion.ranking().stream().filter(f -> f.comunidadId().equals(comunidadId))
                .map(FilaRanking::posicion).findFirst().orElse(null);
        return new Ficha(comunidad, huerto, plantas, comunidadServicio.activos(comunidadId),
                gamificacion.puntosComunitarios(comunidadId), gamificacion.progreso(comunidadId), posicion);
    }

    @Transactional(readOnly = true)
    public List<FilaRanking> ranking() {
        return gamificacion.ranking();
    }
}
