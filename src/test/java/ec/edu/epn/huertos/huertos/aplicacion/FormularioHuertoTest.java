package ec.edu.epn.huertos.huertos.aplicacion;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.epn.huertos.soporte.datos.FormulariosPrueba;
import ec.edu.epn.huertos.soporte.validacion.PruebaValidacion;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag("unitaria")
class FormularioHuertoTest extends PruebaValidacion {
    private FormularioHuerto huerto;

    @BeforeEach
    void prepararFormulario() {
        huerto = FormulariosPrueba.huertoValido();
    }

    @Test
    void validaAbonoDisenoYLimiteDeTiposDeCerca() {
        assertTrue(validator.validate(huerto).isEmpty());
        huerto.setDiseno("DISEÑO_DESCONOCIDO");
        huerto.setAbonoNatural("QUIMICO");
        huerto.setGruposCerramiento(
                List.of("VIVA", "NATURAL", "RECICLADO", "METALICO", "CONVENCIONAL", "EXTRA"));
        Set<String> campos =
                validator.validate(huerto).stream()
                        .map(error -> error.getPropertyPath().toString())
                        .collect(Collectors.toSet());
        assertTrue(campos.containsAll(Set.of("diseno", "abonoNatural", "gruposCerramiento")));
    }
}
