package ec.edu.epn.huertos.bodega;

import jakarta.persistence.*;

@Entity
@Table(name = "articulo_bodega")
public class ArticuloBodega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "huerto_id", nullable = false)
    private Long huertoId;
    private String nombre;
    @Enumerated(EnumType.STRING)
    private CategoriaArticulo categoria;
    private int cantidad;

    protected ArticuloBodega() {
    }

    public ArticuloBodega(Long huertoId, FormularioArticulo f) {
        this.huertoId = huertoId;
        actualizar(f);
    }

    public void actualizar(FormularioArticulo f) {
        nombre = f.getNombre().trim();
        categoria = f.getCategoria();
        cantidad = f.getCantidad();
    }

    public Long getId() {
        return id;
    }

    public Long getHuertoId() {
        return huertoId;
    }

    public String getNombre() {
        return nombre;
    }

    public CategoriaArticulo getCategoria() {
        return categoria;
    }

    public int getCantidad() {
        return cantidad;
    }
}
