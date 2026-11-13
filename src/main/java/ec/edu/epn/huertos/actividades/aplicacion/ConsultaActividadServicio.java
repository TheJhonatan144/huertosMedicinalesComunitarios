package ec.edu.epn.huertos.actividades.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConsultaActividadServicio {
    private final ActividadServicio actividades;
    private final DetalleActividadServicio detalles;

    public ConsultaActividadServicio(
            ActividadServicio actividades, DetalleActividadServicio detalles) {
        this.actividades = actividades;
        this.detalles = detalles;
    }

    public ActividadVista actividadDeGestor(Long u, Long id) {
        return ActividadVista.de(actividades.actividadDeGestor(u, id));
    }

    public DetalleActividad detalleDeGestor(Long u, Long id) {
        return detalles.construir(actividades.actividadDeGestor(u, id));
    }
}
