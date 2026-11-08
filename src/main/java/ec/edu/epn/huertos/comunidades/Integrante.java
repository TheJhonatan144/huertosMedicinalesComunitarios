package ec.edu.epn.huertos.comunidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** Pertenencia de un usuario a una comunidad. El cupo según el tipo lo garantiza también la base de datos. */
@Entity
@Table(name = "integrante")
public class Integrante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comunidad_id", nullable = false)
    private Long comunidadId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "es_gestor", nullable = false)
    private boolean esGestor;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 40)
    private String genero;

    @Column(name = "talentos_certificados", columnDefinition = "text")
    private String talentosCertificados;

    @Column(name = "nivel_experiencia", nullable = false, length = 20)
    private String nivelExperiencia = "APRENDIZ";

    @Column(name = "alta_en", nullable = false)
    private Instant altaEn = Instant.now();

    @Column(name = "baja_en")
    private Instant bajaEn;

    protected Integrante() {
    }

    public Integrante(Long comunidadId, Long usuarioId, boolean esGestor) {
        this.comunidadId = comunidadId;
        this.usuarioId = usuarioId;
        this.esGestor = esGestor;
    }

    public Integrante(Long comunidadId, Long usuarioId, boolean esGestor, LocalDate fechaNacimiento,
                      String genero, String talentosCertificados, String nivelExperiencia) {
        this(comunidadId, usuarioId, esGestor);
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.talentosCertificados = talentosCertificados;
        this.nivelExperiencia = nivelExperiencia;
    }

    public void darDeBaja() {
        this.activo = false;
        this.esGestor = false;
        this.bajaEn = Instant.now();
    }

    public void asumirGestion() { this.esGestor = true; }

    public Long getId() {
        return id;
    }

    public Long getComunidadId() {
        return comunidadId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public boolean isEsGestor() {
        return esGestor;
    }

    public boolean isActivo() {
        return activo;
    }
}
