package ec.edu.epn.huertos.soporte.validacion;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.*;

import java.time.*;

/** Recurso compartido por clase, cerrado al terminar; reloj fijo para restricciones temporales. */
public abstract class PruebaValidacion {
    protected static final LocalDate HOY = LocalDate.of(2026, 11, 12);
    private static ValidatorFactory fabrica;
    protected static Validator validator;

    @BeforeAll
    static void iniciarValidador() {
        Clock reloj =
                Clock.fixed(Instant.parse("2026-11-12T17:00:00Z"), ZoneId.of("America/Guayaquil"));
        fabrica =
                Validation.byDefaultProvider()
                        .configure()
                        .clockProvider(() -> reloj)
                        .buildValidatorFactory();
        validator = fabrica.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        if (fabrica != null) fabrica.close();
    }
}
