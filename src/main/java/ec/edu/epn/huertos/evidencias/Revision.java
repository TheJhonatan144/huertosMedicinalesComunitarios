package ec.edu.epn.huertos.evidencias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Decisión humana sobre un envío. Inmutable: la base de datos rechaza UPDATE y DELETE. */
@Entity
@Table(name = "revision")
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "envio_id", nullable = false, unique = true, updatable = false)
    private Long envioId;

    @Column(name = "revisor_id", nullable = false, updatable = false)
    private Long revisorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EstadoRevision decision;

    @Column(updatable = false)
    private String observaciones;

    @Column(name = "revisada_en", nullable = false, updatable = false)
    private Instant revisadaEn = Instant.now();

    protected Revision() {
    }

    public Revision(Long envioId, Long revisorId, EstadoRevision decision, String observaciones) {
        this.envioId = envioId;
        this.revisorId = revisorId;
        this.decision = decision;
        this.observaciones = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getEnvioId() {
        return envioId;
    }

    public Long getRevisorId() {
        return revisorId;
    }

    public EstadoRevision getDecision() {
        return decision;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Instant getRevisadaEn() {
        return revisadaEn;
    }
}
