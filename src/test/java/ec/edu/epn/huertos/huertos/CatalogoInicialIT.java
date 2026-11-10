package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import ec.edu.epn.huertos.PruebaIntegracion;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CatalogoInicialIT extends PruebaIntegracion {

    @Autowired DataSource dataSource;

    @Test
    void cargaTresBloquesYGuardaLasFotosEnPostgres() {
        String esquema = nuevoEsquema();
        var db = new JdbcTemplate(dataSource);
        try {
            var flyway = flyway(esquema);
            flyway.migrate();
            assertThat(db.queryForObject("SELECT count(*) FROM " + esquema + ".especie", Integer.class))
                    .isEqualTo(30);
            assertThat(db.queryForObject("""
                    SELECT count(*) FROM %1$s.especie e
                    JOIN %1$s.foto_catalogo_inicial f ON f.clave = e.foto_clave
                    WHERE e.foto_fuente IS NOT NULL AND substring(f.contenido, 1, 2) = decode('ffd8', 'hex')
                    """.formatted(esquema), Integer.class)).isEqualTo(30);
            assertThat(flyway.migrate().migrationsExecuted).isZero();
        } finally {
            db.execute("DROP SCHEMA IF EXISTS " + esquema + " CASCADE");
        }
    }

    @Test
    void actualizaElCatalogoSinSobrescribirUnaFichaOFotoExistente() {
        String esquema = nuevoEsquema();
        var db = new JdbcTemplate(dataSource);
        try {
            Flyway.configure().dataSource(dataSource).schemas(esquema).target("2026110802").load().migrate();
            db.execute("""
                    INSERT INTO %s.especie(nombre_comun, nombre_cientifico, fuente_documental, foto_clave, foto_tipo, foto_fuente)
                    VALUES ('Mi albahaca', 'Ocimum basilicum L.', 'Fuente local', '2026/11/11111111-1111-1111-1111-111111111111.jpg',
                            'image/jpeg', 'Autor local')
                    """.formatted(esquema));
            db.execute("INSERT INTO " + esquema + ".especie(nombre_comun) VALUES ('Achiote')");
            flyway(esquema).migrate();
            assertThat(db.queryForObject("SELECT foto_fuente FROM " + esquema + ".especie WHERE nombre_comun = 'Mi albahaca'", String.class))
                    .isEqualTo("Autor local");
            assertThat(db.queryForObject("SELECT count(*) FROM " + esquema + ".especie WHERE nombre_comun = 'Achiote'", Integer.class))
                    .isEqualTo(1);
            assertThat(db.queryForObject("SELECT count(*) FROM " + esquema + ".especie WHERE fuente_documental IS NOT NULL", Integer.class))
                    .isEqualTo(29);
        } finally {
            db.execute("DROP SCHEMA IF EXISTS " + esquema + " CASCADE");
        }
    }

    private Flyway flyway(String esquema) {
        return Flyway.configure().dataSource(dataSource).schemas(esquema)
                .locations("classpath:db/migration", "classpath:db/catalogo").load();
    }

    private String nuevoEsquema() {
        return "catalogo_" + UUID.randomUUID().toString().replace("-", "");
    }
}
