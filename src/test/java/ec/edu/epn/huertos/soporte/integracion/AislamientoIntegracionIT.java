package ec.edu.epn.huertos.soporte.integracion;

import static org.assertj.core.api.Assertions.assertThat;

import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;

/** Cada repetición deja cambios confirmados y un archivo: la siguiente debe empezar limpia. */
class AislamientoIntegracionIT extends PruebaIntegracion {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private FabricaDatos fabrica;

    @RepeatedTest(2)
    void cadaCasoEmpiezaSinDatosOperativosNiArchivosAjenos() throws Exception {
        assertThat(jdbc.queryForObject("select current_schema()", String.class))
                .isEqualTo(EntornoIntegracion.ESQUEMA);
        assertThat(jdbc.queryForObject("select count(*) from usuario", Long.class)).isZero();
        try (var archivos = Files.list(EntornoIntegracion.archivos())) {
            assertThat(archivos).isEmpty();
        }
        fabrica.usuario("rastro_aislamiento", Rol.SIN_ASIGNAR);
        Files.writeString(EntornoIntegracion.archivos().resolve("rastro.txt"), "dato sintético");
        assertThat(jdbc.queryForObject("select count(*) from usuario", Long.class)).isEqualTo(1L);
    }
}
