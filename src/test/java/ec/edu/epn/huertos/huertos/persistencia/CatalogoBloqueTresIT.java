package ec.edu.epn.huertos.huertos.persistencia;

import static org.assertj.core.api.Assertions.*;

import ec.edu.epn.huertos.huertos.aplicacion.CatalogoServicio;
import ec.edu.epn.huertos.huertos.aplicacion.FichaEspecie;
import ec.edu.epn.huertos.huertos.dominio.Especie;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;
import ec.edu.epn.huertos.soporte.integracion.PruebaCatalogoAislado;

import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Transactional
class CatalogoBloqueTresIT extends PruebaCatalogoAislado {
    @Autowired JdbcTemplate jdbc;
    @Autowired EspecieRepository especies;
    @Autowired CatalogoServicio catalogo;
    @Autowired FabricaDatos fabrica;
    @Autowired Validator validator;
    private static final Set<String> NOMBRES =
            Set.of(
                    "Epidendrum quitensium Rchb.f.",
                    "Thymus vulgaris L.",
                    "Melissa officinalis L.",
                    "Taraxacum sect. Taraxacum F.H.Wigg.",
                    "Urtica urens L.",
                    "Viola odorata L.",
                    "Dysphania ambrosioides (L.) Mosyakin & Clemants",
                    "Manihot esculenta Crantz",
                    "Uncaria tomentosa (Willd. ex Schult.) DC.");

    private void cargar() throws Exception {
        jdbc.execute(Files.readString(Path.of("docs/catalogo/carga-bloque-03.sql")));
    }

    private String plantas() {
        return jdbc.queryForObject(
                "SELECT coalesce(jsonb_agg(to_jsonb(p) ORDER BY id)::text,'[]') FROM planta p",
                String.class);
    }

    @Test
    void cargaNueveFichasValidasSinAlterarSiembrasNiCatalogoPrevio() throws Exception {
        var a = fabrica.escenario(0);
        var e = especies.saveAndFlush(new Especie("Planta previa", "Identificación previa"));
        jdbc.update(
                "INSERT INTO"
                    + " planta(huerto_id,especie_id,nombre_comun,nombre_cientifico,cantidad,activa,plantada)"
                    + " VALUES(?,?,?,?,?,?,?)",
                a.huerto().getId(),
                e.getId(),
                "Planta previa",
                "Identificación previa",
                7,
                false,
                true);
        String antes = plantas();
        var previa = jdbc.queryForMap("SELECT * FROM especie WHERE id=?", e.getId());
        int total = jdbc.queryForObject("SELECT count(*) FROM especie", Integer.class);
        cargar();
        assertThat(plantas()).isEqualTo(antes);
        assertThat(jdbc.queryForMap("SELECT * FROM especie WHERE id=?", e.getId()))
                .isEqualTo(previa);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM especie", Integer.class))
                .isEqualTo(total + 9);
        var fichas =
                especies.findAll().stream()
                        .filter(
                                x ->
                                        x.getNombreCientifico() != null
                                                && NOMBRES.contains(x.getNombreCientifico()))
                        .toList();
        assertThat(fichas).hasSize(9);
        for (var ficha : fichas) {
            assertThat(validator.validate(FichaEspecie.de(ficha)))
                    .as(ficha.getNombreComun())
                    .isEmpty();
            assertThat(ficha.getFuenteDocumental()).isNotBlank();
            assertThat(ficha.getUsosMedicinales()).isNotBlank();
        }
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM especie WHERE nombre_cientifico IN"
                                    + " ('Epidendrum quitensium Rchb.f.','Thymus vulgaris"
                                    + " L.','Manihot esculenta Crantz','Uncaria tomentosa (Willd."
                                    + " ex Schult.) DC.') AND cualidad IS NULL",
                                Integer.class))
                .isEqualTo(4);
        assertThat(
                        jdbc.queryForMap(
                                "SELECT altitud_minima,altitud_maxima,ciclo FROM especie WHERE"
                                    + " nombre_cientifico='Epidendrum quitensium Rchb.f.'"))
                .containsEntry("altitud_minima", 1600)
                .containsEntry("altitud_maxima", 2700)
                .containsEntry("ciclo", null);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM especie WHERE nombre_cientifico='Taraxacum"
                                    + " sect. Taraxacum F.H.Wigg.' AND altitud_minima IS NULL AND"
                                    + " altitud_maxima IS NULL",
                                Integer.class))
                .isEqualTo(1);
    }

    @Test
    void repeticionConservaEdicionesDocumentadasYFotografias() throws Exception {
        cargar();
        jdbc.update(
                "UPDATE especie SET nombre_comun='Violeta"
                    + " revisada',foto_clave='existente.jpg',foto_tipo='image/jpeg',foto_fuente='Autor"
                    + " local',version=version+1 WHERE nombre_cientifico='Viola odorata L.'");
        var antes = jdbc.queryForList("SELECT * FROM especie ORDER BY id");
        var regiones = jdbc.queryForList("SELECT * FROM especie_region ORDER BY especie_id,region");
        cargar();
        assertThat(jdbc.queryForList("SELECT * FROM especie ORDER BY id")).isEqualTo(antes);
        assertThat(jdbc.queryForList("SELECT * FROM especie_region ORDER BY especie_id,region"))
                .isEqualTo(regiones);
    }

    @Test
    void nombreCientificoAmbiguoDetieneCarga() {
        especies.saveAndFlush(new Especie("Tomillo uno", "Thymus vulgaris L."));
        especies.saveAndFlush(new Especie("Tomillo dos", "Thymus vulgaris L."));
        assertThatThrownBy(this::cargar)
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("equivalencia ambigua");
    }

    @Test
    void sinonimosYNombrePopularEncuentranLaFichaDelConjuntoCorrecto() throws Exception {
        var a = fabrica.escenario(0);
        cargar();
        assertThat(
                        catalogo.listar(
                                a.gestorUsuario().getId(),
                                "chenopodium ambrosioides",
                                null,
                                null,
                                null))
                .extracting(Especie::getNombreComun)
                .containsExactly("Paico");
        assertThat(
                        catalogo.listar(
                                a.gestorUsuario().getId(),
                                "taraxacum officinale",
                                null,
                                null,
                                null))
                .extracting(Especie::getNombreCientifico)
                .containsExactly("Taraxacum sect. Taraxacum F.H.Wigg.");
        assertThat(catalogo.listar(a.gestorUsuario().getId(), "orquidea", null, null, null))
                .extracting(Especie::getNombreCientifico)
                .containsExactly("Epidendrum quitensium Rchb.f.");
    }
}
