package ec.edu.epn.huertos.huertos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FormularioPlanta {

    @NotBlank(message = "Indica el nombre común de la planta.")
    @Size(max = 100, message = "El nombre común admite hasta 100 caracteres.")
    private String nombreComun;

    @Size(max = 120, message = "El nombre científico admite hasta 120 caracteres.")
    private String nombreCientifico;

    @Size(max = 1000, message = "La descripción admite hasta 1000 caracteres.")
    private String descripcion;

    @NotNull(message = "Indica la cantidad de plantas.")
    @Min(value = 0, message = "La cantidad no puede ser negativa.")
    @Max(value = 100000, message = "La cantidad es demasiado grande.")
    private Integer cantidad = 1;

    public static FormularioPlanta de(Planta p) {
        FormularioPlanta f = new FormularioPlanta();
        f.nombreComun = p.getNombreComun();
        f.nombreCientifico = p.getNombreCientifico();
        f.descripcion = p.getDescripcion();
        f.cantidad = p.getCantidad();
        return f;
    }

    public String getNombreComun() {
        return nombreComun;
    }

    public void setNombreComun(String nombreComun) {
        this.nombreComun = nombreComun;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public void setNombreCientifico(String nombreCientifico) {
        this.nombreCientifico = nombreCientifico;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
