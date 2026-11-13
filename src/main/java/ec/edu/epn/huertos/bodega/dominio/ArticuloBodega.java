package ec.edu.epn.huertos.bodega.dominio;

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

    public ArticuloBodega(Long huertoId, String nombre, CategoriaArticulo categoria, int cantidad) {
        this.huertoId = huertoId;
        actualizar(nombre, categoria, cantidad);
    }

    public void actualizar(String nombre, CategoriaArticulo categoria, int cantidad) {
        this.nombre = nombre.trim();
        this.categoria = categoria;
        this.cantidad = cantidad;
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
