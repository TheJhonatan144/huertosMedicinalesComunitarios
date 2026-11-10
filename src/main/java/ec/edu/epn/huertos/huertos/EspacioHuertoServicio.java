package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspacioHuertoServicio {
    private static final Map<String, String> NOMBRES = Map.ofEntries(
            Map.entry("TIERRA_SURCOS", "Tierra · surcos"), Map.entry("TIERRA_CAMADAS", "Tierra · camadas"),
            Map.entry("TIERRA_CAMELLONES", "Tierra · camellones"), Map.entry("TIERRA_TERRAZAS", "Tierra · terrazas"),
            Map.entry("VERTICAL_PAREDES", "Vertical · paredes"), Map.entry("COLGANTE_TECHOS", "Colgante · techos"),
            Map.entry("PISO_MACETAS", "Piso · macetas"), Map.entry("PISO_BOTELLAS_PATIO", "Piso · botellas"),
            Map.entry("PISO_LLANTAS_PATIO", "Piso · llantas"), Map.entry("PISO_TERRAZA", "Piso · terraza"),
            Map.entry("PISO_GRADAS", "Piso · gradas"), Map.entry("PISO_BALCONES", "Piso · balcones"),
            Map.entry("PISO_FILOS_VENTANA", "Piso · filos de ventanas"));
    private final EspacioHuertoRepository espacios;

    public EspacioHuertoServicio(EspacioHuertoRepository espacios) { this.espacios = espacios; }

    @Transactional(readOnly = true)
    public List<EspacioHuerto> activos(Long huertoId) {
        return espacios.findByHuertoIdAndActivaTrueOrderByNombreAsc(huertoId);
    }

    @Transactional
    public void sincronizar(Long huertoId, BigDecimal areaTotal, List<String> seleccionados) {
        List<String> disenos = seleccionados == null ? List.of() : seleccionados.stream().distinct().toList();
        if (disenos.isEmpty() || !NOMBRES.keySet().containsAll(disenos))
            throw new ReglaNegocioException("Selecciona uno o más diseños válidos para el huerto.");
        if (areaTotal.compareTo(BigDecimal.valueOf(disenos.size()).movePointLeft(2)) < 0)
            throw new ReglaNegocioException("El área total es demasiado pequeña para repartirla entre los espacios seleccionados.");
        espacios.deleteByHuertoIdAndDisenoNotIn(huertoId, disenos);
        BigDecimal areaEspacio = areaTotal.divide(BigDecimal.valueOf(disenos.size()), 2, RoundingMode.HALF_UP);
        BigDecimal usada = areaEspacio.multiply(BigDecimal.valueOf(disenos.size() - 1L));
        for (int i = 0; i < disenos.size(); i++) {
            String diseno = disenos.get(i);
            BigDecimal area = i == disenos.size() - 1 ? areaTotal.subtract(usada) : areaEspacio;
            EspacioHuerto existente = espacios.findByHuertoIdOrderByNombreAsc(huertoId).stream()
                    .filter(e -> e.getDiseno().equals(diseno)).findFirst().orElse(null);
            if (existente == null) espacios.save(new EspacioHuerto(huertoId, NOMBRES.get(diseno), diseno, area, ""));
            else existente.actualizar(NOMBRES.get(diseno), diseno, area, existente.getDescripcion());
        }
    }
}
