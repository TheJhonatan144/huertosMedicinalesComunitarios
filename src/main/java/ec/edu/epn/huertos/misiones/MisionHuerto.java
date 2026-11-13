package ec.edu.epn.huertos.misiones;

import jakarta.persistence.*;
import ec.edu.epn.huertos.compartido.ConflictoException;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "mision_huerto", uniqueConstraints = @UniqueConstraint(columnNames = {"huerto_id", "plantilla_id"}))
public class MisionHuerto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "huerto_id", nullable = false)
    private Long huertoId;
    @Column(name = "plantilla_id", nullable = false)
    private Long plantillaId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 15)
    private EstadoMision estado;
    @Column(name = "actividad_id", unique = true)
    private Long actividadId;
    @Column(name = "disponible_desde")
    private LocalDate disponibleDesde;
    @Column(name = "completada_en")
    private Instant completadaEn;
    protected MisionHuerto() {}
    public MisionHuerto(Long huertoId, Long plantillaId, EstadoMision estado) {
        this.huertoId = huertoId; this.plantillaId = plantillaId; this.estado = estado;
    }
    public void iniciar(Long actividadId) {
        if (estado != EstadoMision.DISPONIBLE) throw new ConflictoException("La misión no está disponible.");
        this.actividadId = actividadId; this.estado = EstadoMision.EN_REVISION;
    }
    public void reenviar() {
        if (estado != EstadoMision.DISPONIBLE || actividadId == null)
            throw new ConflictoException("La misión no puede reenviarse en su estado actual.");
        this.estado = EstadoMision.EN_REVISION;
    }
    public void completar() {
        if (estado != EstadoMision.EN_REVISION) throw new ConflictoException("La misión no está en revisión.");
        this.estado = EstadoMision.COMPLETADA; this.completadaEn = Instant.now();
    }
    public void rechazar() {
        if (estado == EstadoMision.EN_REVISION) { this.estado = EstadoMision.DISPONIBLE; }
    }
    public Long getId() { return id; }
    public Long getHuertoId() { return huertoId; }
    public Long getPlantillaId() { return plantillaId; }
    public EstadoMision getEstado() { return estado; }
    public Long getActividadId() { return actividadId; }
}
