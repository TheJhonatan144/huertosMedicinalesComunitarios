package ec.edu.epn.huertos.evidencias;

import ec.edu.epn.huertos.compartido.ConflictoException;
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

/** Un envío de fotografía de una actividad. Los envíos rechazados se conservan como historial. */
@Entity
@Table(name = "envio_evidencia")
public class EnvioEvidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actividad_id", nullable = false, updatable = false)
    private Long actividadId;

    @Column(nullable = false, updatable = false)
    private int numero;

    @Column(name = "clave_almacenamiento", nullable = false, updatable = false)
    private String claveAlmacenamiento;

    @Column(name = "nombre_original", nullable = false, updatable = false)
    private String nombreOriginal;

    @Column(name = "tipo_contenido", nullable = false, updatable = false)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false, updatable = false)
    private long tamanoBytes;

    @Column(nullable = false, updatable = false, length = 64)
    private String sha256;

    @Column(updatable = false)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRevision estado = EstadoRevision.PENDIENTE;

    @Column(name = "enviado_en", nullable = false, updatable = false)
    private Instant enviadoEn = Instant.now();

    @Version
    private long version;

    protected EnvioEvidencia() {
    }

    public EnvioEvidencia(Long actividadId, int numero, String claveAlmacenamiento, ImagenValidada imagen, String comentario) {
        this.actividadId = actividadId;
        this.numero = numero;
        this.claveAlmacenamiento = claveAlmacenamiento;
        this.nombreOriginal = imagen.nombreOriginal();
        this.tipoContenido = imagen.tipoContenido();
        this.tamanoBytes = imagen.tamano();
        this.sha256 = imagen.sha256();
        this.comentario = comentario == null || comentario.isBlank() ? null : comentario.trim();
    }

    public void aplicarDecision(EstadoRevision decision) {
        if (estado != EstadoRevision.PENDIENTE || decision == EstadoRevision.PENDIENTE) {
            throw new ConflictoException("Este envío ya fue revisado.");
        }
        this.estado = decision;
    }

    public Long getId() {
        return id;
    }

    public Long getActividadId() {
        return actividadId;
    }

    public int getNumero() {
        return numero;
    }

    public String getClaveAlmacenamiento() {
        return claveAlmacenamiento;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public String getComentario() {
        return comentario;
    }

    public EstadoRevision getEstado() {
        return estado;
    }

    public Instant getEnviadoEn() {
        return enviadoEn;
    }
}
