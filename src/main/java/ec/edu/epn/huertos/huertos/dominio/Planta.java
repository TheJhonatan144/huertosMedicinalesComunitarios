package ec.edu.epn.huertos.huertos.dominio;

import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Planta registrada en un huerto. La descripción es texto libre del gestor, no una recomendación
 * clínica.
 */
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

    @Column(name = "especie_id", nullable = false)
    private Long especieId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision", nullable = false)
    private EstadoRevision estadoRevision = EstadoRevision.APROBADA;

    @Column(name = "plantada_en")
    private Instant plantadaEn;

    @Column(name = "observaciones_revision")
    private String observacionesRevision;

    protected Planta() {}

    public Planta(
            Long huertoId,
            String nombreComun,
            String nombreCientifico,
            String descripcion,
            int cantidad) {
        this.huertoId = huertoId;
        actualizar(nombreComun, nombreCientifico, descripcion, cantidad);
    }

    public void actualizar(
            String nombreComun, String nombreCientifico, String descripcion, int cantidad) {
        this.nombreComun = nombreComun;
        this.nombreCientifico = vacioANulo(nombreCientifico);
        this.descripcion = vacioANulo(descripcion);
        this.cantidad = cantidad;
    }

    public void cambiarActiva(boolean activa) {
        this.activa = activa;
    }

    public void cambiarPlantada(boolean plantada) {

        if (plantada && !this.plantada) {
            this.plantadaEn = Instant.now();
            this.estadoRevision = EstadoRevision.PENDIENTE;
            this.observacionesRevision = null;
        }

        this.plantada = plantada;
    }

    public void aplicarRevision(EstadoRevision decision, String observaciones) {

        if (!this.plantada
                || this.estadoRevision != EstadoRevision.PENDIENTE
                || (decision != EstadoRevision.APROBADA && decision != EstadoRevision.RECHAZADA)) {

            throw new ec.edu.epn.huertos.compartido.dominio.ConflictoException(
                    "La siembra ya fue revisada o no está pendiente.");
        }

        this.estadoRevision = decision;

        this.observacionesRevision =
                observaciones == null || observaciones.isBlank() ? null : observaciones.trim();

        if (decision == EstadoRevision.RECHAZADA) {
            this.plantada = false;
        }
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

    public boolean isPlantada() {
        return plantada;
    }

    /** Siembra desde el catálogo: la planta queda ligada a una especie común. */
    public Planta(
            Long huertoId,
            Long especieId,
            String nombreComun,
            String nombreCientifico,
            int cantidad,
            boolean plantada) {
        this.huertoId = huertoId;
        this.especieId = especieId;
        actualizar(nombreComun, nombreCientifico, null, cantidad);
        cambiarPlantada(plantada);
    }

    public Long getEspecieId() {
        return especieId;
    }

    /** Cambia cantidad y plantada; no toca «activa» (son independientes). */
    public void cambiarSiembra(int cantidad, boolean plantada) {
        this.cantidad = cantidad;
        cambiarPlantada(plantada);
    }

    public EstadoRevision getEstadoRevision() {
        return estadoRevision;
    }

    public Instant getPlantadaEn() {
        return plantadaEn;
    }

    public String getObservacionesRevision() {
        return observacionesRevision;
    }
}
