package ec.edu.epn.huertos.huertos.aplicacion;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.huertos.dominio.HuertoCerramiento;
import ec.edu.epn.huertos.huertos.persistencia.HuertoCerramientoRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class HuertoCerramientoServicio {
    public static final Set<String> GRUPOS = Set.of("VIVA", "NATURAL", "RECICLADO", "METALICO", "CONVENCIONAL");
    private final HuertoCerramientoRepository cerramientos;

    public HuertoCerramientoServicio(HuertoCerramientoRepository cerramientos) {
        this.cerramientos = cerramientos;
    }

    @Transactional(readOnly = true)
    public List<String> grupos(Long huertoId) {
        return cerramientos.findByHuertoIdOrderByGrupoAsc(huertoId).stream()
                .map(HuertoCerramiento::getGrupo).toList();
    }

    @Transactional
    public void sincronizar(Long huertoId, List<String> seleccionados) {
        List<String> grupos = seleccionados == null ? List.of() : seleccionados.stream().distinct().toList();
        if (!GRUPOS.containsAll(grupos))
            throw new ReglaNegocioException("Selecciona grupos de cerramiento válidos.");
        cerramientos.deleteByHuertoId(huertoId);
        cerramientos.saveAll(grupos.stream().map(grupo -> new HuertoCerramiento(huertoId, grupo)).toList());
    }
}
