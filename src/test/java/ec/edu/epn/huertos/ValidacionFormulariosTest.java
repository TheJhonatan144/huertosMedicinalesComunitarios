package ec.edu.epn.huertos;

import ec.edu.epn.huertos.autenticacion.FormularioRegistroCuenta;
import ec.edu.epn.huertos.huertos.FormularioHuerto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionFormulariosTest {
    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() { factory.close(); }

    @Test
    void aceptaDatosValidosDeRegistroYClaveDeOchoCaracteres() {
        assertTrue(validator.validate(cuentaValida()).isEmpty());
    }

    @Test
    void rechazaCamposDeCuentaInvalidos() {
        FormularioRegistroCuenta cuenta = cuentaValida();
        cuenta.setNombreUsuario("usuario con espacios");
        cuenta.setCorreo("sin-arroba.example");
        cuenta.setTelefono("abc");
        cuenta.setFechaNacimiento(LocalDate.now().plusDays(1));
        cuenta.setGenero("no definido");
        cuenta.setNivelExperiencia("EXPERTO");
        cuenta.setClave("corta");
        Set<String> campos = validator.validate(cuenta).stream()
                .map(error -> error.getPropertyPath().toString()).collect(Collectors.toSet());
        assertTrue(campos.containsAll(Set.of("nombreUsuario", "correo", "telefono", "fechaNacimiento", "genero", "nivelExperiencia", "clave")));
    }

    @Test
    void validaAbonoDisenoYLimiteDeTiposDeCerca() {
        FormularioHuerto huerto = huertoValido();
        assertTrue(validator.validate(huerto).isEmpty());
        huerto.setDiseno("DISEÑO_DESCONOCIDO");
        huerto.setAbonoNatural("QUIMICO");
        huerto.setGruposCerramiento(List.of("VIVA", "NATURAL", "RECICLADO", "METALICO", "CONVENCIONAL", "EXTRA"));
        Set<String> campos = validator.validate(huerto).stream()
                .map(error -> error.getPropertyPath().toString()).collect(Collectors.toSet());
        assertTrue(campos.containsAll(Set.of("diseno", "abonoNatural", "gruposCerramiento")));
    }

    private static FormularioRegistroCuenta cuentaValida() {
        FormularioRegistroCuenta f = new FormularioRegistroCuenta();
        f.setNombreCompleto("María Ejemplo"); f.setNombreUsuario("maria.ejemplo");
        f.setCorreo("maria@example.org"); f.setTelefono("+593 99 123 4567");
        f.setFechaNacimiento(LocalDate.of(1998, 4, 12)); f.setGenero("FEMENINO");
        f.setTalentosCertificados("Compostaje"); f.setNivelExperiencia("APRENDIZ");
        f.setClave("clave123"); f.setConfirmarClave("clave123");
        return f;
    }

    private static FormularioHuerto huertoValido() {
        FormularioHuerto f = new FormularioHuerto();
        f.setNombreComunidad("Comunidad demostrativa"); f.setTipoComunidad("FAMILIA");
        f.setDescripcionComunidad("Comunidad de ejemplo"); f.setProvinciaComunidad("Pichincha");
        f.setCantonComunidad("Quito"); f.setLatitudComunidad(-0.2); f.setLongitudComunidad(-78.5);
        f.setNombre("Huerto demostrativo"); f.setDescripcion("Descripción del huerto");
        f.setProvincia("Pichincha"); f.setParroquia("Centro"); f.setDireccion("Calle principal 123");
        f.setAreaM2(new BigDecimal("12.5")); f.setDiseno("VERTICAL_PAREDES");
        f.setTipoSuelo("LIMOSO"); f.setFuenteAgua("Lluvia"); f.setAbonoNatural("COMPOST");
        f.setCerramiento("NATURAL_CANA_GUADUA"); f.setLatitud(-0.2); f.setLongitud(-78.5);
        return f;
    }
}
