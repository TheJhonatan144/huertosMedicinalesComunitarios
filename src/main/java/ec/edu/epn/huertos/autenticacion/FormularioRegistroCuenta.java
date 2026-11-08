package ec.edu.epn.huertos.autenticacion;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FormularioRegistroCuenta {
    @NotBlank(message = "Escribe tu nombre completo.")
    @Size(max = 120, message = "El nombre admite hasta 120 caracteres.")
    private String nombreCompleto, nombreUsuario, correo, telefono, genero, talentosCertificados;
    private String nivelExperiencia = "APRENDIZ", clave, confirmarClave;
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Pattern(regexp = "[A-Za-z0-9._-]{3,50}", message = "Usa de 3 a 50 letras, números, punto, guion o guion bajo.")
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String v) { nombreUsuario = v; }

    @NotBlank(message = "El correo es obligatorio.") @Email(message = "Escribe un correo válido con @.")
    @Size(max = 254, message = "El correo admite hasta 254 caracteres.")
    public String getCorreo() { return correo; }
    public void setCorreo(String v) { correo = v; }

    @NotBlank(message = "El teléfono es obligatorio.") @Pattern(regexp = "[+0-9() -]{7,30}", message = "Revisa el formato del teléfono.")
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { telefono = v; }

    @NotNull(message = "Indica tu fecha de nacimiento.") @PastOrPresent(message = "La fecha no puede estar en el futuro.")
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate v) { fechaNacimiento = v; }

    @NotBlank(message = "Selecciona tu género.")
    @Pattern(regexp = "FEMENINO|MASCULINO|NO_BINARIO|OTRO|PREFIERO_NO_DECIR", message = "Selecciona una opción válida.")
    public String getGenero() { return genero; }
    public void setGenero(String v) { genero = v; }

    @Size(max = 2000, message = "Los talentos certificados admiten hasta 2000 caracteres.")
    public String getTalentosCertificados() { return talentosCertificados; }
    public void setTalentosCertificados(String v) { talentosCertificados = v; }

    @NotBlank(message = "Selecciona tu nivel de experiencia.")
    @Pattern(regexp = "APRENDIZ|OFICIAL|MAESTRO", message = "Selecciona un nivel válido.")
    public String getNivelExperiencia() { return nivelExperiencia; }
    public void setNivelExperiencia(String v) { nivelExperiencia = v; }

    @NotBlank(message = "La contraseña es obligatoria.") @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    public String getClave() { return clave; }
    public void setClave(String v) { clave = v; }

    @NotBlank(message = "Confirma tu contraseña.") @Size(min = 8, max = 72, message = "La confirmación debe tener entre 8 y 72 caracteres.")
    public String getConfirmarClave() { return confirmarClave; }
    public void setConfirmarClave(String v) { confirmarClave = v; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String v) { nombreCompleto = v; }
}
