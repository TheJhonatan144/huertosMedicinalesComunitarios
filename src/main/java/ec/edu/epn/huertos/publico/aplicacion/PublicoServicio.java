package ec.edu.epn.huertos.publico.aplicacion;

import ec.edu.epn.huertos.compartido.dominio.RecursoNoEncontradoException;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.dominio.Comunidad;
import ec.edu.epn.huertos.comunidades.persistencia.ComunidadRepository;
import ec.edu.epn.huertos.gamificacion.aplicacion.FilaRanking;
import ec.edu.epn.huertos.gamificacion.aplicacion.GamificacionServicio;
import ec.edu.epn.huertos.gamificacion.aplicacion.ProgresoHuerto;
import ec.edu.epn.huertos.huertos.aplicacion.EspacioHuertoServicio;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoServicio;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoVirtual;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoVirtualServicio;
import ec.edu.epn.huertos.huertos.dominio.EspacioHuerto;
import ec.edu.epn.huertos.huertos.dominio.Huerto;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.huertos.persistencia.HuertoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas públicas (visitante): sin datos personales ni evidencias fotográficas. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class PublicoServicio {

    private final ComunidadRepository comunidades;
    private final ComunidadServicio comunidadServicio;
    private final HuertoRepository huertos;
    private final HuertoServicio huertoServicio;
    private final GamificacionServicio gamificacion;
    private final EspacioHuertoServicio espacios;
    private final HuertoVirtualServicio virtual;
    private final ec.edu.epn.huertos.huertos.aplicacion.HuertosConsultaServicio vistas;

    public PublicoServicio(ComunidadRepository comunidades, ComunidadServicio comunidadServicio,
                           HuertoRepository huertos, HuertoServicio huertoServicio, GamificacionServicio gamificacion,
                           EspacioHuertoServicio espacios, HuertoVirtualServicio virtual,
                           ec.edu.epn.huertos.huertos.aplicacion.HuertosConsultaServicio vistas) {
        this.comunidades = comunidades;
        this.comunidadServicio = comunidadServicio;
        this.huertos = huertos;
        this.huertoServicio = huertoServicio;
        this.gamificacion = gamificacion;
        this.espacios = espacios;
        this.virtual = virtual; this.vistas = vistas;
    }

    /** Elemento del mapa y de su alternativa en lista. */
    public record HuertoMapa(Long comunidadId, String comunidad, String huerto, String provincia, String canton,
                             double latitud, double longitud, long puntosComunitarios, String etapa, int porcentaje) {
    }

    public record Ficha(ec.edu.epn.huertos.comunidades.aplicacion.ComunidadVista comunidad, ec.edu.epn.huertos.huertos.aplicacion.HuertoVista huerto, List<ec.edu.epn.huertos.huertos.aplicacion.EspacioVista> espacios, List<ec.edu.epn.huertos.huertos.aplicacion.PlantaVista> plantas, long integrantesActivos,
                        long puntosComunitarios, ProgresoHuerto progreso, Integer posicionRanking,
                        HuertoVirtual huertoVirtual) {
    }

    @Transactional(readOnly = true)
    public List<HuertoMapa> huertosParaMapa() {
        return comunidades.findByActivaTrueAndEliminacionPendienteFalseOrderByNombreAsc().stream().map(c -> {
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
        List<EspacioHuerto> zonas = huerto == null ? List.of() : espacios.activos(huerto.getId());
        Integer posicion = gamificacion.ranking().stream().filter(f -> f.comunidadId().equals(comunidadId))
                .map(FilaRanking::posicion).findFirst().orElse(null);
        return new Ficha(ec.edu.epn.huertos.comunidades.aplicacion.ComunidadVista.de(comunidad),
                ec.edu.epn.huertos.huertos.aplicacion.HuertoVista.de(huerto),
                zonas.stream().map(ec.edu.epn.huertos.huertos.aplicacion.EspacioVista::de).toList(),
                plantas.stream().map(vistas::vista).toList(), comunidadServicio.activos(comunidadId),
                gamificacion.puntosComunitarios(comunidadId), gamificacion.progreso(comunidadId), posicion,
                comunidad.isActiva() && !comunidad.isEliminacionPendiente()
                        ? virtual.paraComunidad(comunidadId).orElse(null) : null);
    }

    @Transactional(readOnly = true)
    public List<FilaRanking> ranking() {
        return gamificacion.ranking();
    }
}
