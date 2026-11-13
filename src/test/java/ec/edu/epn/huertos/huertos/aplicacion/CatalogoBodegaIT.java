package ec.edu.epn.huertos.huertos.aplicacion;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.bodega.aplicacion.BodegaServicio;
import ec.edu.epn.huertos.bodega.aplicacion.FormularioArticulo;
import ec.edu.epn.huertos.bodega.dominio.CategoriaArticulo;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.huertos.dominio.CicloPlanta;
import ec.edu.epn.huertos.huertos.dominio.CualidadPlanta;
import ec.edu.epn.huertos.huertos.dominio.Especie;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.huertos.dominio.RegionPlanta;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;
import ec.edu.epn.huertos.soporte.datos.ImagenesPrueba;
import ec.edu.epn.huertos.soporte.integracion.PruebaIntegracion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import javax.sql.DataSource;

@AutoConfigureMockMvc
class CatalogoBodegaIT extends PruebaIntegracion {
    @Autowired FabricaDatos fabrica;
    @Autowired CatalogoServicio catalogo;
    @Autowired HuertoServicio huertos;
    @Autowired BodegaServicio bodega;
    @Autowired MockMvc mvc;
    @Autowired DataSource dataSource;

    private FichaEspecie ficha() {
        var f = new FichaEspecie();
        f.setNombreComun("Especie sintética");
        f.setNombreCientifico("Identificación de prueba");
        f.setOtrosNombres("Nombre de prueba");
        f.setFamiliaBotanica("Familia de prueba");
        f.setAltitudMinima(1000);
        f.setAltitudMaxima(2500);
        f.setProvincias("Imbabura, Pichincha");
        f.setRegiones(Set.of(RegionPlanta.SIERRA, RegionPlanta.AMAZONIA));
        f.setClima("Condición sintética");
        f.setCiclo(CicloPlanta.CORTO);
        f.setCualidad(CualidadPlanta.FRIA);
        f.setUsosMedicinales("Texto sintético sin indicaciones terapéuticas");
        f.setSaberes("Contexto sintético");
        f.setFuenteDocumental("Fuente sintética de prueba, página 1");
        f.setFotoFuente("Fotografía sintética de prueba");
        return f;
    }

    private SiembraPlanta siembra(int cantidad, boolean plantada) {
        var s = new SiembraPlanta();
        s.setCantidad(cantidad);
        s.setPlantada(plantada);
        return s;
    }

    private FormularioArticulo articulo() {
        var f = new FormularioArticulo();
        f.setNombre("Semillas de prueba");
        f.setCategoria(CategoriaArticulo.SEMILLA);
        f.setCantidad(8);
        return f;
    }

    @Test
    void unaEspecieComunTieneCantidadYSiembraDistintasPorComunidad() {
        var a = fabrica.escenario(1);
        var b = fabrica.escenario(1);
        Long especie = catalogo.guardar(a.revisor().getId(), null, ficha(), null).getId();
        var pa = catalogo.seleccionar(a.gestorUsuario().getId(), especie, siembra(0, true));
        var pb = catalogo.seleccionar(b.gestorUsuario().getId(), especie, siembra(7, false));
        assertThat(pa.getEspecieId()).isEqualTo(pb.getEspecieId());
        huertos.cambiarEstadoPlanta(a.gestorUsuario().getId(), pa.getId(), false);
        assertThat(huertos.plantasPlantadasDeGestor(a.gestorUsuario().getId()))
                .extracting(Planta::getId)
                .containsExactly(pa.getId());
        assertThat(huertos.plantaDeGestor(a.gestorUsuario().getId(), pa.getId()).isActiva())
                .isFalse();
        assertThat(huertos.plantasPlantadasDeGestor(b.gestorUsuario().getId())).isEmpty();
        assertThat(huertos.plantaDeGestor(b.gestorUsuario().getId(), pb.getId()).getCantidad())
                .isEqualTo(7);
        assertThatThrownBy(
                        () ->
                                catalogo.seleccionar(
                                        a.gestorUsuario().getId(), especie, siembra(1, true)))
                .isInstanceOf(ConflictoException.class);
        huertos.actualizarSiembra(a.gestorUsuario().getId(), pa.getId(), siembra(4, false));
        assertThat(huertos.plantaDeGestor(a.gestorUsuario().getId(), pa.getId()).isActiva())
                .isFalse();
    }

    @Test
    void fichaCompletaSeComparteYFotoSeConservaAlEditar() {
        var a = fabrica.escenario(1);
        var b = fabrica.escenario(1);
        var e = catalogo.guardar(a.revisor().getId(), null, ficha(), ImagenesPrueba.foto());
        var pa = catalogo.seleccionar(a.gestorUsuario().getId(), e.getId(), siembra(3, true));
        var pb = catalogo.seleccionar(b.gestorUsuario().getId(), e.getId(), siembra(9, false));
        var edit = FichaEspecie.de(catalogo.obtener(b.gestorUsuario().getId(), e.getId()));
        edit.setNombreComun("Nombre compartido editado");
        catalogo.guardar(b.revisor().getId(), e.getId(), edit, null);
        var actual = catalogo.obtener(a.gestorUsuario().getId(), e.getId());
        assertThat(actual.getFotoClave()).isEqualTo(e.getFotoClave());
        assertThat(actual.getFamiliaBotanica()).isEqualTo(edit.getFamiliaBotanica());
        assertThat(actual.getOtrosNombres()).isEqualTo(edit.getOtrosNombres());
        assertThat(actual.getRegiones())
                .containsExactlyInAnyOrder(RegionPlanta.SIERRA, RegionPlanta.AMAZONIA);
        assertThat(actual.getUsosMedicinales()).isEqualTo(edit.getUsosMedicinales());
        assertThat(actual.getSaberes()).isEqualTo(edit.getSaberes());
        assertThat(actual.getFuenteDocumental()).isEqualTo(edit.getFuenteDocumental());
        assertThat(huertos.plantaDeGestor(a.gestorUsuario().getId(), pa.getId()).getNombreComun())
                .isEqualTo("Nombre compartido editado");
        assertThat(huertos.plantaDeGestor(b.gestorUsuario().getId(), pb.getId()).getCantidad())
                .isEqualTo(9);
        assertThatThrownBy(() -> catalogo.guardar(a.revisor().getId(), e.getId(), edit, null))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void filtrosCombinanRegionProvinciaAltitudYNombre() {
        var a = fabrica.escenario(1);
        var f = ficha();
        f.setNombreComun("Filtro exclusivo 9843");
        var e = catalogo.guardar(a.revisor().getId(), null, f, null);
        assertThat(
                        catalogo.listar(
                                a.gestorUsuario().getId(),
                                "9843",
                                RegionPlanta.SIERRA,
                                "Imbabura",
                                2000))
                .extracting(Especie::getId)
                .containsExactly(e.getId());
        assertThat(catalogo.listar(a.gestorUsuario().getId(), "9843", RegionPlanta.COSTA, "", null))
                .isEmpty();
        assertThat(catalogo.listar(a.gestorUsuario().getId(), "9843", null, "", 3000)).isEmpty();
    }

    @Test
    void fuenteAltitudYCantidadesInvalidasSonRechazadas() throws Exception {
        var a = fabrica.escenario(1);
        var f = ficha();
        f.setFuenteDocumental(" ");
        assertThatThrownBy(() -> catalogo.guardar(a.revisor().getId(), null, f, null))
                .isInstanceOf(ReglaNegocioException.class);
        f.setFuenteDocumental("Fuente de prueba");
        f.setAltitudMinima(3000);
        assertThatThrownBy(() -> catalogo.guardar(a.revisor().getId(), null, f, null))
                .isInstanceOf(ReglaNegocioException.class);
        mvc.perform(
                        post("/gestor/plantas/catalogo")
                                .with(user(new UsuarioAutenticado(a.revisor())))
                                .with(csrf())
                                .param("nombreComun", "Prueba")
                                .param("usosMedicinales", "Texto sin fuente"))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Registra una fuente")));
    }

    @Test
    void renderizaPantallasFotoYContratoDePlantadas() throws Exception {
        var a = fabrica.escenario(1);
        Long u = a.gestorUsuario().getId();
        var actor = new UsuarioAutenticado(a.gestorUsuario());
        var fichaVista = ficha();
        fichaVista.setNombreComun("Vista de catálogo 46391");
        Long e =
                catalogo.guardar(a.revisor().getId(), null, fichaVista, ImagenesPrueba.foto())
                        .getId();
        var p = catalogo.seleccionar(u, e, siembra(2, true));
        for (String ruta :
                new String[] {
                    "/gestor/plantas",
                    "/gestor/plantas/catalogo",
                    "/gestor/plantas/catalogo/" + e,
                    "/gestor/plantas/" + p.getId() + "/siembra",
                    "/gestor/bodega",
                    "/gestor/bodega/nuevo"
                }) mvc.perform(get(ruta).with(user(actor))).andExpect(status().isOk());
        String bodegaHtml =
                mvc.perform(
                                get("/gestor/bodega")
                                        .with(user(actor))
                                        .flashAttr("mensaje", "Artículo guardado."))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(bodegaHtml)
                .contains(
                        "href=\"/gestor/bodega\" aria-current=\"page\"",
                        "toast-container plantas-avisos",
                        "Artículo guardado.")
                .doesNotContain("alert alert-success");
        mvc.perform(get("/gestor/plantas/catalogo/" + e + "/foto").with(user(actor)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
        mvc.perform(
                        get("/gestor/plantas/catalogo")
                                .with(user(actor))
                                .param("vista", "lista")
                                .param("q", "46391")
                                .param("region", "SIERRA")
                                .param("provincia", "Imbabura")
                                .param("altitud", "1500"))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "catalogo-resultados catalogo-lista")))
                .andExpect(
                        content()
                                .string(org.hamcrest.Matchers.containsString("1 ficha encontrada")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("En mi huerto")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "q=46391&amp;region=SIERRA&amp;provincia=Imbabura&amp;altitud=1500&amp;vista=cuadricula")));
        mvc.perform(get("/gestor/plantas/plantadas").with(user(actor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especieId").value(e))
                .andExpect(jsonPath("$[0].plantada").value(true));
    }

    @Test
    void bodegaCrudNoCambiaLaSiembra() {
        var a = fabrica.escenario(1);
        Long u = a.gestorUsuario().getId();
        var f = articulo();
        Long e = catalogo.guardar(a.revisor().getId(), null, ficha(), null).getId();
        Long p = catalogo.seleccionar(u, e, siembra(0, true)).getId();
        Long id = bodega.crear(u, f).getId();
        assertThat(bodega.listar(u)).hasSize(1);
        f.setCantidad(0);
        bodega.editar(u, id, f);
        assertThat(bodega.obtener(u, id).getCantidad()).isZero();
        bodega.eliminar(u, id);
        assertThat(bodega.listar(u)).isEmpty();
        assertThat(huertos.plantasPlantadasDeGestor(u))
                .extracting(Planta::getId)
                .containsExactly(p);
    }

    @Test
    void soloGestoresYDatosComunitariosAislados() throws Exception {
        var a = fabrica.escenario(1);
        var b = fabrica.escenario(1);
        Long u = a.gestorUsuario().getId();
        Long e = catalogo.guardar(a.revisor().getId(), null, ficha(), null).getId();
        Long p = catalogo.seleccionar(u, e, siembra(1, true)).getId();
        Long art = bodega.crear(u, articulo()).getId();
        var otro = new UsuarioAutenticado(b.gestorUsuario());
        mvc.perform(get("/gestor/plantas/" + p + "/siembra").with(user(otro)))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/gestor/plantas/" + p + "/siembra")
                                .with(user(otro))
                                .with(csrf())
                                .param("cantidad", "2"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/gestor/bodega/" + art + "/editar").with(user(otro)))
                .andExpect(status().isForbidden());
        assertThatThrownBy(() -> bodega.eliminar(b.gestorUsuario().getId(), art))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> catalogo.obtener(a.participanteUsuarios().get(0).getId(), e))
                .isInstanceOf(AccessDeniedException.class);
        mvc.perform(get("/gestor/plantas/catalogo").with(user(new UsuarioAutenticado(a.revisor()))))
                .andExpect(status().isOk());
        mvc.perform(post("/gestor/bodega").with(user(otro))).andExpect(status().isForbidden());
    }

    @Test
    void rechazaFotosFalsasSinGuardarLaEspecie() {
        var a = fabrica.escenario(1);
        var f = ficha();
        f.setNombreComun("Falsa 773312");
        assertThatThrownBy(
                        () ->
                                catalogo.guardar(
                                        a.revisor().getId(),
                                        null,
                                        f,
                                        new MockMultipartFile(
                                                "foto",
                                                "falsa.png",
                                                "image/png",
                                                "texto".getBytes())))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(catalogo.listar(a.gestorUsuario().getId(), "773312", null, "", null)).isEmpty();
    }

    @Test
    void ministerioEditaFichasYGestionComunitariaSigueReservadaAlGestor() throws Exception {
        var a = fabrica.escenario(1);
        var gestor = new UsuarioAutenticado(a.gestorUsuario());
        var ministerio = new UsuarioAutenticado(a.revisor());
        String raiz = "/gestor/plantas/catalogo";
        Long id = catalogo.guardar(a.revisor().getId(), null, ficha(), null).getId();
        String detalle = raiz + "/" + id;

        mvc.perform(get(raiz + "/nueva").with(user(gestor))).andExpect(status().isForbidden());
        mvc.perform(get(detalle + "/editar").with(user(gestor))).andExpect(status().isForbidden());
        mvc.perform(post(raiz).with(user(gestor)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(detalle).with(user(gestor)).with(csrf()))
                .andExpect(status().isForbidden());
        assertThatThrownBy(() -> catalogo.guardar(a.gestorUsuario().getId(), id, ficha(), null))
                .isInstanceOf(AccessDeniedException.class);

        mvc.perform(get(raiz + "/nueva").with(user(ministerio))).andExpect(status().isOk());
        mvc.perform(get(detalle + "/editar").with(user(ministerio))).andExpect(status().isOk());
        mvc.perform(
                        post(detalle)
                                .with(user(ministerio))
                                .with(csrf())
                                .param("nombreComun", "Ficha revisada por el MSP"))
                .andExpect(status().is3xxRedirection());
        assertThat(catalogo.obtener(a.gestorUsuario().getId(), id).getNombreComun())
                .isEqualTo("Ficha revisada por el MSP");
        mvc.perform(post(detalle + "/seleccionar").with(user(ministerio)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/gestor/bodega").with(user(ministerio))).andExpect(status().isForbidden());
        mvc.perform(
                        post(detalle + "/seleccionar")
                                .with(user(gestor))
                                .with(csrf())
                                .param("cantidad", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void migracionConservaRegistrosYNoAsumeQueEstanPlantados() {
        String esquema = "migracion_" + java.util.UUID.randomUUID().toString().replace("-", "");
        var jdbc = new JdbcTemplate(dataSource);
        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(esquema)
                    .defaultSchema(esquema)
                    .target("3")
                    .load()
                    .migrate();
            jdbc.execute(
                    "INSERT INTO "
                            + esquema
                            + ".comunidad(id,nombre,descripcion,provincia,canton,latitud,longitud)"
                            + " VALUES (1,'Prueba','Prueba','Imbabura','Otavalo',0.2,-78.2)");
            jdbc.execute(
                    "INSERT INTO "
                            + esquema
                            + ".huerto(id,comunidad_id,nombre,descripcion,latitud,longitud) VALUES"
                            + " (1,1,'Prueba','Prueba',0.2,-78.2)");
            jdbc.execute(
                    "INSERT INTO "
                            + esquema
                            + ".planta(huerto_id,nombre_comun,cantidad,activa,descripcion) VALUES"
                            + " (1,'Preexistente',9,false,'Texto local')");
            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(esquema)
                    .defaultSchema(esquema)
                    .load()
                    .migrate();
            var fila = jdbc.queryForMap("SELECT * FROM " + esquema + ".planta");
            assertThat(fila.get("cantidad")).isEqualTo(9);
            assertThat(fila.get("activa")).isEqualTo(false);
            assertThat(fila.get("plantada")).isEqualTo(false);
            assertThat(fila.get("especie_id")).isNotNull();
            assertThat(fila.get("descripcion")).isEqualTo("Texto local");
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + esquema + " CASCADE");
        }
    }
}
