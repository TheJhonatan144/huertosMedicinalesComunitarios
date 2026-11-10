package ec.edu.epn.huertos.comunidades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FormularioSolicitudEliminacion {
    @NotBlank(message = "Explica por qué solicitas eliminar la comunidad.")
    @Size(max = 2000, message = "El motivo admite hasta 2000 caracteres.")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
