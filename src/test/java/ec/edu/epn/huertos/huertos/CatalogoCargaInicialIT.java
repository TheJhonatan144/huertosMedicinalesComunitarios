package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.PruebaIntegracion;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import jakarta.validation.Validator;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
class CatalogoCargaInicialIT extends PruebaIntegracion {
    @Autowired JdbcTemplate jdbc;
    @Autowired EspecieRepository especies;
    @Autowired Validator validator;
    @Autowired MockMvc mvc;
    @Autowired FabricaDatos fabrica;

    private void cargar() throws Exception {
        jdbc.execute(Files.readString(Path.of("docs/catalogo/carga-bloque-01.sql")));
    }

    private Especie especie(String nombre) {
        return especies.findAll().stream().filter(e -> e.getNombreComun().equals(nombre)).findFirst().orElseThrow();
    }

    private int cantidad(String tabla) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
    }

    @Test void cargaDiezFichasValidasSinCrearSiembrasYConservaElContexto() throws Exception {
        var antes = jdbc.queryForMap("SELECT (SELECT COUNT(*) FROM planta) AS plantas, "
                + "(SELECT COUNT(*) FROM huerto) AS huertos, (SELECT COUNT(*) FROM comunidad) AS comunidades, "
                + "(SELECT COUNT(*) FROM articulo_bodega) AS bodega");
        int anteriores = cantidad("especie");
        cargar();
        assertThat(cantidad("especie")).isEqualTo(anteriores + 10);
        assertThat(jdbc.queryForMap("SELECT (SELECT COUNT(*) FROM planta) AS plantas, "
                + "(SELECT COUNT(*) FROM huerto) AS huertos, (SELECT COUNT(*) FROM comunidad) AS comunidades, "
                + "(SELECT COUNT(*) FROM articulo_bodega) AS bodega")).isEqualTo(antes);

        var albahaca = especie("Albahaca");
        assertThat(albahaca.getCualidad()).isEqualTo(CualidadPlanta.VARIABLE);
        assertThat(albahaca.getContextoCualidad()).contains("Caliente", "Fría", "Saraguro");
        assertThat(albahaca.getAltitudMinima()).isNull();
        assertThat(albahaca.getAltitudMaxima()).isNull();
        assertThat(albahaca.getObservacionesAltitud()).contains("650–700", "localidad");
        assertThat(especie("Escancel").getCualidad()).isEqualTo(CualidadPlanta.FRESCA);
        assertThat(especie("Ajo sacha").getCualidad()).isNull();
        assertThat(especie("Algodón").getObservacionesCiclo()).contains("multianual", "154 y 199");
        for (String nombre : new String[]{"Achiote", "Aguacate", "Ajo sacha", "Albahaca", "Algodón",
                "Café arábigo", "Canela amazónica", "Ceibo costeño", "Chilca", "Escancel"}) {
            var e = especie(nombre);
            assertThat(validator.validate(FichaEspecie.de(e))).as(nombre).isEmpty();
            assertThat(e.getFuenteDocumental()).isNotBlank();
            assertThat(e.getFotoClave()).isNull();
        }
    }

    @Test void repetirCargaNoDuplicaNiSobrescribeUnaFichaYaEditada() throws Exception {
        var existente = especies.saveAndFlush(new Especie("Achiote editado por el gestor", "Bixa orellana L."));
        int anteriores = cantidad("especie");
        cargar();
        cargar();
        assertThat(cantidad("especie")).isEqualTo(anteriores + 9);
        var conservada = jdbc.queryForMap("SELECT nombre_comun, fuente_documental, version FROM especie WHERE id = ?", existente.getId());
        assertThat(conservada.get("nombre_comun")).isEqualTo("Achiote editado por el gestor");
        assertThat(conservada.get("fuente_documental")).isNull();
        assertThat(((Number) conservada.get("version")).longValue()).isEqualTo(existente.getVersion());
    }

    @Test void unaFichaPreviaSinIdentificacionExigeRevisarlaAntesDeCargar() {
        especies.saveAndFlush(new Especie("Achiote", null));
        assertThatThrownBy(this::cargar).isInstanceOf(DataAccessException.class)
                .hasMessageContaining("sin identificación científica");
    }

    @Test void unaIdentificacionDuplicadaDetieneLaCarga() {
        especies.saveAndFlush(new Especie("Achiote uno", "Bixa orellana L."));
        especies.saveAndFlush(new Especie("Achiote dos", "Bixa orellana L."));
        assertThatThrownBy(this::cargar).isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Identificación repetida");
    }

    @Test void formularioYDetalleConservanLaCualidadFrescaYSusAclaraciones() throws Exception {
        var a = fabrica.escenario(0);
        var actor = new UsuarioAutenticado(a.gestorUsuario());
        mvc.perform(post("/gestor/plantas/catalogo").with(user(actor)).with(csrf())
                .param("nombreComun", "Ficha contextual de prueba")
                .param("cualidad", "FRESCA").param("contextoCualidad", "Tradición local de prueba")
                .param("ciclo", "LARGO").param("observacionesCiclo", "Perenne con cosecha anual")
                .param("observacionesAltitud", "Colecta puntual a 2700 m")
                .param("fuenteDocumental", "Fuente de prueba"))
                .andExpect(status().is3xxRedirection());
        var e = especie("Ficha contextual de prueba");
        assertThat(e.getContextoCualidad()).isEqualTo("Tradición local de prueba");
        assertThat(e.getObservacionesCiclo()).isEqualTo("Perenne con cosecha anual");
        assertThat(e.getObservacionesAltitud()).isEqualTo("Colecta puntual a 2700 m");
        mvc.perform(get("/gestor/plantas/catalogo/" + e.getId()).with(user(actor)))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Fresca")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tradición local de prueba")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Colecta puntual a 2700 m")));
        mvc.perform(get("/gestor/plantas/catalogo/" + e.getId() + "/editar").with(user(actor)))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Perenne con cosecha anual")));

        var ficha = FichaEspecie.de(e);
        ficha.setCualidad(CualidadPlanta.VARIABLE);
        ficha.setContextoCualidad(" ");
        assertThat(validator.validate(ficha)).extracting(v -> v.getPropertyPath().toString()).contains("contextoCualidadValido");
        ficha.setCualidad(null);
        ficha.setContextoCualidad("Cualidad no atribuible a un taxón concreto");
        ficha.setFuenteDocumental(" ");
        assertThat(validator.validate(ficha)).extracting(v -> v.getPropertyPath().toString()).contains("fuenteValida");
    }
}
