package ec.edu.epn.huertos.huertos.aplicacion;
import ec.edu.epn.huertos.huertos.dominio.CicloPlanta;
import ec.edu.epn.huertos.huertos.dominio.CualidadPlanta;
import ec.edu.epn.huertos.huertos.dominio.Especie;
import ec.edu.epn.huertos.huertos.dominio.RegionPlanta;
import jakarta.validation.constraints.*;
import java.util.*;

public class FichaEspecie {
    @NotNull private Long version = 0L;
    private Set<RegionPlanta> regiones = new HashSet<>();
    @Size(max = 600) private String clima;
    @NotBlank(message = "Indica el nombre común.")
    @Size(max = 100)
    private String nombreComun;
    @Size(max = 120)
    private String nombreCientifico;
    @Size(max = 250)
    private String otrosNombres;
    @Size(max = 120)
    private String familiaBotanica;
    @Min(0) @Max(6500)
    private Integer altitudMinima;
    @Min(0) @Max(6500)
    private Integer altitudMaxima;
    @Size(max = 1000)
    private String observacionesAltitud;
    @Size(max = 600)
    private String provincias;
    private CicloPlanta ciclo;
    @Size(max = 1000)
    private String observacionesCiclo;
    private CualidadPlanta cualidad;
    @Size(max = 1500)
    private String contextoCualidad;
    @Size(max = 3000)
    private String usosMedicinales;
    @Size(max = 3000)
    private String saberes;
    @Size(max = 2000)
    private String fuenteDocumental;
    @Size(max = 1000)
    private String fotoFuente;
    public static FichaEspecie de(Especie e) {
        var f = new FichaEspecie(); f.version = e.getVersion(); f.regiones = new HashSet<>(e.getRegiones()); f.clima = e.getClima();
        f.nombreComun = e.getNombreComun();
        f.nombreCientifico = e.getNombreCientifico();
        f.otrosNombres = e.getOtrosNombres();
        f.familiaBotanica = e.getFamiliaBotanica();
        f.altitudMinima = e.getAltitudMinima();
        f.altitudMaxima = e.getAltitudMaxima();
        f.observacionesAltitud = e.getObservacionesAltitud();
        f.provincias = e.getProvincias();
        f.ciclo = e.getCiclo();
        f.observacionesCiclo = e.getObservacionesCiclo();
        f.cualidad = e.getCualidad();
        f.contextoCualidad = e.getContextoCualidad();
        f.usosMedicinales = e.getUsosMedicinales();
        f.saberes = e.getSaberes();
        f.fuenteDocumental = e.getFuenteDocumental();
        f.fotoFuente = e.getFotoFuente();
        return f;
    }
    @AssertTrue(message = "La altitud mínima no puede superar la máxima.")
    public boolean isAltitudValida() { return altitudMinima == null || altitudMaxima == null || altitudMinima <= altitudMaxima; }
    @AssertTrue(message = "Registra una fuente para los usos, saberes y cualidad tradicional.")
    public boolean isFuenteValida() {
        return ((usosMedicinales == null || usosMedicinales.isBlank()) && (saberes == null || saberes.isBlank()) && cualidad == null
            && (contextoCualidad == null || contextoCualidad.isBlank()))
            || (fuenteDocumental != null && !fuenteDocumental.isBlank());
    }
    @AssertTrue(message = "Explica las clasificaciones tradicionales que varían según el contexto.")
    public boolean isContextoCualidadValido() {
        return cualidad != CualidadPlanta.VARIABLE || (contextoCualidad != null && !contextoCualidad.isBlank());
    }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Set<RegionPlanta> getRegiones() { return regiones; }
    public void setRegiones(Set<RegionPlanta> regiones) { this.regiones = regiones == null ? new HashSet<>() : regiones; }
    public String getClima() { return clima; }
    public void setClima(String clima) { this.clima = clima; }
    public String getNombreComun() { return nombreComun; }
    public void setNombreComun(String valor) { nombreComun = valor == null ? null : valor.trim(); }
    public String getNombreCientifico() { return nombreCientifico; }
    public void setNombreCientifico(String valor) { nombreCientifico = valor == null ? null : valor.trim(); }
    public String getOtrosNombres() { return otrosNombres; }
    public void setOtrosNombres(String valor) { otrosNombres = valor == null ? null : valor.trim(); }
    public String getFamiliaBotanica() { return familiaBotanica; }
    public void setFamiliaBotanica(String valor) { familiaBotanica = valor == null ? null : valor.trim(); }
    public Integer getAltitudMinima() { return altitudMinima; }
    public void setAltitudMinima(Integer valor) { altitudMinima = valor; }
    public Integer getAltitudMaxima() { return altitudMaxima; }
    public void setAltitudMaxima(Integer valor) { altitudMaxima = valor; }
    public String getObservacionesAltitud() { return observacionesAltitud; }
    public void setObservacionesAltitud(String valor) { observacionesAltitud = valor == null ? null : valor.trim(); }
    public String getProvincias() { return provincias; }
    public void setProvincias(String valor) { provincias = valor == null ? null : valor.trim(); }
    public CicloPlanta getCiclo() { return ciclo; }
    public void setCiclo(CicloPlanta valor) { ciclo = valor; }
    public String getObservacionesCiclo() { return observacionesCiclo; }
    public void setObservacionesCiclo(String valor) { observacionesCiclo = valor == null ? null : valor.trim(); }
    public CualidadPlanta getCualidad() { return cualidad; }
    public void setCualidad(CualidadPlanta valor) { cualidad = valor; }
    public String getContextoCualidad() { return contextoCualidad; }
    public void setContextoCualidad(String valor) { contextoCualidad = valor == null ? null : valor.trim(); }
    public String getUsosMedicinales() { return usosMedicinales; }
    public void setUsosMedicinales(String valor) { usosMedicinales = valor == null ? null : valor.trim(); }
    public String getSaberes() { return saberes; }
    public void setSaberes(String valor) { saberes = valor == null ? null : valor.trim(); }
    public String getFuenteDocumental() { return fuenteDocumental; }
    public void setFuenteDocumental(String valor) { fuenteDocumental = valor == null ? null : valor.trim(); }
    public String getFotoFuente() { return fotoFuente; }
    public void setFotoFuente(String valor) { fotoFuente = valor == null ? null : valor.trim(); }

    public ec.edu.epn.huertos.huertos.dominio.DatosEspecie datosDominio() {
        return new ec.edu.epn.huertos.huertos.dominio.DatosEspecie(getNombreComun(), getNombreCientifico(), getOtrosNombres(), getFamiliaBotanica(), getAltitudMinima(), getAltitudMaxima(), getObservacionesAltitud(), getProvincias(), getCiclo(), getObservacionesCiclo(), getCualidad(), getContextoCualidad(), getUsosMedicinales(), getSaberes(), getFuenteDocumental(), getFotoFuente(), getClima(), getRegiones());
    }

    public static FichaEspecie de(EspecieVista e) {
        var f = new FichaEspecie(); f.version = e.getVersion(); f.regiones = new HashSet<>(e.getRegiones()); f.clima = e.getClima();
        f.nombreComun = e.getNombreComun();
        f.nombreCientifico = e.getNombreCientifico();
        f.otrosNombres = e.getOtrosNombres();
        f.familiaBotanica = e.getFamiliaBotanica();
        f.altitudMinima = e.getAltitudMinima();
        f.altitudMaxima = e.getAltitudMaxima();
        f.observacionesAltitud = e.getObservacionesAltitud();
        f.provincias = e.getProvincias();
        f.ciclo = e.getCiclo();
        f.observacionesCiclo = e.getObservacionesCiclo();
        f.cualidad = e.getCualidad();
        f.contextoCualidad = e.getContextoCualidad();
        f.usosMedicinales = e.getUsosMedicinales();
        f.saberes = e.getSaberes();
        f.fuenteDocumental = e.getFuenteDocumental();
        f.fotoFuente = e.getFotoFuente();
        return f;
    }
}
