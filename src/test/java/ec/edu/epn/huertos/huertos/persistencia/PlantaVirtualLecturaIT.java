package ec.edu.epn.huertos.huertos.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import ec.edu.epn.huertos.huertos.aplicacion.PlantaPlantadaVista;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.soporte.integracion.PruebaIntegracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/** Consulta de lectura del huerto virtual: filtro y orden estable. */
class PlantaVirtualLecturaIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired PlantaRepository plantas;
    @Autowired ec.edu.epn.huertos.huertos.persistencia.EspecieRepository especies;
    @Autowired PlantaVirtualLectura lectura;

    private Long guardar(Long huertoId, String nombre) {
        var especie = especies.save(new ec.edu.epn.huertos.huertos.dominio.Especie(nombre, null));
        Planta p = new Planta(huertoId, especie.getId(), nombre, null, 1, false);
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

        assertThat(resultado)
                .extracting(PlantaPlantadaVista::nombreComun)
                .containsExactly("Toronjil", "Toronjil");
        assertThat(resultado).extracting(PlantaPlantadaVista::id).containsExactly(primero, segundo);
    }
}
