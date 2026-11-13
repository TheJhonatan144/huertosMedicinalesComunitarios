package ec.edu.epn.huertos.huertos.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "espacio_huerto")
public class EspacioHuerto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "huerto_id", nullable = false)
    private Long huertoId;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, length = 40)
    private String diseno;
    @Column(name = "area_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaM2;
    @Column(nullable = false, length = 500)
    private String descripcion = "";
    @Column(nullable = false)
    private boolean activa = true;
    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    protected EspacioHuerto() { }

    public EspacioHuerto(Long huertoId, String nombre, String diseno, BigDecimal areaM2, String descripcion) {
        this.huertoId = huertoId;
        this.nombre = nombre;
        this.diseno = diseno;
        this.areaM2 = areaM2;
        this.descripcion = descripcion == null ? "" : descripcion;
    }

    public void actualizar(String nombre, String diseno, BigDecimal areaM2, String descripcion) {
        this.nombre = nombre;
        this.diseno = diseno;
        this.areaM2 = areaM2;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.activa = true;
    }
    public Long getId() { return id; }
    public Long getHuertoId() { return huertoId; }
    public String getNombre() { return nombre; }
    public String getDiseno() { return diseno; }
    public BigDecimal getAreaM2() { return areaM2; }
    public String getDescripcion() { return descripcion; }
    public boolean isActiva() { return activa; }
}
