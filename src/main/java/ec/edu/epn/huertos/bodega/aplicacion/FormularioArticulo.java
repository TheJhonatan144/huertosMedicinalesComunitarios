package ec.edu.epn.huertos.bodega.aplicacion;
import ec.edu.epn.huertos.bodega.dominio.ArticuloBodega;
import ec.edu.epn.huertos.bodega.dominio.CategoriaArticulo;
import jakarta.validation.constraints.*;

public class FormularioArticulo {
 @NotBlank(message = "Indica el nombre.") @Size(max = 120) private String nombre;
 @NotNull(message = "Selecciona una categoría.") private CategoriaArticulo categoria;
 @NotNull @Min(value = 0, message = "La cantidad no puede ser negativa.") @Max(100000) private Integer cantidad = 0;
 public static FormularioArticulo de(ArticuloBodega a) {
  var f = new FormularioArticulo(); f.nombre = a.getNombre(); f.categoria = a.getCategoria(); f.cantidad = a.getCantidad(); return f;
 }
 public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = nombre; }
 public CategoriaArticulo getCategoria() { return categoria; } public void setCategoria(CategoriaArticulo categoria) { this.categoria = categoria; }
 public Integer getCantidad() { return cantidad; } public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public static FormularioArticulo de(ArticuloVista a) {
  var f = new FormularioArticulo(); f.nombre = a.getNombre(); f.categoria = a.getCategoria(); f.cantidad = a.getCantidad(); return f;
 }
}
