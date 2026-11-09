package ec.edu.epn.huertos.comunidades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FormularioRechazoSolicitud {
    @NotBlank(message = "Escribe el motivo del rechazo.")
    @Size(max = 2000, message = "El motivo admite hasta 2000 caracteres.")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
