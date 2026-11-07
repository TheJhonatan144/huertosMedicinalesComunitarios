package ec.edu.epn.huertos.huertos;
public enum CualidadPlanta {
CALIENTE("Caliente"), FRIA("Fría"), FRESCA("Fresca"), VARIABLE("Variable según el contexto");
private final String etiqueta;
CualidadPlanta(String etiqueta) { this.etiqueta = etiqueta; }
public String etiqueta() { return etiqueta; }
}
