package ec.edu.epn.huertos.misiones.persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MisionDependenciaRepository {
    private final JdbcTemplate jdbc;

    public MisionDependenciaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean tieneDependencias(Long plantillaId) {
        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        "select count(*) > 0 from mision_dependencia where mision_id = ?",
                        Boolean.class,
                        plantillaId));
    }

    public Long plantillaDisponible(Long id, Long huerto) {
        return jdbc.queryForObject(
                "select plantilla_id from mision_huerto where id = ? and huerto_id = ? and estado ="
                        + " 'DISPONIBLE'",
                Long.class,
                id,
                huerto);
    }

    public List<Long> requisitos(Long plantillaId) {
        return jdbc.queryForList(
                "select depende_de_id from mision_dependencia where mision_id = ?",
                Long.class,
                plantillaId);
    }

    public void habilitar(Long id) {
        jdbc.update("update mision_huerto set estado = 'DISPONIBLE' where id = ?", id);
    }
}
