
package ec.edu.epn.huertos.huertos;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

import ec.edu.epn.huertos.evidencias.EstadoRevision;

/**
 * Planta registrada en un huerto.
 * La descripción es texto libre del gestor,
 * no una recomendación clínica.
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

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision", nullable = false)
    private EstadoRevision estadoRevision = EstadoRevision.APROBADA;

    @Column(name = "plantada_en")
    private Instant plantadaEn;

    @Column(name = "observaciones_revision")
    private String observacionesRevision;

    @ManyToOne(
        fetch = FetchType.EAGER,
        cascade = CascadeType.PERSIST
    )
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    protected Planta() {
    }

    /**
     * Constructor para registrar una planta.
     */
    public Planta(
            Long huertoId,
            String nombreComun,
            String nombreCientifico,
            String descripcion,
            int cantidad) {

        this.huertoId = huertoId;
        this.especie = new Especie(
                nombreComun,
                nombreCientifico
        );

        actualizar(
                nombreComun,
                nombreCientifico,
                descripcion,
                cantidad
        );
    }

    /**
     * Actualiza los datos de la planta.
     */
    public void actualizar(
            String nombreComun,
            String nombreCientifico,
            String descripcion,
            int cantidad) {

        this.nombreComun = nombreComun;
        this.nombreCientifico = vacioANulo(nombreCientifico);
        this.descripcion = vacioANulo(descripcion);
        this.cantidad = cantidad;
    }

    public void cambiarActiva(boolean activa) {
        this.activa = activa;
    }

    /**
     * Cuando se marca como plantada por primera vez,
     * comienza el tiempo de crecimiento y queda pendiente
     * de revisión.
     */
    public void cambiarPlantada(boolean plantada) {

        if (plantada && !this.plantada) {
            this.plantadaEn = Instant.now();
            this.estadoRevision = EstadoRevision.PENDIENTE;
            this.observacionesRevision = null;
        }

        this.plantada = plantada;
    }

    /**
     * Aplica la decisión del revisor.
     */
    public void aplicarRevision(
            EstadoRevision decision,
            String observaciones) {

        if (!this.plantada
                || this.estadoRevision != EstadoRevision.PENDIENTE
                || (decision != EstadoRevision.APROBADA
                && decision != EstadoRevision.RECHAZADA)) {

            throw new ec.edu.epn.huertos.compartido.ConflictoException(
                    "La siembra ya fue revisada o no está pendiente."
            );
        }

        this.estadoRevision = decision;

        this.observacionesRevision =
                observaciones == null || observaciones.isBlank()
                        ? null
                        : observaciones.trim();

        if (decision == EstadoRevision.RECHAZADA) {
            this.plantada = false;
        }
    }

    /**
     * Convierte cadenas vacías en null.
     */
    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank()
                ? null
                : valor.trim();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getHuertoId() {
        return huertoId;
    }

    public String getNombreComun() {
        return especie == null
                ? nombreComun
                : especie.getNombreComun();
    }

    public String getNombreCientifico() {
        return especie == null
                ? nombreCientifico
                : especie.getNombreCientifico();
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

    public EstadoRevision getEstadoRevision() {
        return estadoRevision;
    }

    public Instant getPlantadaEn() {
        return plantadaEn;
    }

    public String getObservacionesRevision() {
        return observacionesRevision;
    }

    public Especie getEspecie() {
        return especie;
    }

    /**
     * Constructor para sembrar desde el catálogo.
     * La planta queda ligada a una especie común.
     */
    public Planta(
            Long huertoId,
            Especie especie,
            int cantidad,
            boolean plantada) {

        this.huertoId = huertoId;
        this.especie = especie;

        actualizar(
                especie.getNombreComun(),
                especie.getNombreCientifico(),
                null,
                cantidad
        );

        this.plantada = false;

        if (plantada) {
            cambiarPlantada(true);
        }
    }

    /**
     * Cambia cantidad y estado de siembra.
     * No modifica el estado activa.
     */
    public void cambiarSiembra(
            int cantidad,
            boolean plantada) {

        this.cantidad = cantidad;
        cambiarPlantada(plantada);
    }
}
