package ec.edu.epn.huertos.huertos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "huerto")
public class Huerto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comunidad_id", nullable = false, unique = true)
    private Long comunidadId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false, length = 60)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String parroquia;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Column(name = "area_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaM2;

    @Column(nullable = false, length = 40)
    private String diseno;

    @Column(name = "tipo_suelo", nullable = false, length = 20)
    private String tipoSuelo;

    @Column(name = "fuente_agua", nullable = false, length = 120)
    private String fuenteAgua;

    @Column(name = "abono_natural", nullable = false, length = 120)
    private String abonoNatural;

    @Column(length = 40)
    private String cerramiento;

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected Huerto() {
    }

    public Huerto(Long comunidadId, String nombre, String descripcion, double latitud, double longitud) {
        this(comunidadId, nombre, descripcion, "", "", "", BigDecimal.ONE,
                "TIERRA_SURCOS", "LIMOSO", "No especificada", "No especificado", "NATURAL",
                latitud, longitud);
    }

    public Huerto(Long comunidadId, String nombre, String descripcion, String provincia, String parroquia,
                  String direccion, BigDecimal areaM2, String diseno, String tipoSuelo, String fuenteAgua,
                  String abonoNatural, String cerramiento, double latitud, double longitud) {
        this.comunidadId = comunidadId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.provincia = provincia;
        this.parroquia = parroquia;
        this.direccion = direccion;
        this.areaM2 = areaM2;
        this.diseno = diseno;
        this.tipoSuelo = tipoSuelo;
        this.fuenteAgua = fuenteAgua;
        this.abonoNatural = abonoNatural;
        this.cerramiento = cerramiento;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public void actualizar(String nombre, String descripcion, String provincia, String parroquia, String direccion,
                           BigDecimal areaM2, String diseno, String tipoSuelo, String fuenteAgua,
                           String abonoNatural, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.provincia = provincia;
        this.parroquia = parroquia;
        this.direccion = direccion;
        this.areaM2 = areaM2;
        this.diseno = diseno;
        this.tipoSuelo = tipoSuelo;
        this.fuenteAgua = fuenteAgua;
        this.abonoNatural = abonoNatural;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Long getId() {
        return id;
    }

    public Long getComunidadId() {
        return comunidadId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getProvincia() { return provincia; }
    public String getParroquia() { return parroquia; }
    public String getDireccion() { return direccion; }
    public BigDecimal getAreaM2() { return areaM2; }
    public String getDiseno() { return diseno; }
    public String getTipoSuelo() { return tipoSuelo; }
    public String getFuenteAgua() { return fuenteAgua; }
    public String getAbonoNatural() { return abonoNatural; }
    public String getCerramiento() { return cerramiento; }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }
}
