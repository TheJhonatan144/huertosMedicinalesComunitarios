package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.evidencias.EstadoRevision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "actividad")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comunidad_id", nullable = false)
    private Long comunidadId;

    @Column(name = "huerto_id", nullable = false)
    private Long huertoId;

    @Column(name = "registrada_por", nullable = false)
    private Long registradaPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoActividad tipo;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "fecha_actividad", nullable = false)
    private LocalDate fechaActividad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRevision estado = EstadoRevision.PENDIENTE;

    @Column(name = "participantes_fijados", nullable = false)
    private boolean participantesFijados;

    @Version
    private long version;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    @Column(name = "actualizada_en", nullable = false)
    private Instant actualizadaEn = Instant.now();

    protected Actividad() {
    }

    public Actividad(Long comunidadId, Long huertoId, Long registradaPor, TipoActividad tipo, String descripcion,
                     LocalDate fechaActividad) {
        this.comunidadId = comunidadId;
        this.huertoId = huertoId;
        this.registradaPor = registradaPor;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaActividad = fechaActividad;
    }

    /** Edición de los datos descriptivos; los participantes quedan fuera y no pueden cambiar. */
    public void editar(TipoActividad tipo, String descripcion, LocalDate fecha) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaActividad = fecha;
        this.actualizadaEn = Instant.now();
    }

    public void fijarParticipantes() {
        this.participantesFijados = true;
    }

    public void aplicarDecision(EstadoRevision decision) {
        if (this.estado != EstadoRevision.PENDIENTE || decision == EstadoRevision.PENDIENTE) {
            throw new ConflictoException("La actividad no está pendiente de revisión.");
        }
        this.estado = decision;
        this.actualizadaEn = Instant.now();
    }

    public void reabrirPorReenvio() {
        if (this.estado != EstadoRevision.RECHAZADA) {
            throw new ConflictoException("Solo se puede reenviar una actividad rechazada.");
        }
        this.estado = EstadoRevision.PENDIENTE;
        this.actualizadaEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getComunidadId() {
        return comunidadId;
    }

    public Long getHuertoId() {
        return huertoId;
    }

    public Long getRegistradaPor() {
        return registradaPor;
    }

    public TipoActividad getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFechaActividad() {
        return fechaActividad;
    }

    public EstadoRevision getEstado() {
        return estado;
    }

    public boolean isParticipantesFijados() {
        return participantesFijados;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }
}
