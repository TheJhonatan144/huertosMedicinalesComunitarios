package ec.edu.epn.huertos.huertos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "huerto_cerramiento", uniqueConstraints =
        @UniqueConstraint(name = "uq_huerto_cerramiento_grupo", columnNames = {"huerto_id", "grupo"}))
public class HuertoCerramiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "huerto_id", nullable = false)
    private Long huertoId;

    @Column(nullable = false, length = 20)
    private String grupo;

    protected HuertoCerramiento() { }

    public HuertoCerramiento(Long huertoId, String grupo) {
        this.huertoId = huertoId;
        this.grupo = grupo;
    }

    public Long getId() { return id; }
    public Long getHuertoId() { return huertoId; }
    public String getGrupo() { return grupo; }
}
