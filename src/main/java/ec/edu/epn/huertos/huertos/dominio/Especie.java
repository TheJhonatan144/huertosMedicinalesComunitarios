package ec.edu.epn.huertos.huertos.dominio;
import jakarta.persistence.*;
import java.util.*;

@Entity @Table(name = "especie")
public class Especie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private long version;
    @Column(name = "nombre_comun") private String nombreComun;
    @Column(name = "nombre_cientifico") private String nombreCientifico;
    @Column(name = "otros_nombres") private String otrosNombres;
    @Column(name = "familia_botanica") private String familiaBotanica;
    @Column(name = "altitud_minima") private Integer altitudMinima;
    @Column(name = "altitud_maxima") private Integer altitudMaxima;
    @Column(name = "observaciones_altitud", length = 1000) private String observacionesAltitud;
    @Column(name = "provincias") private String provincias;
    @Enumerated(EnumType.STRING)
    @Column(name = "ciclo") private CicloPlanta ciclo;
    @Column(name = "observaciones_ciclo", length = 1000) private String observacionesCiclo;
    @Enumerated(EnumType.STRING)
    @Column(name = "cualidad") private CualidadPlanta cualidad;
    @Column(name = "contexto_cualidad", length = 1500) private String contextoCualidad;
    @Column(name = "usos_medicinales") private String usosMedicinales;
    @Column(name = "saberes") private String saberes;
    @Column(name = "fuente_documental") private String fuenteDocumental;
    @Column(name = "foto_fuente") private String fotoFuente;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "especie_region", joinColumns = @JoinColumn(name = "especie_id"))
    @Enumerated(EnumType.STRING) @Column(name = "region")
    private Set<RegionPlanta> regiones = new HashSet<>();
    @Column(name = "clima") private String clima;
    @Column(name = "foto_clave") private String fotoClave;
    @Column(name = "foto_tipo") private String fotoTipo;
    protected Especie() {}
    public Especie(String nombre, String cientifico) { nombreComun = nombre; nombreCientifico = cientifico; }
    public void actualizar(DatosEspecie f) {
        nombreComun = f.getNombreComun();
        nombreCientifico = f.getNombreCientifico();
        otrosNombres = f.getOtrosNombres();
        familiaBotanica = f.getFamiliaBotanica();
        altitudMinima = f.getAltitudMinima();
        altitudMaxima = f.getAltitudMaxima();
        observacionesAltitud = f.getObservacionesAltitud();
        provincias = f.getProvincias();
        ciclo = f.getCiclo();
        observacionesCiclo = f.getObservacionesCiclo();
        cualidad = f.getCualidad();
        contextoCualidad = f.getContextoCualidad();
        usosMedicinales = f.getUsosMedicinales();
        saberes = f.getSaberes();
        fuenteDocumental = f.getFuenteDocumental();
        fotoFuente = f.getFotoFuente();
        regiones.clear(); regiones.addAll(f.getRegiones()); clima = f.getClima();
    }
    public void cambiarNombre(String nombre, String cientifico) { nombreComun = nombre; nombreCientifico = cientifico; }
    public void cambiarFoto(String clave, String tipo) { fotoClave = clave; fotoTipo = tipo; }
    public Long getId() { return id; }
    public long getVersion() { return version; }
    public Set<RegionPlanta> getRegiones() { return Set.copyOf(regiones); }
    public String getClima() { return clima; }
    public String getFotoClave() { return fotoClave; }
    public String getFotoTipo() { return fotoTipo; }
    public String getNombreComun() { return nombreComun; }
    public String getNombreCientifico() { return nombreCientifico; }
    public String getOtrosNombres() { return otrosNombres; }
    public String getFamiliaBotanica() { return familiaBotanica; }
    public Integer getAltitudMinima() { return altitudMinima; }
    public Integer getAltitudMaxima() { return altitudMaxima; }
    public String getObservacionesAltitud() { return observacionesAltitud; }
    public String getProvincias() { return provincias; }
    public CicloPlanta getCiclo() { return ciclo; }
    public String getObservacionesCiclo() { return observacionesCiclo; }
    public CualidadPlanta getCualidad() { return cualidad; }
    public String getContextoCualidad() { return contextoCualidad; }
    public String getUsosMedicinales() { return usosMedicinales; }
    public String getSaberes() { return saberes; }
    public String getFuenteDocumental() { return fuenteDocumental; }
    public String getFotoFuente() { return fotoFuente; }
}
