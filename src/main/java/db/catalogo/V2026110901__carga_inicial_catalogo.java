package db.catalogo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.HexFormat;
import java.util.List;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

public class V2026110901__carga_inicial_catalogo extends BaseJavaMigration {

    private static final String RECURSOS = "catalogo-inicial/";

    @Override
    public void migrate(Context context) throws Exception {
        Connection db = context.getConnection();
        try (var sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE foto_catalogo_inicial (
                    clave VARCHAR(200) PRIMARY KEY,
                    contenido BYTEA NOT NULL
                )
                """);
            sql.execute("SET LOCAL huertos.carga_catalogo_automatica = 'on'");
            for (String bloque : List.of("01", "02", "03")) {
                sql.execute(new String(recurso("carga-bloque-" + bloque + ".sql"), StandardCharsets.UTF_8));
            }
        }
        for (String carpeta : List.of("fotografias/", "fotografias/bloque-02/", "fotografias/bloque-03/")) {
            cargarFotos(db, carpeta);
        }
    }

    private void cargarFotos(Connection db, String carpeta) throws Exception {
        var fotos = JsonMapper.builder().build().readTree(recurso(carpeta + "fotografias.json")).required("fotografias");
        try (var insertar = db.prepareStatement("INSERT INTO foto_catalogo_inicial(clave, contenido) VALUES (?, ?)");
             var asociar = db.prepareStatement("""
                 UPDATE especie SET foto_clave = ?, foto_tipo = 'image/jpeg',
                     foto_fuente = ?, version = version + 1
                 WHERE foto_clave IS NULL AND id = (
                     SELECT min(id) FROM especie
                     WHERE lower(btrim(nombre_cientifico)) = lower(btrim(?))
                     HAVING count(*) = 1
                 )
                 """)) {
            for (var foto : fotos) {
                String ruta = carpeta + foto.required("archivo").asString();
                String clave = RECURSOS + ruta;
                byte[] contenido = recurso(ruta);
                String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
                if (!hash.equalsIgnoreCase(foto.required("sha256").asString())) {
                    throw new IOException("La fotografía del catálogo no coincide con su hash: " + ruta);
                }
                insertar.setString(1, clave);
                insertar.setBytes(2, contenido);
                insertar.executeUpdate();
                asociar.setString(1, clave);
                asociar.setString(2, foto.required("atribucion").asString());
                asociar.setString(3, foto.required("nombre_cientifico").asString());
                asociar.executeUpdate();
            }
        }
    }

    private byte[] recurso(String ruta) throws IOException {
        return new ClassPathResource(RECURSOS + ruta).getContentAsByteArray();
    }
}
