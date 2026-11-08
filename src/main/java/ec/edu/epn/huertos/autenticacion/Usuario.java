package ec.edu.epn.huertos.autenticacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "correo", length = 254)
    private String correo;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 40)
    private String genero;

    @Column(name = "talentos_certificados", columnDefinition = "text")
    private String talentosCertificados;

    @Column(name = "nivel_experiencia", length = 20)
    private String nivelExperiencia;

    @Column(name = "clave_hash", nullable = false)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String nombreCompleto, String claveHash, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.claveHash = claveHash;
        this.rol = rol;
    }

    public Usuario(String nombreUsuario, String nombreCompleto, String claveHash, Rol rol, String correo,
                   String telefono, LocalDate fechaNacimiento, String genero, String talentosCertificados,
                   String nivelExperiencia) {
        this(nombreUsuario, nombreCompleto, claveHash, rol);
        this.correo = correo;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.talentosCertificados = talentosCertificados;
        this.nivelExperiencia = nivelExperiencia;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getGenero() { return genero; }
    public String getTalentosCertificados() { return talentosCertificados; }
    public String getNivelExperiencia() { return nivelExperiencia; }

    public String getClaveHash() {
        return claveHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void cambiarRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
