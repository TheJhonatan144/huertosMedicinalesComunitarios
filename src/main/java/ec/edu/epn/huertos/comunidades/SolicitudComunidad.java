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
@Table(name = "solicitud_comunidad")
public class SolicitudComunidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSolicitudComunidad tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudComunidad estado = EstadoSolicitudComunidad.PENDIENTE;

    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;

    @Column(name = "comunidad_id")
    private Long comunidadId;

    @Column(name = "nombre_comunidad", nullable = false, length = 120)
    private String nombreComunidad;

    @Column(name = "datos_json", columnDefinition = "text")
    private String datosJson;

    @Column(name = "motivo_solicitud", columnDefinition = "text")
    private String motivoSolicitud;

    @Column(name = "motivo_rechazo", columnDefinition = "text")
    private String motivoRechazo;

    @Column(name = "revisada_por")
    private Long revisadaPor;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    @Column(name = "revisada_en")
    private Instant revisadaEn;

    protected SolicitudComunidad() { }

    private SolicitudComunidad(TipoSolicitudComunidad tipo, Long solicitanteId, Long comunidadId,
                               String nombreComunidad, String datosJson, String motivoSolicitud) {
        this.tipo = tipo;
        this.solicitanteId = solicitanteId;
        this.comunidadId = comunidadId;
        this.nombreComunidad = nombreComunidad;
        this.datosJson = datosJson;
        this.motivoSolicitud = motivoSolicitud;
    }

    public static SolicitudComunidad crear(Long solicitanteId, String nombreComunidad, String datosJson) {
        return new SolicitudComunidad(TipoSolicitudComunidad.CREAR, solicitanteId, null,
                nombreComunidad, datosJson, null);
    }

    public static SolicitudComunidad eliminar(Long solicitanteId, Long comunidadId, String nombreComunidad,
                                               String motivoSolicitud) {
        return new SolicitudComunidad(TipoSolicitudComunidad.ELIMINAR, solicitanteId, comunidadId,
                nombreComunidad, null, motivoSolicitud);
    }

    public void aprobar(Long revisorId) {
        exigirPendiente();
        estado = EstadoSolicitudComunidad.APROBADA;
        revisadaPor = revisorId;
        revisadaEn = Instant.now();
    }

    public void rechazar(Long revisorId, String motivo) {
        exigirPendiente();
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Indica el motivo del rechazo.");
        estado = EstadoSolicitudComunidad.RECHAZADA;
        revisadaPor = revisorId;
        motivoRechazo = motivo.trim();
        revisadaEn = Instant.now();
    }

    public void limpiarDatosSolicitud() { datosJson = null; }

    private void exigirPendiente() {
        if (estado != EstadoSolicitudComunidad.PENDIENTE)
            throw new IllegalStateException("La solicitud ya fue resuelta.");
    }

    public Long getId() { return id; }
    public TipoSolicitudComunidad getTipo() { return tipo; }
    public EstadoSolicitudComunidad getEstado() { return estado; }
    public Long getSolicitanteId() { return solicitanteId; }
    public Long getComunidadId() { return comunidadId; }
    public String getNombreComunidad() { return nombreComunidad; }
    public String getDatosJson() { return datosJson; }
    public String getMotivoSolicitud() { return motivoSolicitud; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public Long getRevisadaPor() { return revisadaPor; }
    public Instant getCreadaEn() { return creadaEn; }
    public Instant getRevisadaEn() { return revisadaEn; }
}
