package ec.edu.epn.huertos.gamificacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Puntos comunitarios y progreso del huerto por una actividad aprobada; UNIQUE (actividad) impide duplicarlo. */
@Entity
@Table(name = "recompensa_comunitaria")
public class RecompensaComunitaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actividad_id", nullable = false, unique = true, updatable = false)
    private Long actividadId;

    @Column(name = "comunidad_id", nullable = false, updatable = false)
    private Long comunidadId;

    @Column(nullable = false, updatable = false)
    private int puntos;

    @Column(nullable = false, updatable = false)
    private int progreso;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private Instant creadaEn = Instant.now();

    protected RecompensaComunitaria() {
    }

    public RecompensaComunitaria(Long actividadId, Long comunidadId, int puntos, int progreso) {
        this.actividadId = actividadId;
        this.comunidadId = comunidadId;
        this.puntos = puntos;
        this.progreso = progreso;
    }

    public Long getActividadId() {
        return actividadId;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getProgreso() {
        return progreso;
    }
}
