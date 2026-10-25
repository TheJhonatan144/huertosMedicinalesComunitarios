package ec.edu.epn.huertos.huertos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FormularioHuerto {

    @NotBlank(message = "Indica el nombre del huerto.")
    @Size(max = 120, message = "El nombre admite hasta 120 caracteres.")
    private String nombre;

    @NotBlank(message = "Escribe una descripción del huerto.")
    @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres.")
    private String descripcion;

    public static FormularioHuerto de(Huerto h) {
        FormularioHuerto f = new FormularioHuerto();
        f.nombre = h.getNombre();
        f.descripcion = h.getDescripcion();
        return f;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
