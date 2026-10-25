package ec.edu.epn.huertos.actividades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Participante seleccionado al registrar la actividad. Inmutable: la base de datos rechaza UPDATE y DELETE. */
@Entity
@Table(name = "actividad_participante")
public class ActividadParticipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actividad_id", nullable = false, updatable = false)
    private Long actividadId;

    @Column(name = "integrante_id", nullable = false, updatable = false)
    private Long integranteId;

    @Column(name = "comunidad_id", nullable = false, updatable = false)
    private Long comunidadId;

    protected ActividadParticipante() {
    }

    public ActividadParticipante(Long actividadId, Long integranteId, Long comunidadId) {
        this.actividadId = actividadId;
        this.integranteId = integranteId;
        this.comunidadId = comunidadId;
    }

    public Long getActividadId() {
        return actividadId;
    }

    public Long getIntegranteId() {
        return integranteId;
    }

    public Long getComunidadId() {
        return comunidadId;
    }
}
