package ec.edu.epn.huertos.huertos;
import jakarta.validation.constraints.*;
public class SiembraPlanta {
 @NotNull(message = "Indica la cantidad.") @Min(value = 0, message = "La cantidad no puede ser negativa.") @Max(100000)
 private Integer cantidad = 1;
 private boolean plantada;
 public Integer getCantidad() { return cantidad; }
 public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
 public boolean isPlantada() { return plantada; }
 public void setPlantada(boolean plantada) { this.plantada = plantada; }
}
