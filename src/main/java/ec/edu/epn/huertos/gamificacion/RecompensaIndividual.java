package ec.edu.epn.huertos.gamificacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Movimiento de puntos individuales; UNIQUE (actividad, integrante) impide duplicarlo. */
@Entity
@Table(name = "recompensa_individual")
public class RecompensaIndividual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actividad_id", nullable = false, updatable = false)
    private Long actividadId;

    @Column(name = "integrante_id", nullable = false, updatable = false)
    private Long integranteId;

    @Column(name = "comunidad_id", nullable = false, updatable = false)
    private Long comunidadId;

    @Column(nullable = false, updatable = false)
    private int puntos;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private Instant creadaEn = Instant.now();

    protected RecompensaIndividual() {
    }

    public RecompensaIndividual(Long actividadId, Long integranteId, Long comunidadId, int puntos) {
        this.actividadId = actividadId;
        this.integranteId = integranteId;
        this.comunidadId = comunidadId;
        this.puntos = puntos;
    }

    public Long getActividadId() {
        return actividadId;
    }

    public Long getIntegranteId() {
        return integranteId;
    }

    public int getPuntos() {
        return puntos;
    }
}
