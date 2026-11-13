package ec.edu.epn.huertos.soporte.datos;

import ec.edu.epn.huertos.autenticacion.aplicacion.FormularioRegistroCuenta;
import ec.edu.epn.huertos.huertos.aplicacion.FormularioHuerto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cada llamada entrega un formulario independiente; nunca comparte objetos mutables entre casos.
 */
public final class FormulariosPrueba {
    private FormulariosPrueba() {}

    public static FormularioRegistroCuenta cuentaValida() {
        FormularioRegistroCuenta f = new FormularioRegistroCuenta();
        f.setNombreCompleto("María Ejemplo");
        f.setNombreUsuario("maria.ejemplo");
        f.setCorreo("maria@example.org");
        f.setTelefono("+593 99 123 4567");
        f.setFechaNacimiento(LocalDate.of(1998, 4, 12));
        f.setGenero("FEMENINO");
        f.setTalentosCertificados("Compostaje");
        f.setNivelExperiencia("APRENDIZ");
        f.setClave("clave123");
        f.setConfirmarClave("clave123");
        return f;
    }

    public static FormularioHuerto huertoValido() {
        FormularioHuerto f = new FormularioHuerto();
        f.setNombreComunidad("Comunidad demostrativa");
        f.setTipoComunidad("FAMILIA");
        f.setDescripcionComunidad("Comunidad de ejemplo");
        f.setProvinciaComunidad("Pichincha");
        f.setCantonComunidad("Quito");
        f.setLatitudComunidad(-0.2);
        f.setLongitudComunidad(-78.5);
        f.setNombre("Huerto demostrativo");
        f.setDescripcion("Descripción del huerto");
        f.setProvincia("Pichincha");
        f.setParroquia("Centro");
        f.setDireccion("Calle principal 123");
        f.setAreaM2(new BigDecimal("12.5"));
        f.setDiseno("VERTICAL_PAREDES");
        f.setTipoSuelo("LIMOSO");
        f.setFuenteAgua("Lluvia");
        f.setAbonoNatural("COMPOST");
        f.setCerramiento("NATURAL_CANA_GUADUA");
        f.setLatitud(-0.2);
        f.setLongitud(-78.5);
        return f;
    }
}
