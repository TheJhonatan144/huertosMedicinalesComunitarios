package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.PruebaIntegracion;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.comunidades.Comunidad;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Formulario del huerto con tarjetas seleccionables: opciones, preselección, persistencia, validación y aislamiento. */
@AutoConfigureMockMvc
class FormularioHuertoTarjetasIT extends PruebaIntegracion {

    static final Set<String> DISENOS = Set.of("TIERRA_SURCOS", "TIERRA_CAMADAS", "TIERRA_CAMELLONES", "TIERRA_TERRAZAS",
            "VERTICAL_PAREDES", "COLGANTE_TECHOS", "PISO_MACETAS", "PISO_BOTELLAS_PATIO", "PISO_LLANTAS_PATIO",
            "PISO_TERRAZA", "PISO_GRADAS", "PISO_BALCONES", "PISO_FILOS_VENTANA");
    static final Set<String> SUELOS = Set.of("ARCILLOSO", "LIMOSO", "ARENOSO", "MARGOSO", "GREDOSO", "PANTANOSO");
    static final Set<String> CERRAMIENTOS = Set.of("VIVO_HIGUERILLA", "VIVO_FLORIPONDIO", "VIVO_AGAVE", "VIVO_LAVANDA",
            "VIVO_NARANJOS", "VIVO_CAPULIES", "VIVO_TOCTE", "VIVO_GUABO", "NATURAL_CANA_GUADUA", "NATURAL_BAHAREQUE",
            "NATURAL_TAPIAL", "RECICLADO_TABLAS", "RECICLADO_BOTELLAS", "RECICLADO_NEUMATICOS", "METALICO_ALAMBRE_PUAS",
            "CONVENCIONAL_PIEDRA", "CONVENCIONAL_LADRILLO", "CONVENCIONAL_BLOQUE");
    static final Set<String> ABONOS = Set.of("BARBECHO", "RESTOS_VEGETALES", "HOJA", "COMPOST");

    private static final Pattern RADIO = Pattern.compile("<input[^>]*class=\"hf-radio\"[^>]*>");

    @Autowired FabricaDatos fabrica;
    @Autowired HuertoRepository huertos;
    @Autowired MockMvc mvc;

    private static UsuarioAutenticado como(Usuario u) {
        return new UsuarioAutenticado(u);
    }

    /** Radios del formulario agrupados por name: valor -> marcado. */
    private static Map<String, Map<String, Boolean>> radios(String html) {
        Map<String, Map<String, Boolean>> porCampo = new LinkedHashMap<>();
        Matcher m = RADIO.matcher(html);
        while (m.find()) {
            String input = m.group();
            String nombre = atributo(input, "name");
            String valor = atributo(input, "value");
            porCampo.computeIfAbsent(nombre, k -> new LinkedHashMap<>()).put(valor, input.contains("checked"));
        }
        return porCampo;
    }

    private static String atributo(String input, String nombre) {
        Matcher m = Pattern.compile("\\s" + nombre + "=\"([^\"]*)\"").matcher(input);
        return m.find() ? m.group(1) : null;
    }

    private static List<String> marcados(Map<String, Boolean> opciones) {
        List<String> r = new ArrayList<>();
        opciones.forEach((v, marcado) -> { if (marcado) r.add(v); });
        return r;
    }

    private String formulario(Usuario u) throws Exception {
        return mvc.perform(get("/gestor/huerto").with(user(como(u)))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private void fijar(Escenario e, String diseno, String suelo, String abono, String cerramiento) {
        Huerto h = huertos.findById(e.huerto().getId()).orElseThrow();
        h.actualizar(h.getNombre(), h.getDescripcion(), h.getProvincia(), h.getParroquia(), h.getDireccion(), h.getAreaM2(),
                diseno, suelo, h.getFuenteAgua(), abono, cerramiento, h.getLatitud(), h.getLongitud());
        huertos.save(h);
    }

    /** POST completo, con los datos de comunidad tal como están (la sección 2 no cambia). */
    private MockHttpServletRequestBuilder guardar(Escenario e, Usuario quien, String diseno, String suelo, String abono,
                                                 String cerramiento) {
        Comunidad c = e.comunidad();
        return post("/gestor/huerto").with(user(como(quien))).with(csrf())
                .param("nombreComunidad", c.getNombre()).param("tipoComunidad", c.getTipoComunidad())
                .param("descripcionComunidad", c.getDescripcion()).param("provinciaComunidad", c.getProvincia())
                .param("cantonComunidad", c.getCanton()).param("direccionComunidad", "Calle sintética 1")
                .param("latitudComunidad", "0.23").param("longitudComunidad", "-78.26")
                .param("nombre", "Huerto editado " + e.comunidad().getId()).param("descripcion", "Descripción editada")
                .param("provincia", "Imbabura").param("parroquia", "San Luis").param("direccion", "Junto a la escuela")
                .param("areaM2", "12.50").param("latitud", "0.23").param("longitud", "-78.26")
                .param("fuenteAgua", "Canal")
                .param("diseno", diseno).param("tipoSuelo", suelo).param("abonoNatural", abono)
                .param("cerramiento", cerramiento);
    }

    @Test
    void muestraTodasLasOpcionesComoTarjetasYPreseleccionaLosValoresGuardados() throws Exception {
        Escenario e = fabrica.escenario(1);
        fijar(e, "PISO_GRADAS", "PANTANOSO", "COMPOST", "CONVENCIONAL_BLOQUE");

        String html = formulario(e.gestorUsuario());
        Map<String, Map<String, Boolean>> r = radios(html);

        assertThat(r.get("diseno").keySet()).containsExactlyInAnyOrderElementsOf(DISENOS);
        assertThat(r.get("tipoSuelo").keySet()).containsExactlyInAnyOrderElementsOf(SUELOS);
        assertThat(r.get("cerramiento").keySet()).containsExactlyInAnyOrderElementsOf(CERRAMIENTOS);
        assertThat(r.get("abonoNatural").keySet()).containsExactlyInAnyOrderElementsOf(ABONOS);

        assertThat(marcados(r.get("diseno"))).containsExactly("PISO_GRADAS");
        assertThat(marcados(r.get("tipoSuelo"))).containsExactly("PANTANOSO");
        assertThat(marcados(r.get("abonoNatural"))).containsExactly("COMPOST");
        assertThat(marcados(r.get("cerramiento"))).containsExactly("CONVENCIONAL_BLOQUE");

        // Agrupación por familia/tipo, ilustraciones propias y sin <select> para estas opciones.
        assertThat(html).contains("En tierra").contains("Vertical").contains("Colgante").contains("En piso")
                .contains("Cerco vivo y árboles frutales").contains("Reciclado").contains("Metálico")
                .contains("/img/huerto-form/diseno-piso.svg").contains("/img/huerto-form/suelo-pantanoso.svg")
                .contains("/img/huerto-form/abono-compost.svg").contains("/img/huerto-form/cerramiento-convencional.svg")
                .contains("list=\"hf-fuentes-agua\"").contains("<option value=\"Goteo\">")
                .doesNotContain("id=\"abonoNatural\"").doesNotContain("http://").doesNotContain("https://");
        // La sección 2 sigue igual: el select de tipo de comunidad.
        assertThat(html).contains("id=\"tipoComunidad\"").contains("Datos de la comunidad");
    }

    @Test
    void conValoresAntiguosNoSePreseleccionaNingunaTarjeta() throws Exception {
        Escenario e = fabrica.escenario(1);   // huerto con cerramiento NATURAL y abono «No especificado» (valores previos)
        Map<String, Map<String, Boolean>> r = radios(formulario(e.gestorUsuario()));
        assertThat(marcados(r.get("diseno"))).containsExactly("TIERRA_SURCOS");
        assertThat(marcados(r.get("tipoSuelo"))).containsExactly("LIMOSO");
        assertThat(marcados(r.get("cerramiento"))).isEmpty();
        assertThat(marcados(r.get("abonoNatural"))).isEmpty();
    }

    @Test
    void guardarConLosValoresDeLasTarjetasPersiste() throws Exception {
        Escenario e = fabrica.escenario(1);
        mvc.perform(guardar(e, e.gestorUsuario(), "COLGANTE_TECHOS", "GREDOSO", "RESTOS_VEGETALES", "VIVO_CAPULIES"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/gestor"));

        Huerto h = huertos.findById(e.huerto().getId()).orElseThrow();
        assertThat(h.getDiseno()).isEqualTo("COLGANTE_TECHOS");
        assertThat(h.getTipoSuelo()).isEqualTo("GREDOSO");
        assertThat(h.getAbonoNatural()).isEqualTo("RESTOS_VEGETALES");
        assertThat(h.getCerramiento()).isEqualTo("VIVO_CAPULIES");
        assertThat(h.getFuenteAgua()).isEqualTo("Canal");

        Map<String, Map<String, Boolean>> r = radios(formulario(e.gestorUsuario()));
        assertThat(marcados(r.get("cerramiento"))).containsExactly("VIVO_CAPULIES");
        assertThat(marcados(r.get("abonoNatural"))).containsExactly("RESTOS_VEGETALES");
    }

    @Test
    void unCodigoInvalidoSigueRechazandose() throws Exception {
        Escenario e = fabrica.escenario(1);
        fijar(e, "PISO_MACETAS", "ARENOSO", "HOJA", "NATURAL_TAPIAL");

        String html = mvc.perform(guardar(e, e.gestorUsuario(), "DISEÑO_DESCONOCIDO", "ARENOSO", "HOJA", "NATURAL_TAPIAL"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Selecciona un diseño válido.").contains("class=\"hf-error\"");

        mvc.perform(guardar(e, e.gestorUsuario(), "PISO_MACETAS", "ARENOSO", "QUIMICO", "NATURAL_TAPIAL"))
                .andExpect(status().isOk());
        mvc.perform(guardar(e, e.gestorUsuario(), "PISO_MACETAS", "ARENOSO", "HOJA", "NATURAL"))   // código general antiguo
                .andExpect(status().isOk());
        // tipoSuelo no tiene @Pattern en FormularioHuerto: lo rechaza HuertoServicio (ReglaNegocioException -> 422).
        mvc.perform(guardar(e, e.gestorUsuario(), "PISO_MACETAS", "FRANCO", "HOJA", "NATURAL_TAPIAL"))
                .andExpect(status().is(422));

        Huerto h = huertos.findById(e.huerto().getId()).orElseThrow();
        assertThat(List.of(h.getDiseno(), h.getTipoSuelo(), h.getAbonoNatural(), h.getCerramiento()))
                .containsExactly("PISO_MACETAS", "ARENOSO", "HOJA", "NATURAL_TAPIAL");
    }

    @Test
    void otroGestorNoPuedeEditarEsteHuerto() throws Exception {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        fijar(a, "VERTICAL_PAREDES", "MARGOSO", "BARBECHO", "METALICO_ALAMBRE_PUAS");

        // El formulario de B muestra el huerto de B, nunca el de A.
        String deB = formulario(b.gestorUsuario());
        assertThat(deB).contains(b.huerto().getNombre()).doesNotContain(a.huerto().getNombre());

        // B envía el formulario: solo cambia su propio huerto (la ruta no recibe id de huerto).
        mvc.perform(guardar(b, b.gestorUsuario(), "PISO_BALCONES", "ARCILLOSO", "COMPOST", "RECICLADO_TABLAS"))
                .andExpect(status().is3xxRedirection());
        Huerto deA = huertos.findById(a.huerto().getId()).orElseThrow();
        assertThat(List.of(deA.getNombre(), deA.getDiseno(), deA.getTipoSuelo(), deA.getAbonoNatural(), deA.getCerramiento()))
                .containsExactly(a.huerto().getNombre(), "VERTICAL_PAREDES", "MARGOSO", "BARBECHO", "METALICO_ALAMBRE_PUAS");
        assertThat(huertos.findById(b.huerto().getId()).orElseThrow().getDiseno()).isEqualTo("PISO_BALCONES");

        // Un participante de A no puede ver ni enviar el formulario.
        Usuario participante = a.participanteUsuarios().get(0);
        mvc.perform(get("/gestor/huerto").with(user(como(participante)))).andExpect(status().isForbidden());
        mvc.perform(guardar(a, participante, "PISO_BALCONES", "ARCILLOSO", "COMPOST", "RECICLADO_TABLAS"))
                .andExpect(status().isForbidden());
        assertThat(huertos.findById(a.huerto().getId()).orElseThrow().getDiseno()).isEqualTo("VERTICAL_PAREDES");
    }
}
