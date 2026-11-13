package ec.edu.epn.huertos.huertos.persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FotoCatalogoInicialRepository {
    private final JdbcTemplate jdbc;

    public FotoCatalogoInicialRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<byte[]> contenido(String clave) {
        return jdbc
                .query(
                        "SELECT contenido FROM foto_catalogo_inicial WHERE clave = ?",
                        (fila, numero) -> fila.getBytes(1),
                        clave)
                .stream()
                .findFirst();
    }
}
