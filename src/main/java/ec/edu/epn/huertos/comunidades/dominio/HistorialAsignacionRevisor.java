package ec.edu.epn.huertos.comunidades.dominio;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "historial_asignacion_revisor")
public class HistorialAsignacionRevisor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "comunidad_id", nullable = false) private Long comunidadId;
    @Column(name = "revisor_anterior_id") private Long revisorAnteriorId;
    @Column(name = "revisor_nuevo_id", nullable = false) private Long revisorNuevoId;
    @Column(name = "actor_tecnico_id", nullable = false) private Long actorTecnicoId;
    @Column(nullable = false, columnDefinition = "text") private String motivo;
    @Column(name = "cambiada_en", nullable = false) private Instant cambiadaEn = Instant.now();
    protected HistorialAsignacionRevisor() { }
    public HistorialAsignacionRevisor(Long comunidadId, Long anterior, Long nuevo, Long actor, String motivo) {
        this.comunidadId = comunidadId; this.revisorAnteriorId = anterior; this.revisorNuevoId = nuevo;
        this.actorTecnicoId = actor; this.motivo = motivo;
    }
}
