package ec.edu.epn.huertos.huertos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

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

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    /** Opcional: nulo = sin registrar (el huerto virtual usa apariencia neutra). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_suelo")
    private TipoSuelo tipoSuelo;

    /** Opcional: nulo = sin registrar. */
    @Enumerated(EnumType.STRING)
    private DisenoHuerto diseno;

    protected Huerto() {
    }

    public Huerto(Long comunidadId, String nombre, String descripcion, double latitud, double longitud) {
        this.comunidadId = comunidadId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public void actualizar(String nombre, String descripcion, TipoSuelo tipoSuelo, DisenoHuerto diseno) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoSuelo = tipoSuelo;
        this.diseno = diseno;
    }

    public TipoSuelo getTipoSuelo() {
        return tipoSuelo;
    }

    public DisenoHuerto getDiseno() {
        return diseno;
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

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }
}
