package ec.edu.epn.huertos.actividades.aplicacion;
import ec.edu.epn.huertos.actividades.dominio.Actividad;
import ec.edu.epn.huertos.actividades.persistencia.ActividadParticipanteRepository;
import ec.edu.epn.huertos.autenticacion.dominio.Usuario;
import ec.edu.epn.huertos.autenticacion.persistencia.UsuarioRepository;
import ec.edu.epn.huertos.compartido.dominio.Fechas;
import ec.edu.epn.huertos.comunidades.persistencia.ComunidadRepository;
import ec.edu.epn.huertos.evidencias.dominio.EnvioEvidencia;
import ec.edu.epn.huertos.evidencias.dominio.Revision;
import ec.edu.epn.huertos.evidencias.persistencia.EnvioEvidenciaRepository;
import ec.edu.epn.huertos.evidencias.persistencia.RevisionRepository;
import ec.edu.epn.huertos.gamificacion.dominio.RecompensaComunitaria;
import ec.edu.epn.huertos.gamificacion.dominio.RecompensaIndividual;
import ec.edu.epn.huertos.gamificacion.persistencia.RecompensaComunitariaRepository;
import ec.edu.epn.huertos.gamificacion.persistencia.RecompensaIndividualRepository;
import ec.edu.epn.huertos.huertos.persistencia.HuertoRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma la vista de detalle de una actividad. NO autoriza: quien la invoque (gestor o revisor)
 * debe haber verificado antes el acceso a la comunidad de la actividad.
 */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class DetalleActividadServicio {

    private final ComunidadRepository comunidades;
    private final HuertoRepository huertos;
    private final UsuarioRepository usuarios;
    private final ActividadParticipanteRepository participantes;
    private final EnvioEvidenciaRepository envios;
    private final RevisionRepository revisiones;
    private final RecompensaIndividualRepository individuales;
    private final RecompensaComunitariaRepository comunitarias;

    public DetalleActividadServicio(ComunidadRepository comunidades, HuertoRepository huertos,
                                    UsuarioRepository usuarios, ActividadParticipanteRepository participantes,
                                    EnvioEvidenciaRepository envios, RevisionRepository revisiones,
                                    RecompensaIndividualRepository individuales,
                                    RecompensaComunitariaRepository comunitarias) {
        this.comunidades = comunidades;
        this.huertos = huertos;
        this.usuarios = usuarios;
        this.participantes = participantes;
        this.envios = envios;
        this.revisiones = revisiones;
        this.individuales = individuales;
        this.comunitarias = comunitarias;
    }

    @Transactional(readOnly = true)
    public DetalleActividad construir(Actividad a) {
        List<EnvioEvidencia> listaEnvios = envios.findByActividadIdOrderByNumeroAsc(a.getId());
        Map<Long, Revision> porEnvio = revisiones
                .findByEnvioIdIn(listaEnvios.stream().map(EnvioEvidencia::getId).toList()).stream()
                .collect(Collectors.toMap(Revision::getEnvioId, Function.identity()));
        Map<Long, String> nombres = usuarios.findAllById(
                        java.util.stream.Stream.concat(
                                java.util.stream.Stream.of(a.getRegistradaPor()),
                                porEnvio.values().stream().map(Revision::getRevisorId)).distinct().toList())
                .stream().collect(Collectors.toMap(Usuario::getId, Usuario::getNombreCompleto));

        List<DetalleActividad.EnvioVista> vistaEnvios = listaEnvios.stream().map(e -> {
            Revision r = porEnvio.get(e.getId());
            return new DetalleActividad.EnvioVista(e.getId(), e.getNumero(), Fechas.hora(e.getEnviadoEn()),
                    e.getEstado(), e.getComentario(), e.getNombreOriginal(), e.getTipoContenido(), e.getDuracionSegundos(),
                    r == null ? null : nombres.get(r.getRevisorId()),
                    r == null ? null : Fechas.hora(r.getRevisadaEn()),
                    r == null ? null : r.getObservaciones());
        }).toList();

        Integer puntos = individuales.findByActividadId(a.getId()).stream().findFirst()
                .map(RecompensaIndividual::getPuntos).orElse(null);
        RecompensaComunitaria comunitaria = comunitarias.findByActividadId(a.getId()).orElse(null);

        return new DetalleActividad(a.getId(),
                comunidades.findById(a.getComunidadId()).map(c -> c.getNombre()).orElse(""),
                huertos.findById(a.getHuertoId()).map(h -> h.getNombre()).orElse(""),
                a.getTipo(), a.getDescripcion(), Fechas.dia(a.getFechaActividad()), a.getFechaActividad(),
                a.getEstado(), nombres.get(a.getRegistradaPor()), Fechas.hora(a.getCreadaEn()),
                participantes.vistaPorActividad(a.getId()), vistaEnvios, puntos,
                comunitaria == null ? null : comunitaria.getPuntos(),
                comunitaria == null ? null : comunitaria.getProgreso());
    }
}
