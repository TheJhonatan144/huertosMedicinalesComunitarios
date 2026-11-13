package ec.edu.epn.huertos.soporte.integracion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import javax.sql.DataSource;

/**
 * Las cargas iniciales se verifican con un catálogo vacío, sin depender del orden de las demás
 * pruebas.
 */
@Transactional
public abstract class PruebaCatalogoAislado extends PruebaIntegracion {
    @Autowired private DataSource fuenteCatalogo;
    @Autowired private JdbcTemplate conexionCatalogo;
    private String esquemaCatalogo;

    @BeforeEach
    void crearEsquemaCatalogo() {
        esquemaCatalogo = "prueba_catalogo_" + UUID.randomUUID().toString().replace("-", "");
        Flyway.configure()
                .dataSource(fuenteCatalogo)
                .schemas(esquemaCatalogo)
                .defaultSchema(esquemaCatalogo)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        conexionCatalogo.execute("SET LOCAL search_path TO " + esquemaCatalogo);
    }

    @AfterTransaction
    void eliminarEsquemaCatalogo() throws Exception {
        if (esquemaCatalogo == null) return;
        try (var conexion = fuenteCatalogo.getConnection();
                var orden = conexion.createStatement()) {
            orden.execute("DROP SCHEMA " + esquemaCatalogo + " CASCADE");
        }
    }
}
