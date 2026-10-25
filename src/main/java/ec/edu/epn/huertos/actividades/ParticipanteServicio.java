package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.comunidades.Comunidad;
import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.Integrante;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.evidencias.EstadoRevision;
import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import ec.edu.epn.huertos.gamificacion.ProgresoHuerto;
import ec.edu.epn.huertos.gamificacion.RecompensaIndividual;
import ec.edu.epn.huertos.gamificacion.RecompensaIndividualRepository;
import ec.edu.epn.huertos.gamificacion.ResumenPuntos;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Panel del participante: solo datos propios y públicos de su comunidad. */
@Service
public class ParticipanteServicio {

    private final ComunidadServicio comunidades;
    private final ActividadRepository actividades;
    private final ActividadParticipanteRepository participantes;
    private final RecompensaIndividualRepository individuales;
    private final GamificacionServicio gamificacion;

    public ParticipanteServicio(ComunidadServicio comunidades, ActividadRepository actividades,
                                ActividadParticipanteRepository participantes,
                                RecompensaIndividualRepository individuales, GamificacionServicio gamificacion) {
        this.comunidades = comunidades;
        this.actividades = actividades;
        this.participantes = participantes;
        this.individuales = individuales;
        this.gamificacion = gamificacion;
    }

    public record Participacion(Long actividadId, String tipo, String fecha, EstadoRevision estado, Integer puntos) {
    }

    public record Panel(String comunidad, boolean activo, ResumenPuntos puntos, long puntosComunitarios,
                        ProgresoHuerto progreso, List<Participacion> participaciones) {
    }

    @Transactional(readOnly = true)
    public Panel panel(Long usuarioId) {
        Integrante yo = comunidades.participanteDe(usuarioId);
        Comunidad comunidad = comunidades.comunidad(yo.getComunidadId());
        Map<Long, Integer> puntosPorActividad = individuales.findByIntegranteIdOrderByIdDesc(yo.getId()).stream()
                .collect(Collectors.toMap(RecompensaIndividual::getActividadId, RecompensaIndividual::getPuntos));
        List<Participacion> lista = actividades.findAllById(participantes.actividadesDeIntegrante(yo.getId())).stream()
                .sorted((a, b) -> b.getCreadaEn().compareTo(a.getCreadaEn()))
                .map(a -> new Participacion(a.getId(), a.getTipo().etiqueta(), Fechas.dia(a.getFechaActividad()),
                        a.getEstado(), puntosPorActividad.get(a.getId())))
                .toList();
        return new Panel(comunidad.getNombre(), yo.isActivo(), gamificacion.resumenIndividual(yo.getId()),
                gamificacion.puntosComunitarios(comunidad.getId()), gamificacion.progreso(comunidad.getId()), lista);
    }
}
