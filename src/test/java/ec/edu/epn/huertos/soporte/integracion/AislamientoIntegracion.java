package ec.edu.epn.huertos.soporte.integracion;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.util.ArrayList;
import java.util.Set;

import javax.sql.DataSource;

/** Limpia antes de abrir y después de cerrar la transacción de cada prueba, incluidos fallos. */
public class AislamientoIntegracion extends AbstractTestExecutionListener {
    private static final Set<String> SEMILLAS =
            Set.of("flyway_schema_history", "mision_plantilla", "mision_dependencia");

    @Override
    public int getOrder() {
        return 3500;
    } // TransactionalTestExecutionListener usa 4000.

    @Override
    public void beforeTestMethod(TestContext contexto) throws Exception {
        limpiar(contexto);
    }

    @Override
    public void afterTestMethod(TestContext contexto) throws Exception {
        limpiar(contexto);
    }

    private void limpiar(TestContext contexto) throws Exception {
        DataSource fuente = contexto.getApplicationContext().getBean(DataSource.class);
        try (var conexion = fuente.getConnection()) {
            conexion.setAutoCommit(true);
            var tablas = new ArrayList<String>();
            try (var consulta =
                    conexion.prepareStatement(
                            "select tablename from pg_tables where schemaname = ? order by"
                                + " tablename")) {
                consulta.setString(1, EntornoIntegracion.ESQUEMA);
                try (var filas = consulta.executeQuery()) {
                    while (filas.next()) {
                        String tabla = filas.getString(1);
                        if (!SEMILLAS.contains(tabla))
                            tablas.add(citar(EntornoIntegracion.ESQUEMA) + "." + citar(tabla));
                    }
                }
            }
            if (!tablas.isEmpty())
                try (var orden = conexion.createStatement()) {
                    orden.setQueryTimeout(20);
                    // Sin CASCADE: no permite extender la limpieza a otros esquemas.
                    orden.execute(
                            "TRUNCATE TABLE " + String.join(",", tablas) + " RESTART IDENTITY");
                }
        }
        EntornoIntegracion.limpiarArchivos();
    }

    private String citar(String identificador) {
        return "\"" + identificador.replace("\"", "\"\"") + "\"";
    }
}
