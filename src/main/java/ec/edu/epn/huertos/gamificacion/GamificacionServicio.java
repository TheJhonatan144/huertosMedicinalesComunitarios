package ec.edu.epn.huertos.gamificacion;

import ec.edu.epn.huertos.actividades.Actividad;
import ec.edu.epn.huertos.actividades.ActividadParticipante;
import ec.edu.epn.huertos.actividades.ActividadParticipanteRepository;
import ec.edu.epn.huertos.evidencias.EstadoRevision;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cálculo y otorgamiento de puntos y progreso. Los totales se derivan siempre de los movimientos
 * (recompensas), por lo que no existen contadores que puedan quedar desincronizados.
 */
@Service
public class GamificacionServicio {

    private final PropiedadesGamificacion config;
    private final ActividadParticipanteRepository participantes;
    private final RecompensaIndividualRepository individuales;
    private final RecompensaComunitariaRepository comunitarias;

    public GamificacionServicio(PropiedadesGamificacion config, ActividadParticipanteRepository participantes,
                                RecompensaIndividualRepository individuales,
                                RecompensaComunitariaRepository comunitarias) {
        this.config = config;
        this.participantes = participantes;
        this.individuales = individuales;
        this.comunitarias = comunitarias;
    }

    /**
     * Otorga la recompensa de una actividad aprobada. Debe invocarse dentro de la transacción de la aprobación.
     * Es idempotente: si ya existe la recompensa comunitaria no hace nada; además UNIQUE en base de datos
     * impide filas duplicadas aunque este chequeo se evadiera.
     */
    @Transactional
    public boolean otorgar(Actividad actividad) {
        if (actividad.getEstado() != EstadoRevision.APROBADA) {
            throw new IllegalStateException("Solo una actividad aprobada genera recompensas");
        }
        if (comunitarias.existsByActividadId(actividad.getId())) {
            return false;
        }
        for (ActividadParticipante p : participantes.findByActividadId(actividad.getId())) {
            individuales.save(new RecompensaIndividual(actividad.getId(), p.getIntegranteId(),
                    actividad.getComunidadId(), config.puntosIndividualesPorActividad()));
        }
        comunitarias.save(new RecompensaComunitaria(actividad.getId(), actividad.getComunidadId(),
                config.puntosComunitariosPorActividad(), config.progresoPorActividad()));
        individuales.flush();
        return true;
    }

    @Transactional(readOnly = true)
    public ResumenPuntos resumenIndividual(Long integranteId) {
        long puntos = individuales.totalPuntos(integranteId);
        Nivel nivel = nivelPara(puntos);
        return switch (nivel) {
            case APRENDIZ -> new ResumenPuntos(puntos, nivel, Nivel.OFICIAL, (int) (config.umbralOficial() - puntos));
            case OFICIAL -> new ResumenPuntos(puntos, nivel, Nivel.MAESTRO, (int) (config.umbralMaestro() - puntos));
            case MAESTRO -> new ResumenPuntos(puntos, nivel, null, null);
        };
    }

    public Nivel nivelPara(long puntos) {
        if (puntos >= config.umbralMaestro()) {
            return Nivel.MAESTRO;
        }
        return puntos >= config.umbralOficial() ? Nivel.OFICIAL : Nivel.APRENDIZ;
    }

    @Transactional(readOnly = true)
    public long puntosComunitarios(Long comunidadId) {
        return comunitarias.totalPuntos(comunidadId);
    }

    @Transactional(readOnly = true)
    public ProgresoHuerto progreso(Long comunidadId) {
        return progresoPara((int) Math.min(Integer.MAX_VALUE, comunitarias.totalProgreso(comunidadId)));
    }

    public ProgresoHuerto progresoPara(int progreso) {
        List<PropiedadesGamificacion.Etapa> etapas = config.etapas().stream()
                .sorted(java.util.Comparator.comparingInt(PropiedadesGamificacion.Etapa::desde)).toList();
        String actual = etapas.isEmpty() ? "Semilla" : etapas.get(0).nombre();
        String siguiente = null;
        Integer faltan = null;
        for (PropiedadesGamificacion.Etapa e : etapas) {
            if (e.desde() <= progreso) {
                actual = e.nombre();
            } else if (siguiente == null) {
                siguiente = e.nombre();
                faltan = e.desde() - progreso;
            }
        }
        int porcentaje = Math.min(100, (int) Math.round(progreso * 100.0 / Math.max(1, config.metaProgresoHuerto())));
        return new ProgresoHuerto(progreso, config.metaProgresoHuerto(), porcentaje, actual, siguiente, faltan);
    }

    /** Ranking comunitario por puntos comunitarios; las comunidades empatadas comparten posición (1, 1, 3). */
    @Transactional(readOnly = true)
    public List<FilaRanking> ranking() {
        List<FilaRanking> filas = new ArrayList<>();
        long anterior = -1;
        int posicion = 0;
        int indice = 0;
        for (PuntosComunidad c : comunitarias.puntosPorComunidad()) {
            indice++;
            if (c.puntos() != anterior) {
                posicion = indice;
                anterior = c.puntos();
            }
            filas.add(new FilaRanking(posicion, c.comunidadId(), c.nombre(), c.provincia(), c.puntos()));
        }
        return filas;
    }
}
