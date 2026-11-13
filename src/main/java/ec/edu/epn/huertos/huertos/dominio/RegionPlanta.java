package ec.edu.epn.huertos.huertos.dominio;
public enum RegionPlanta {
COSTA("Costa"), SIERRA("Sierra"), AMAZONIA("Amazonía"), INSULAR("Insular");
private final String etiqueta;
RegionPlanta(String etiqueta) { this.etiqueta = etiqueta; }
public String etiqueta() { return etiqueta; }
}
