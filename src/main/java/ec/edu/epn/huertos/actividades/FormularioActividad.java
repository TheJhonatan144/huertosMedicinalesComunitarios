package ec.edu.epn.huertos.actividades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public class FormularioActividad {

    @NotNull(message = "Selecciona el tipo de actividad.")
    private TipoActividad tipo;

    @NotBlank(message = "Describe brevemente lo que se hizo.")
    @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres.")
    private String descripcion;

    @NotNull(message = "Indica la fecha de la actividad.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    /** Solo se usa al registrar; al editar o reenviar se ignora (la lista queda fijada). */
    private List<Long> participanteIds = new ArrayList<>();

    public TipoActividad getTipo() {
        return tipo;
    }

    public void setTipo(TipoActividad tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public List<Long> getParticipanteIds() {
        return participanteIds;
    }

    public void setParticipanteIds(List<Long> participanteIds) {
        this.participanteIds = participanteIds == null ? new ArrayList<>() : participanteIds;
    }
}
