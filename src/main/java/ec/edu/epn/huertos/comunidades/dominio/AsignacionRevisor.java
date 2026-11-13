package ec.edu.epn.huertos.comunidades.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/** Comunidad cuyas evidencias puede revisar un administrador de evidencias. */
@Entity
@Table(name = "asignacion_revisor", uniqueConstraints = @UniqueConstraint(name = "uq_asignacion_revisor_comunidad", columnNames = "comunidad_id"))
public class AsignacionRevisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "comunidad_id", nullable = false)
    private Long comunidadId;

    @Column(name = "asignada_en", nullable = false)
    private Instant asignadaEn = Instant.now();

    protected AsignacionRevisor() {
    }

    public AsignacionRevisor(Long usuarioId, Long comunidadId) {
        this.usuarioId = usuarioId;
        this.comunidadId = comunidadId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void cambiarResponsable(Long usuarioId) { this.usuarioId = usuarioId; this.asignadaEn = Instant.now(); }

    public Long getComunidadId() {
        return comunidadId;
    }
}
