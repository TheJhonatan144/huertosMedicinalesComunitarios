package ec.edu.epn.huertos.comunidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "invitacion_comunidad")
public class InvitacionComunidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "comunidad_id", nullable = false) private Long comunidadId;
    @Column(name = "invitada_por", nullable = false) private Long invitadaPor;
    @Column(name = "usuario_destino_id", nullable = false) private Long usuarioDestinoId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoInvitacion tipo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;
    @Column(name = "creada_en", nullable = false) private Instant creadaEn = Instant.now();
    @Column(name = "respondida_en") private Instant respondidaEn;

    protected InvitacionComunidad() { }
    public InvitacionComunidad(Long comunidadId, Long invitadaPor, Long usuarioDestinoId, TipoInvitacion tipo) {
        this.comunidadId = comunidadId; this.invitadaPor = invitadaPor;
        this.usuarioDestinoId = usuarioDestinoId; this.tipo = tipo;
    }
    public void responder(EstadoInvitacion estado) { this.estado = estado; this.respondidaEn = Instant.now(); }
    public Long getId() { return id; }
    public Long getComunidadId() { return comunidadId; }
    public Long getInvitadaPor() { return invitadaPor; }
    public Long getUsuarioDestinoId() { return usuarioDestinoId; }
    public TipoInvitacion getTipo() { return tipo; }
    public EstadoInvitacion getEstado() { return estado; }
    public Instant getCreadaEn() { return creadaEn; }
}
