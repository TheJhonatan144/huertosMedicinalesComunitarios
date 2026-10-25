package ec.edu.epn.huertos.evidencias;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Límites provisionales de las fotografías (pendientes de aprobación; ver docs/pendientes.md). */
@ConfigurationProperties("app.almacenamiento")
public record PropiedadesAlmacenamiento(String directorio, long tamanoMaximoBytes, int ladoMaximoPx, long pixelesMaximos) {
}
