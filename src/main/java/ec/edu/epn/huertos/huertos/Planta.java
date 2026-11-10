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

    @Column(nullable = false)
    private boolean plantada = false;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.EAGER, cascade = jakarta.persistence.CascadeType.PERSIST)
    @jakarta.persistence.JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    protected Planta() {
    }

    public Planta(Long huertoId, String nombreComun, String nombreCientifico, String descripcion, int cantidad) {
        this.huertoId = huertoId;
        this.especie = new Especie(nombreComun, nombreCientifico);
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

    public void cambiarPlantada(boolean plantada) { this.plantada = plantada; }

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
        return especie == null ? nombreComun : especie.getNombreComun();
    }

    public String getNombreCientifico() {
        return especie == null ? nombreCientifico : especie.getNombreCientifico();
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

    public boolean isPlantada() { return plantada; }

    /** Siembra desde el catálogo: la planta queda ligada a una especie común. */
    public Planta(Long huertoId, Especie especie, int cantidad, boolean plantada) {
        this.huertoId = huertoId; this.especie = especie;
        actualizar(especie.getNombreComun(), especie.getNombreCientifico(), null, cantidad);
        this.plantada = plantada;
    }

    public Especie getEspecie() { return especie; }

    /** Cambia cantidad y plantada; no toca «activa» (son independientes). */
    public void cambiarSiembra(int cantidad, boolean plantada) { this.cantidad = cantidad; this.plantada = plantada; }
}
