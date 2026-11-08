package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma la vista del huerto virtual con datos reales y SOLO los lee: suelo y diseño del huerto, plantas plantadas y
 * activas (se dibujan hasta dos; «plantada» no se limita) y el progreso que calcula {@link GamificacionServicio}. No otorga puntos ni recalcula nada.
 */
@Service
public class HuertoVirtualServicio {

    /** Tope SOLO del dibujo: se dibujan como máximo dos plantas; guardar «plantada» no tiene límite. */
    static final int MAXIMO_PLANTAS_VISIBLES = 2;

    private final HuertoServicio huertos;
    private final HuertoRepository huertoRepository;
    private final PlantaVirtualLectura plantas;
    private final GamificacionServicio gamificacion;

    public HuertoVirtualServicio(HuertoServicio huertos, HuertoRepository huertoRepository,
                                 PlantaVirtualLectura plantas, GamificacionServicio gamificacion) {
        this.huertos = huertos;
        this.huertoRepository = huertoRepository;
        this.plantas = plantas;
        this.gamificacion = gamificacion;
    }

    /**
     * Panel del gestor. La comunidad se deduce del usuario autenticado mediante {@link HuertoServicio#huertoDeGestor}
     * (exige rol de gestor activo), así que nunca se dibuja el huerto de otra comunidad.
     */
    @Transactional(readOnly = true)
    public HuertoVirtual paraGestor(Long gestorUsuarioId) {
        Huerto huerto = huertos.huertoDeGestor(gestorUsuarioId);
        boolean puedeRegistrar = !huertos.comunidadDeGestor(gestorUsuarioId).isEliminacionPendiente();
        return construir(huerto, puedeRegistrar);
    }

    /**
     * Pantallas de solo lectura (ficha pública, panel del participante). NO autoriza: el llamador debe obtener
     * {@code comunidadId} de una fuente ya autorizada. Vacío si la comunidad aún no tiene huerto.
     */
    @Transactional(readOnly = true)
    public Optional<HuertoVirtual> paraComunidad(Long comunidadId) {
        return huertoRepository.findByComunidadId(comunidadId).map(h -> construir(h, false));
    }

    private HuertoVirtual construir(Huerto huerto, boolean puedeRegistrar) {
        List<PlantaPlantadaVista> elegibles = plantas.plantadasActivas(huerto.getId());
        List<HuertoVirtual.PlantaVirtual> visibles = elegibles.stream().limit(MAXIMO_PLANTAS_VISIBLES)
                .map(p -> new HuertoVirtual.PlantaVirtual(p.nombreComun(), SpritePlanta.paraNombre(p.nombreComun())))
                .toList();
        return new HuertoVirtual(huerto.getNombre(), huerto.getTipoSuelo(), huerto.getDiseno(), visibles,
                elegibles.size(), gamificacion.progreso(huerto.getComunidadId()), puedeRegistrar);
    }
}
