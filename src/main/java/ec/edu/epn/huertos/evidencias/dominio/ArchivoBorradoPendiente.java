package ec.edu.epn.huertos.evidencias.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "archivo_borrado_pendiente")
public class ArchivoBorradoPendiente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;
    @Column(name = "clave_almacenamiento", nullable = false, unique = true, length = 200)
    private String claveAlmacenamiento;
    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();
    @Column(nullable = false)
    private int intentos;
    @Column(name = "ultimo_error", columnDefinition = "text")
    private String ultimoError;

    protected ArchivoBorradoPendiente() { }
    public ArchivoBorradoPendiente(Long solicitudId, String claveAlmacenamiento) {
        this.solicitudId = solicitudId;
        this.claveAlmacenamiento = claveAlmacenamiento;
    }
    public void fallo(String error) { intentos++; ultimoError = error; }
    public Long getId() { return id; }
    public String getClaveAlmacenamiento() { return claveAlmacenamiento; }
}
