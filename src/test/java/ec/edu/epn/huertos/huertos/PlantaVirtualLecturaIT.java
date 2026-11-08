package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.PruebaIntegracion;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Consulta de lectura del huerto virtual: filtro y orden estable. */
class PlantaVirtualLecturaIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired PlantaRepository plantas;
    @Autowired PlantaVirtualLectura lectura;

    private Long guardar(Long huertoId, String nombre) {
        Planta p = new Planta(huertoId, nombre, null, null, 1);
        p.cambiarPlantada(true);
        return plantas.save(p).getId();
    }

    @Test
    void aIgualNombreComunDesempataPorIdAscendente() {
        Escenario e = fabrica.escenario(0);
        Long huertoId = e.huerto().getId();
        Long primero = guardar(huertoId, "Toronjil");
        Long segundo = guardar(huertoId, "Toronjil");
        assertThat(primero).isLessThan(segundo);

        List<PlantaPlantadaVista> resultado = lectura.plantadasActivas(huertoId);

        assertThat(resultado).extracting(PlantaPlantadaVista::nombreComun).containsExactly("Toronjil", "Toronjil");
        assertThat(resultado).extracting(PlantaPlantadaVista::id).containsExactly(primero, segundo);
    }
}
