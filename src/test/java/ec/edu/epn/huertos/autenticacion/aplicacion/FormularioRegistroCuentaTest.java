package ec.edu.epn.huertos.autenticacion.aplicacion;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.epn.huertos.soporte.datos.FormulariosPrueba;
import ec.edu.epn.huertos.soporte.validacion.PruebaValidacion;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag("unitaria")
class FormularioRegistroCuentaTest extends PruebaValidacion {
    private FormularioRegistroCuenta cuenta;

    @BeforeEach
    void prepararFormulario() {
        cuenta = FormulariosPrueba.cuentaValida();
    }

    @Test
    void aceptaDatosValidosDeRegistroYClaveDeOchoCaracteres() {
        assertTrue(validator.validate(cuenta).isEmpty());
    }

    @Test
    void rechazaCamposDeCuentaInvalidos() {
        cuenta.setNombreUsuario("usuario con espacios");
        cuenta.setCorreo("sin-arroba.example");
        cuenta.setTelefono("abc");
        cuenta.setFechaNacimiento(HOY.plusDays(1));
        cuenta.setGenero("no definido");
        cuenta.setNivelExperiencia("EXPERTO");
        cuenta.setClave("corta");
        Set<String> campos =
                validator.validate(cuenta).stream()
                        .map(error -> error.getPropertyPath().toString())
                        .collect(Collectors.toSet());
        assertTrue(
                campos.containsAll(
                        Set.of(
                                "nombreUsuario",
                                "correo",
                                "telefono",
                                "fechaNacimiento",
                                "genero",
                                "nivelExperiencia",
                                "clave")));
    }
}
