package ec.edu.epn.huertos.bodega.dominio;
public enum CategoriaArticulo {
 HERRAMIENTA("Herramienta o implemento"), SEMILLA("Semilla"), ESTACA("Estaca"), ESQUEJE("Esqueje"), PLANTULA("Plántula"), ABONO("Abono orgánico"), COMPOST("Compost");
 private final String etiqueta;
 CategoriaArticulo(String etiqueta) { this.etiqueta = etiqueta; }
 public String etiqueta() { return etiqueta; }
}
