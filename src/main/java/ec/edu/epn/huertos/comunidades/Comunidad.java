package ec.edu.epn.huertos.comunidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comunidad")
public class Comunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "tipo_comunidad", nullable = false, length = 20)
    private String tipoComunidad;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String canton;

    @Column(name = "direccion_exacta", nullable = false, length = 250)
    private String direccionExacta;

    @Column(name = "contacto_telefono", nullable = false, length = 30)
    private String contactoTelefono;

    @Column(name = "contacto_correo", nullable = false, length = 254)
    private String contactoCorreo;

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "archivada_por")
    private Long archivadaPor;

    @Column(name = "archivada_en")
    private Instant archivadaEn;

    @Column(name = "eliminacion_pendiente", nullable = false)
    private boolean eliminacionPendiente;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    protected Comunidad() {
    }

    public Comunidad(String nombre, String descripcion, String provincia, String canton, double latitud, double longitud) {
        this(nombre, descripcion, provincia, canton, "", "", latitud, longitud);
    }

    public Comunidad(String nombre, String descripcion, String provincia, String canton, String contactoTelefono,
                     String contactoCorreo, double latitud, double longitud) {
        this(nombre, descripcion, "BARRIO", provincia, canton, "", contactoTelefono, contactoCorreo, latitud, longitud);
    }

    public Comunidad(String nombre, String descripcion, String tipoComunidad, String provincia, String canton,
                     String direccionExacta, String contactoTelefono, String contactoCorreo, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoComunidad = tipoComunidad;
        this.provincia = provincia;
        this.canton = canton;
        this.direccionExacta = direccionExacta;
        this.contactoTelefono = contactoTelefono;
        this.contactoCorreo = contactoCorreo;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipoComunidad() { return tipoComunidad; }

    public String getProvincia() {
        return provincia;
    }

    public String getCanton() {
        return canton;
    }

    public String getDireccionExacta() { return direccionExacta; }

    public String getContactoTelefono() { return contactoTelefono; }
    public String getContactoCorreo() { return contactoCorreo; }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public boolean isActiva() { return activa; }
    public Long getArchivadaPor() { return archivadaPor; }
    public Instant getArchivadaEn() { return archivadaEn; }
    public boolean isEliminacionPendiente() { return eliminacionPendiente; }
    public void archivar(Long gestorUsuarioId) {
        this.activa = false;
        this.archivadaPor = gestorUsuarioId;
        this.archivadaEn = Instant.now();
    }
    public void restaurar() {
        this.activa = true;
        this.archivadaPor = null;
        this.archivadaEn = null;
    }
    public void marcarEliminacionPendiente(boolean pendiente) { this.eliminacionPendiente = pendiente; }

    public void actualizarDatos(String nombre, String descripcion, String tipoComunidad, String provincia,
                                 String canton, String direccionExacta, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoComunidad = tipoComunidad;
        this.provincia = provincia;
        this.canton = canton;
        this.direccionExacta = direccionExacta;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}
