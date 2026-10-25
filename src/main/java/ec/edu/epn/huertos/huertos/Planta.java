package ec.edu.epn.huertos.huertos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Planta registrada en un huerto. La descripción es texto libre del gestor, no una recomendación clínica. */
@Entity
@Table(name = "planta")
public class Planta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "huerto_id", nullable = false)
    private Long huertoId;

    @Column(name = "nombre_comun", nullable = false)
    private String nombreComun;

    @Column(name = "nombre_cientifico")
    private String nombreCientifico;

    private String descripcion;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    protected Planta() {
    }

    public Planta(Long huertoId, String nombreComun, String nombreCientifico, String descripcion, int cantidad) {
        this.huertoId = huertoId;
        actualizar(nombreComun, nombreCientifico, descripcion, cantidad);
    }

    public void actualizar(String nombreComun, String nombreCientifico, String descripcion, int cantidad) {
        this.nombreComun = nombreComun;
        this.nombreCientifico = vacioANulo(nombreCientifico);
        this.descripcion = vacioANulo(descripcion);
        this.cantidad = cantidad;
    }

    public void cambiarActiva(boolean activa) {
        this.activa = activa;
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getHuertoId() {
        return huertoId;
    }

    public String getNombreComun() {
        return nombreComun;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isActiva() {
        return activa;
    }
}
