package ec.edu.epn.huertos.huertos.aplicacion;

import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadVista;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.huertos.persistencia.EspecieRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Frontera de lectura web: resuelve relaciones y materializa records antes de salir de la
 * transacción.
 */
@Service
@Transactional(readOnly = true)
public class HuertosConsultaServicio {
    private final HuertoServicio huertos;
    private final EspecieRepository especies;

    public HuertosConsultaServicio(HuertoServicio huertos, EspecieRepository especies) {
        this.huertos = huertos;
        this.especies = especies;
    }

    public HuertoVista huertoDeGestor(Long u) {
        return HuertoVista.de(huertos.huertoDeGestor(u));
    }

    public ComunidadVista comunidadDeGestor(Long u) {
        return ComunidadVista.de(huertos.comunidadDeGestor(u));
    }

    public List<EspacioVista> espaciosDeGestor(Long u) {
        return huertos.espaciosDeGestor(u).stream().map(EspacioVista::de).toList();
    }

    public List<String> gruposCerramientoDeGestor(Long u) {
        return huertos.gruposCerramientoDeGestor(u);
    }

    public List<PlantaVista> plantasDeGestor(Long u) {
        return huertos.plantasDeGestor(u).stream().map(this::vista).toList();
    }

    public List<PlantaVista> plantasPlantadasDeGestor(Long u) {
        return huertos.plantasPlantadasDeGestor(u).stream().map(this::vista).toList();
    }

    public PlantaVista plantaDeGestor(Long u, Long id) {
        return huertos.plantaDeGestor(u, id);
    }

    public PlantaVista vista(Planta p) {
        return PlantaVista.de(
                p, especies.findById(p.getEspecieId()).map(EspecieVista::de).orElse(null));
    }
}
