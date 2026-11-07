package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma la vista del huerto virtual con datos reales: suelo y diseño del huerto, plantas registradas como plantadas
 * (activas y con al menos una unidad; las existencias de bodega no cuentan) y el progreso, que proviene solo de
 * actividades aprobadas. NO autoriza: el llamador decide para qué comunidad la pide (la información es pública).
 */
@Service
public class HuertoVirtualServicio {

    /** Primera versión: se representan una o dos plantas. */
    static final int MAXIMO_PLANTAS_VISIBLES = 2;

    private final HuertoRepository huertos;
    private final PlantaRepository plantas;
    private final GamificacionServicio gamificacion;

    public HuertoVirtualServicio(HuertoRepository huertos, PlantaRepository plantas, GamificacionServicio gamificacion) {
        this.huertos = huertos;
        this.plantas = plantas;
        this.gamificacion = gamificacion;
    }

    /** Vacío si la comunidad aún no tiene huerto. */
    @Transactional(readOnly = true)
    public Optional<HuertoVirtual> paraComunidad(Long comunidadId, boolean puedeRegistrar) {
        return huertos.findByComunidadId(comunidadId).map(h -> construir(h, comunidadId, puedeRegistrar));
    }

    private HuertoVirtual construir(Huerto huerto, Long comunidadId, boolean puedeRegistrar) {
        List<Planta> plantadas = plantas.findByHuertoIdAndActivaTrueAndCantidadGreaterThanOrderByNombreComunAsc(huerto.getId(), 0);
        List<HuertoVirtual.PlantaVirtual> visibles = plantadas.stream().limit(MAXIMO_PLANTAS_VISIBLES)
                .map(p -> new HuertoVirtual.PlantaVirtual(p.getNombreComun(), SpritePlanta.paraNombre(p.getNombreComun())))
                .toList();
        List<String> faltantes = new ArrayList<>();
        if (huerto.getTipoSuelo() == null) {
            faltantes.add("tipo de suelo");
        }
        if (huerto.getDiseno() == null) {
            faltantes.add("diseño del huerto");
        }
        return new HuertoVirtual(huerto.getNombre(), huerto.getTipoSuelo(), huerto.getDiseno(), visibles,
                plantadas.size() - visibles.size(), gamificacion.progreso(comunidadId), faltantes, puedeRegistrar);
    }
}
