package ec.edu.epn.huertos.misiones.dominio;

import ec.edu.epn.huertos.actividades.dominio.TipoActividad;
import jakarta.persistence.*;

@Entity
@Table(name = "mision_plantilla")
public class MisionPlantilla {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;
    @Column(nullable = false)
    private int orden;
    @Column(nullable = false, length = 140)
    private String titulo;
    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_actividad", nullable = false, length = 30)
    private TipoActividad tipoActividad;
    @Column(name = "puntos_individuales", nullable = false)
    private int puntosIndividuales;
    @Column(name = "puntos_comunitarios", nullable = false)
    private int puntosComunitarios;
    @Column(nullable = false)
    private int progreso;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private RecurrenciaMision recurrencia;
    @Column(nullable = false)
    private boolean activa;
    protected MisionPlantilla() {}
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public int getOrden() { return orden; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public TipoActividad getTipoActividad() { return tipoActividad; }
    public int getPuntosIndividuales() { return puntosIndividuales; }
    public int getPuntosComunitarios() { return puntosComunitarios; }
    public int getProgreso() { return progreso; }
    public RecurrenciaMision getRecurrencia() { return recurrencia; }
}
