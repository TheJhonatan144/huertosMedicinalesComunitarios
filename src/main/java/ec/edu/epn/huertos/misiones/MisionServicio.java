package ec.edu.epn.huertos.misiones;

import ec.edu.epn.huertos.actividades.TipoActividad;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orquesta el ciclo de vida de las misiones únicas de inicio de cada huerto. */
@Service
public class MisionServicio {
    private final MisionPlantillaRepository plantillas;
    private final MisionHuertoRepository misiones;
    private final JdbcTemplate jdbc;

    public MisionServicio(MisionPlantillaRepository plantillas, MisionHuertoRepository misiones, JdbcTemplate jdbc) {
        this.plantillas = plantillas; this.misiones = misiones; this.jdbc = jdbc;
    }

    @Transactional
    public void crearParaHuerto(Long huertoId) {
        if (misiones.existsByHuertoId(huertoId)) return;
        List<MisionPlantilla> todas = plantillas.findByActivaTrueOrderByOrdenAsc();
        for (MisionPlantilla plantilla : todas) {
            boolean bloqueada = jdbc.queryForObject(
                    "select count(*) > 0 from mision_dependencia where mision_id = ?",
                    Boolean.class, plantilla.getId());
            misiones.save(new MisionHuerto(huertoId, plantilla.getId(),
                    bloqueada ? EstadoMision.BLOQUEADA : EstadoMision.DISPONIBLE));
        }
    }

    public record RecompensaMision(int puntosIndividuales, int puntosComunitarios, int progreso) {}
    public record MisionVista(Long id, String codigo, String titulo, String descripcion, String tipo,
                              int puntosIndividuales, int puntosComunitarios, EstadoMision estado, Long actividadId) {}

    @Transactional(readOnly = true)
    public List<MisionVista> listarParaHuerto(Long huertoId) {
        Map<Long, MisionPlantilla> porId = plantillas.findAll().stream()
                .collect(Collectors.toMap(MisionPlantilla::getId, p -> p));
        return misiones.findByHuertoIdOrderByIdAsc(huertoId).stream().map(m -> {
            MisionPlantilla p = porId.get(m.getPlantillaId());
            return new MisionVista(m.getId(), p.getCodigo(), p.getTitulo(), p.getDescripcion(),
                    p.getTipoActividad().etiqueta(), p.getPuntosIndividuales(), p.getPuntosComunitarios(), m.getEstado(), m.getActividadId());
        }).toList();
    }

    @Transactional(readOnly = true)
    public MisionVista detalleDisponible(Long misionHuertoId, Long huertoId) {
        MisionHuerto m = misiones.findByIdAndHuertoId(misionHuertoId, huertoId)
                .orElseThrow(() -> new ReglaNegocioException("La misión no pertenece a este huerto."));
        MisionPlantilla p = plantillas.findById(m.getPlantillaId()).orElseThrow();
        return new MisionVista(m.getId(), p.getCodigo(), p.getTitulo(), p.getDescripcion(),
                p.getTipoActividad().name(), p.getPuntosIndividuales(), p.getPuntosComunitarios(), m.getEstado(), m.getActividadId());
    }

    @Transactional(readOnly = true)
    public void exigirDisponible(Long misionHuertoId, Long huertoId, TipoActividad tipo) {
        Long plantillaId;
        try {
            plantillaId = jdbc.queryForObject(
                    "select plantilla_id from mision_huerto where id = ? and huerto_id = ? and estado = 'DISPONIBLE'",
                    Long.class, misionHuertoId, huertoId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new ReglaNegocioException("La misión no pertenece a este huerto o todavía no está disponible.");
        }
        MisionPlantilla plantilla = plantillas.findById(plantillaId).orElseThrow();
        if (plantilla.getTipoActividad() != tipo)
            throw new ReglaNegocioException("El tipo de actividad no corresponde a esta misión.");
    }

    @Transactional(readOnly = true)
    public Optional<RecompensaMision> recompensa(Long misionHuertoId) {
        if (misionHuertoId == null) return Optional.empty();
        return misiones.findById(misionHuertoId).flatMap(m -> plantillas.findById(m.getPlantillaId())
                .map(p -> new RecompensaMision(p.getPuntosIndividuales(), p.getPuntosComunitarios(), p.getProgreso())));
    }

    @Transactional
    public void vincularActividad(Long misionHuertoId, Long huertoId, Long actividadId) {
        MisionHuerto mision = misiones.bloquearPorIdYHuerto(misionHuertoId, huertoId)
                .orElseThrow(() -> new ReglaNegocioException("La misión no pertenece a este huerto."));
        mision.iniciar(actividadId);
    }

    @Transactional
    public void reenviarActividad(Long actividadId) {
        misiones.findByActividadId(actividadId).ifPresent(MisionHuerto::reenviar);
    }

    @Transactional
    public void completarPorActividad(Long actividadId, boolean aprobada) {
        Optional<MisionHuerto> encontrada = misiones.findByActividadId(actividadId);
        if (encontrada.isEmpty()) return; // Las actividades libres conservan el comportamiento anterior.
        MisionHuerto mision = encontrada.get();
        if (!aprobada) { mision.rechazar(); return; }
        mision.completar();
        desbloquearDependientes(mision.getHuertoId());
    }

    private void desbloquearDependientes(Long huertoId) {
        List<MisionHuerto> delHuerto = misiones.findByHuertoIdOrderByIdAsc(huertoId);
        Map<Long, MisionHuerto> porPlantilla = delHuerto.stream()
                .collect(Collectors.toMap(MisionHuerto::getPlantillaId, m -> m));
        for (MisionHuerto candidata : delHuerto) {
            if (candidata.getEstado() != EstadoMision.BLOQUEADA) continue;
            List<Long> dependencias = jdbc.queryForList(
                    "select depende_de_id from mision_dependencia where mision_id = ?", Long.class,
                    candidata.getPlantillaId());
            if (!dependencias.isEmpty() && dependencias.stream().allMatch(id ->
                    porPlantilla.containsKey(id) && porPlantilla.get(id).getEstado() == EstadoMision.COMPLETADA)) {
                jdbc.update("update mision_huerto set estado = 'DISPONIBLE' where id = ?", candidata.getId());
            }
        }
    }
}
