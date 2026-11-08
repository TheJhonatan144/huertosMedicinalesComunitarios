package ec.edu.epn.huertos.huertos;
public enum CicloPlanta {
CORTO("Corto"), LARGO("Largo");
private final String etiqueta;
CicloPlanta(String etiqueta) { this.etiqueta = etiqueta; }
public String etiqueta() { return etiqueta; }
}
