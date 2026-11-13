package ec.edu.epn.huertos.huertos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.bodega.aplicacion.BodegaServicio;
import ec.edu.epn.huertos.bodega.aplicacion.FormularioArticulo;
import ec.edu.epn.huertos.bodega.dominio.ArticuloBodega;
import ec.edu.epn.huertos.bodega.dominio.CategoriaArticulo;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.aplicacion.SolicitudComunidadServicio;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.soporte.integracion.PruebaIntegracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

/**
 * Editar una planta no cambia la especie compartida del catálogo, y la siembra y la bodega solo se
 * modifican mientras la comunidad está operativa (ni archivada ni con eliminación pendiente).
 */
class SiembraYBodegaComunidadIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired HuertoServicio huertos;
    @Autowired CatalogoServicio catalogo;
    @Autowired BodegaServicio bodega;
    @Autowired ComunidadServicio comunidades;
    @Autowired SolicitudComunidadServicio solicitudes;

    private Long especie(Escenario e, String nombre) {
        FichaEspecie f = new FichaEspecie();
        f.setNombreComun(nombre);
        f.setNombreCientifico("Identificación sintética");
        return catalogo.guardar(e.revisor().getId(), null, f, null).getId();
    }

    private static SiembraPlanta siembra(int cantidad, boolean plantada) {
        SiembraPlanta s = new SiembraPlanta();
        s.setCantidad(cantidad);
        s.setPlantada(plantada);
        return s;
    }

    private static FormularioArticulo articulo(String nombre, int cantidad) {
        FormularioArticulo f = new FormularioArticulo();
        f.setNombre(nombre);
        f.setCategoria(CategoriaArticulo.HERRAMIENTA);
        f.setCantidad(cantidad);
        return f;
    }

    private static FormularioPlanta edicion(String nombre, String cientifico, int cantidad) {
        FormularioPlanta f = new FormularioPlanta();
        f.setNombreComun(nombre);
        f.setNombreCientifico(cientifico);
        f.setDescripcion("Nota local de la comunidad");
        f.setCantidad(cantidad);
        return f;
    }

    @Test
    void editarLaPlantaDeUnaComunidadNoRenombraLaEspecieDeLasDemas() {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        Long especieId = especie(a, "Especie compartida 7781");
        Planta pa = catalogo.seleccionar(a.gestorUsuario().getId(), especieId, siembra(2, false));
        Planta pb = catalogo.seleccionar(b.gestorUsuario().getId(), especieId, siembra(5, false));

        huertos.actualizarPlanta(
                a.gestorUsuario().getId(),
                pa.getId(),
                edicion("Nombre local de A", "Otro científico", 9));

        assertThat(catalogo.obtener(b.gestorUsuario().getId(), especieId).getNombreComun())
                .isEqualTo("Especie compartida 7781");
        assertThat(catalogo.obtener(b.gestorUsuario().getId(), especieId).getNombreCientifico())
                .isEqualTo("Identificación sintética");
        var deB = huertos.plantaDeGestor(b.gestorUsuario().getId(), pb.getId());
        assertThat(deB.getNombreComun()).isEqualTo("Especie compartida 7781");
        assertThat(deB.getCantidad()).isEqualTo(5);
        // A sí guarda sus datos propios (cantidad, descripción); el nombre mostrado sigue siendo el
        // de la especie.
        var deA = huertos.plantaDeGestor(a.gestorUsuario().getId(), pa.getId());
        assertThat(deA.getCantidad()).isEqualTo(9);
        assertThat(deA.getDescripcion()).isEqualTo("Nota local de la comunidad");
        assertThat(deA.getNombreComun()).isEqualTo("Especie compartida 7781");
    }

    @Test
    void enUnaComunidadActivaSiembraYBodegaFuncionan() {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        Planta p =
                catalogo.seleccionar(gestor, especie(e, "Especie activa 7782"), siembra(1, false));
        huertos.actualizarSiembra(gestor, p.getId(), siembra(4, true));
        assertThat(huertos.plantaDeGestor(gestor, p.getId()).isPlantada()).isTrue();

        ArticuloBodega art = bodega.crear(gestor, articulo("Pala", 2));
        bodega.editar(gestor, art.getId(), articulo("Pala grande", 3));
        assertThat(bodega.obtener(gestor, art.getId()).getCantidad()).isEqualTo(3);
        bodega.eliminar(gestor, art.getId());
        assertThat(bodega.listar(gestor)).isEmpty();
    }

    @Test
    void conEliminacionPendienteSeRechazanSiembraYCambiosDeBodega() {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        Long especieNueva = especie(e, "Especie nueva 7783");
        Planta p =
                catalogo.seleccionar(gestor, especie(e, "Especie previa 7784"), siembra(1, false));
        ArticuloBodega art = bodega.crear(gestor, articulo("Rastrillo", 1));

        solicitudes.solicitarEliminacion(
                gestor, e.comunidad().getId(), "Prueba de comunidad no operativa");

        assertThatThrownBy(() -> catalogo.seleccionar(gestor, especieNueva, siembra(1, true)))
                .isInstanceOf(ConflictoException.class);
        assertThatThrownBy(() -> huertos.actualizarSiembra(gestor, p.getId(), siembra(9, true)))
                .isInstanceOf(ConflictoException.class);
        assertThatThrownBy(() -> bodega.crear(gestor, articulo("Azadón", 1)))
                .isInstanceOf(ConflictoException.class);
        assertThatThrownBy(
                        () -> bodega.editar(gestor, art.getId(), articulo("Rastrillo editado", 5)))
                .isInstanceOf(ConflictoException.class);
        assertThatThrownBy(() -> bodega.eliminar(gestor, art.getId()))
                .isInstanceOf(ConflictoException.class);

        var sinCambio = huertos.plantaDeGestor(gestor, p.getId());
        assertThat(sinCambio.getCantidad()).isEqualTo(1);
        assertThat(sinCambio.isPlantada()).isFalse();
        assertThat(bodega.listar(gestor))
                .extracting(ArticuloBodega::getNombre)
                .containsExactly("Rastrillo");
        assertThat(huertos.plantasDeGestor(gestor)).hasSize(1);
    }

    @Test
    void enUnaComunidadArchivadaSeRechazanSiembraYCambiosDeBodega() {
        Escenario e = fabrica.escenario(1);
        Long gestor = e.gestorUsuario().getId();
        Long especieNueva = especie(e, "Especie nueva 7785");
        Planta p =
                catalogo.seleccionar(gestor, especie(e, "Especie previa 7786"), siembra(1, false));
        ArticuloBodega art = bodega.crear(gestor, articulo("Regadera", 1));

        comunidades.archivarComunidad(gestor);

        // Al archivar, el gestor pierde su pertenencia activa: el rechazo llega antes de tocar
        // datos.
        assertThatThrownBy(() -> catalogo.seleccionar(gestor, especieNueva, siembra(1, true)))
                .isInstanceOfAny(AccessDeniedException.class, ConflictoException.class);
        assertThatThrownBy(() -> huertos.actualizarSiembra(gestor, p.getId(), siembra(9, true)))
                .isInstanceOfAny(AccessDeniedException.class, ConflictoException.class);
        assertThatThrownBy(() -> bodega.crear(gestor, articulo("Azadón", 1)))
                .isInstanceOfAny(AccessDeniedException.class, ConflictoException.class);
        assertThatThrownBy(
                        () -> bodega.editar(gestor, art.getId(), articulo("Regadera editada", 5)))
                .isInstanceOfAny(AccessDeniedException.class, ConflictoException.class);
        assertThatThrownBy(() -> bodega.eliminar(gestor, art.getId()))
                .isInstanceOfAny(AccessDeniedException.class, ConflictoException.class);
    }
}
