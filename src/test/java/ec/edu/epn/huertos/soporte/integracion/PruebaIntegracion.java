package ec.edu.epn.huertos.soporte.integracion;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de las pruebas de integración: PostgreSQL real (Testcontainers), una sola instancia por
 * ejecución. Cada caso utiliza un esquema dedicado cuyos datos operativos y archivos se limpian
 * antes y después de su transacción.
 */
@SpringBootTest
@ActiveProfiles("test")
@org.junit.jupiter.api.Tag("integracion")
@org.springframework.test.context.TestExecutionListeners(
        listeners = AislamientoIntegracion.class,
        mergeMode =
                org.springframework.test.context.TestExecutionListeners.MergeMode
                        .MERGE_WITH_DEFAULTS)
public abstract class PruebaIntegracion {

    private static final String URL_EXTERNA = System.getProperty("huertos.test.db.url");
    static final PostgreSQLContainer POSTGRES =
            URL_EXTERNA == null ? new PostgreSQLContainer("postgres:17-alpine") : null;

    static {
        if (POSTGRES != null) POSTGRES.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.hikari.schema", () -> EntornoIntegracion.ESQUEMA);
        registro.add("spring.flyway.schemas", () -> EntornoIntegracion.ESQUEMA);
        registro.add("spring.flyway.default-schema", () -> EntornoIntegracion.ESQUEMA);
        registro.add(
                "app.almacenamiento.directorio", () -> EntornoIntegracion.archivos().toString());
        registro.add("app.archivos.reintento-borrado-ms", () -> "3600000");
        if (URL_EXTERNA != null) {
            registro.add("spring.datasource.url", () -> URL_EXTERNA);
            registro.add(
                    "spring.datasource.username",
                    () -> System.getProperty("huertos.test.db.usuario", "huertos"));
            registro.add(
                    "spring.datasource.password",
                    () -> System.getProperty("huertos.test.db.clave", ""));
        } else {
            registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registro.add("spring.datasource.username", POSTGRES::getUsername);
            registro.add("spring.datasource.password", POSTGRES::getPassword);
        }
    }

    @org.junit.jupiter.api.AfterEach
    void liberarContextoDePeticion() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }
}
