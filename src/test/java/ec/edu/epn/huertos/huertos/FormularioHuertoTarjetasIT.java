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
import java.math.BigDecimal;
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

/**
 * Formulario del huerto con el modelo de varios espacios (diseños en casillas), suelo en radios, abono en lista
 * y cerramiento en hasta cinco grupos: opciones, valor guardado marcado, persistencia, validación y aislamiento.
 */
@AutoConfigureMockMvc
class FormularioHuertoTarjetasIT extends PruebaIntegracion {

    static final Set<String> DISENOS = Set.of("TIERRA_SURCOS", "TIERRA_CAMADAS", "TIERRA_CAMELLONES", "TIERRA_TERRAZAS",
            "VERTICAL_PAREDES", "COLGANTE_TECHOS", "PISO_MACETAS", "PISO_BOTELLAS_PATIO", "PISO_LLANTAS_PATIO",
            "PISO_TERRAZA", "PISO_GRADAS", "PISO_BALCONES", "PISO_FILOS_VENTANA");
    static final Set<String> SUELOS = Set.of("ARCILLOSO", "LIMOSO", "ARENOSO", "MARGOSO", "GREDOSO", "PANTANOSO");
    static final Set<String> ABONOS = Set.of("BARBECHO", "RESTOS_VEGETALES", "HOJA", "COMPOST");
    static final Set<String> GRUPOS = Set.of("VIVA", "NATURAL", "RECICLADO", "METALICO", "CONVENCIONAL");

    private static final Pattern OPCION_TARJETA = Pattern.compile("<input[^>]*class=\"btn-check\"[^>]*>");
    private static final Pattern SELECT_ABONO = Pattern.compile("<select[^>]*id=\"abonoNatural\"[^>]*>(.*?)</select>", Pattern.DOTALL);
    private static final Pattern OPTION = Pattern.compile("<option[^>]*>");

    @Autowired FabricaDatos fabrica;
    @Autowired HuertoRepository huertos;
    @Autowired EspacioHuertoServicio espacios;
    @Autowired HuertoCerramientoServicio cerramientos;
    @Autowired MockMvc mvc;

    private static UsuarioAutenticado como(Usuario u) {
        return new UsuarioAutenticado(u);
    }

    /** Casillas y radios del formulario agrupados por name: valor -> marcado. */
    private static Map<String, Map<String, Boolean>> opciones(String html) {
        Map<String, Map<String, Boolean>> porCampo = new LinkedHashMap<>();
        Matcher m = OPCION_TARJETA.matcher(html);
        while (m.find()) {
            String input = m.group();
            porCampo.computeIfAbsent(atributo(input, "name"), k -> new LinkedHashMap<>())
                    .put(atributo(input, "value"), input.contains("checked"));
        }
        return porCampo;
    }

    /** Opciones reales del select de abono (sin la opción vacía «Selecciona un abono»): valor -> seleccionada. */
    private static Map<String, Boolean> abonos(String html) {
        Matcher s = SELECT_ABONO.matcher(html);
        assertThat(s.find()).as("select de abono").isTrue();
        Map<String, Boolean> r = new LinkedHashMap<>();
        Matcher o = OPTION.matcher(s.group(1));
        while (o.find()) {
            String valor = atributo(o.group(), "value");
            if (!valor.isEmpty()) r.put(valor, o.group().contains("selected"));
        }
        return r;
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

    /** Fija los datos del huerto por los servicios públicos del modelo (sin pasar por el formulario). */
    private void fijar(Escenario e, List<String> disenos, String suelo, String abono, List<String> grupos) {
        Huerto h = huertos.findById(e.huerto().getId()).orElseThrow();
        BigDecimal area = new BigDecimal("12.00");
        h.actualizar(h.getNombre(), h.getDescripcion(), h.getProvincia(), h.getParroquia(), h.getDireccion(), area,
                disenos.getFirst(), suelo, h.getFuenteAgua(), abono, h.getLatitud(), h.getLongitud());
        huertos.save(h);
        espacios.sincronizar(h.getId(), area, disenos);
        cerramientos.sincronizar(h.getId(), grupos);
    }

    private List<String> disenosGuardados(Escenario e) {
        return espacios.activos(e.huerto().getId()).stream().map(EspacioHuerto::getDiseno).toList();
    }

    private List<String> gruposGuardados(Escenario e) {
        return cerramientos.grupos(e.huerto().getId());
    }

    /** POST completo, con los datos de comunidad tal como están (la sección 2 no cambia). */
    private MockHttpServletRequestBuilder guardar(Escenario e, Usuario quien, List<String> disenos, String suelo,
                                                 String abono, List<String> grupos) {
        Comunidad c = e.comunidad();
        MockHttpServletRequestBuilder p = post("/gestor/huerto").with(user(como(quien))).with(csrf())
                .param("nombreComunidad", c.getNombre()).param("tipoComunidad", c.getTipoComunidad())
                .param("descripcionComunidad", c.getDescripcion()).param("provinciaComunidad", c.getProvincia())
                .param("cantonComunidad", c.getCanton()).param("direccionComunidad", "Calle sintética 1")
                .param("latitudComunidad", "0.23").param("longitudComunidad", "-78.26")
                .param("nombre", "Huerto editado " + e.comunidad().getId()).param("descripcion", "Descripción editada")
                .param("provincia", "Imbabura").param("parroquia", "San Luis").param("direccion", "Junto a la escuela")
                .param("areaM2", "12.50").param("latitud", "0.23").param("longitud", "-78.26")
                .param("fuenteAgua", "Canal").param("tipoSuelo", suelo).param("abonoNatural", abono);
        if (!disenos.isEmpty()) p.param("disenos", disenos.toArray(String[]::new));
        if (!grupos.isEmpty()) p.param("gruposCerramiento", grupos.toArray(String[]::new));
        return p;
    }

    @Test
    void muestraTodasLasOpcionesYMarcaLosValoresGuardados() throws Exception {
        Escenario e = fabrica.escenario(1);
        fijar(e, List.of("PISO_GRADAS", "VERTICAL_PAREDES"), "PANTANOSO", "COMPOST", List.of("VIVA", "CONVENCIONAL"));

        String html = formulario(e.gestorUsuario());
        Map<String, Map<String, Boolean>> r = opciones(html);

        assertThat(r.get("disenos").keySet()).containsExactlyInAnyOrderElementsOf(DISENOS);
        assertThat(r.get("tipoSuelo").keySet()).containsExactlyInAnyOrderElementsOf(SUELOS);
        assertThat(r.get("gruposCerramiento").keySet()).containsExactlyInAnyOrderElementsOf(GRUPOS);
        assertThat(abonos(html).keySet()).containsExactlyInAnyOrderElementsOf(ABONOS);

        assertThat(marcados(r.get("disenos"))).containsExactlyInAnyOrder("PISO_GRADAS", "VERTICAL_PAREDES");
        assertThat(marcados(r.get("tipoSuelo"))).containsExactly("PANTANOSO");
        assertThat(marcados(abonos(html))).containsExactly("COMPOST");
        assertThat(marcados(r.get("gruposCerramiento"))).containsExactlyInAnyOrder("VIVA", "CONVENCIONAL");

        // Diseños y cerca son casillas (varios a la vez); el suelo es una sola opción.
        assertThat(html).contains("type=\"checkbox\"").contains("type=\"radio\"")
                .contains("/img/huerto/disenos/PISO_GRADAS.jpg").contains("/img/huerto/suelos/PANTANOSO.jpg")
                .contains("/img/huerto/cerramientos/VIVA.jpg")
                .contains("list=\"fuentes-agua\"").contains("<option value=\"Río\">").contains("<option value=\"Goteo\">")
                .doesNotContain("http://").doesNotContain("https://");
        // La sección 2 sigue igual: el select de tipo de comunidad.
        assertThat(html).contains("id=\"tipoComunidad\"").contains("Datos de la comunidad");
    }

    @Test
    void huertoConValoresPorDefectoMarcaSuDisenoYNingunaCerca() throws Exception {
        Escenario e = fabrica.escenario(1);   // constructor corto: TIERRA_SURCOS, LIMOSO, abono «No especificado»
        String html = formulario(e.gestorUsuario());
        Map<String, Map<String, Boolean>> r = opciones(html);
        assertThat(marcados(r.get("disenos"))).containsExactly("TIERRA_SURCOS");
        assertThat(marcados(r.get("tipoSuelo"))).containsExactly("LIMOSO");
        assertThat(marcados(r.get("gruposCerramiento"))).isEmpty();
        // «No especificado» no es una opción: ninguna real queda seleccionada y la primera es la vacía.
        assertThat(marcados(abonos(html))).isEmpty();
        assertThat(html).contains("<option value=\"\">Selecciona un abono</option><option value=\"BARBECHO\">");
    }

    @Test
    void guardarVariosDisenosYGruposPersisteYQuedaMarcado() throws Exception {
        Escenario e = fabrica.escenario(1);
        mvc.perform(guardar(e, e.gestorUsuario(), List.of("COLGANTE_TECHOS", "PISO_MACETAS"), "GREDOSO",
                        "RESTOS_VEGETALES", List.of("VIVA", "RECICLADO")))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/gestor"));

        Huerto h = huertos.findById(e.huerto().getId()).orElseThrow();
        assertThat(h.getDiseno()).isEqualTo("COLGANTE_TECHOS");
        assertThat(h.getTipoSuelo()).isEqualTo("GREDOSO");
        assertThat(h.getAbonoNatural()).isEqualTo("RESTOS_VEGETALES");
        assertThat(h.getFuenteAgua()).isEqualTo("Canal");
        assertThat(disenosGuardados(e)).containsExactlyInAnyOrder("COLGANTE_TECHOS", "PISO_MACETAS");
        assertThat(gruposGuardados(e)).containsExactlyInAnyOrder("VIVA", "RECICLADO");

        String html = formulario(e.gestorUsuario());
        Map<String, Map<String, Boolean>> r = opciones(html);
        assertThat(marcados(r.get("disenos"))).containsExactlyInAnyOrder("COLGANTE_TECHOS", "PISO_MACETAS");
        assertThat(marcados(r.get("tipoSuelo"))).containsExactly("GREDOSO");
        assertThat(marcados(abonos(html))).containsExactly("RESTOS_VEGETALES");
        assertThat(marcados(r.get("gruposCerramiento"))).containsExactlyInAnyOrder("VIVA", "RECICLADO");

        // La cerca es opcional: desmarcar todos los grupos los elimina.
        mvc.perform(guardar(e, e.gestorUsuario(), List.of("COLGANTE_TECHOS"), "GREDOSO", "RESTOS_VEGETALES", List.of()))
                .andExpect(status().is3xxRedirection());
        assertThat(gruposGuardados(e)).isEmpty();
        assertThat(disenosGuardados(e)).containsExactly("COLGANTE_TECHOS");
    }

    @Test
    void guardarSinElegirAbonoSeRechazaYVuelveAlFormularioConElMensaje() throws Exception {
        Escenario e = fabrica.escenario(1);   // abono antiguo «No especificado»
        String html = mvc.perform(guardar(e, e.gestorUsuario(), List.of("PISO_MACETAS"), "ARENOSO", "", List.of()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Selecciona el abono natural utilizado.").contains("id=\"abonoNatural\"")
                .containsPattern("<option value=\"\"( selected=\"selected\")?>Selecciona un abono</option>");
        assertThat(marcados(abonos(html))).isEmpty();

        Huerto h = huertos.findById(e.huerto().getId()).orElseThrow();
        assertThat(h.getAbonoNatural()).isEqualTo("No especificado");
        assertThat(h.getAbonoNatural()).isNotEqualTo("BARBECHO");
        assertThat(h.getTipoSuelo()).isEqualTo("LIMOSO");
    }

    @Test
    void valoresInvalidosSeRechazanYNoCambianNada() throws Exception {
        Escenario e = fabrica.escenario(1);
        fijar(e, List.of("PISO_MACETAS"), "ARENOSO", "HOJA", List.of("NATURAL"));
        List<String> ok = List.of("PISO_MACETAS");
        List<String> cerca = List.of("NATURAL");

        // Validación del formulario (vuelve a mostrarlo con el error, 200).
        String html = mvc.perform(guardar(e, e.gestorUsuario(), List.of("DISEÑO_DESCONOCIDO"), "ARENOSO", "HOJA", cerca))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Selecciona un diseño válido.");
        html = mvc.perform(guardar(e, e.gestorUsuario(), List.of(), "ARENOSO", "HOJA", cerca))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Selecciona al menos un espacio para el huerto.");
        html = mvc.perform(guardar(e, e.gestorUsuario(), ok, "ARENOSO", "QUIMICO", cerca))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Selecciona un abono válido.");

        // Reglas del servicio (ReglaNegocioException -> 422): suelo, grupo de cerca y diseños adicionales.
        mvc.perform(guardar(e, e.gestorUsuario(), ok, "FRANCO", "HOJA", cerca)).andExpect(status().is(422));
        mvc.perform(guardar(e, e.gestorUsuario(), ok, "ARENOSO", "HOJA", List.of("MATERIAL_DESCONOCIDO")))
                .andExpect(status().is(422));
        mvc.perform(guardar(e, e.gestorUsuario(), ok, "ARENOSO", "HOJA", List.of("VIVO_CAPULIES")))   // código detallado anterior
                .andExpect(status().is(422));
        mvc.perform(guardar(e, e.gestorUsuario(), List.of("PISO_MACETAS", "DISEÑO_DESCONOCIDO"), "ARENOSO", "HOJA", cerca))
                .andExpect(status().is(422));

        Huerto h = huertos.findById(e.huerto().getId()).orElseThrow();
        assertThat(List.of(h.getNombre(), h.getDiseno(), h.getTipoSuelo(), h.getAbonoNatural()))
                .containsExactly(e.huerto().getNombre(), "PISO_MACETAS", "ARENOSO", "HOJA");
        assertThat(disenosGuardados(e)).containsExactly("PISO_MACETAS");
        assertThat(gruposGuardados(e)).containsExactly("NATURAL");
    }

    @Test
    void otroGestorSoloVeYEditaSuHuertoYElParticipanteRecibe403() throws Exception {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        fijar(a, List.of("VERTICAL_PAREDES", "TIERRA_CAMADAS"), "MARGOSO", "BARBECHO", List.of("METALICO"));

        // El formulario de B muestra el huerto de B, nunca el de A.
        String deB = formulario(b.gestorUsuario());
        assertThat(deB).contains(b.huerto().getNombre()).doesNotContain(a.huerto().getNombre());
        assertThat(marcados(opciones(deB).get("gruposCerramiento"))).isEmpty();

        // B envía el formulario: solo cambia su propio huerto (la ruta no recibe id de huerto).
        mvc.perform(guardar(b, b.gestorUsuario(), List.of("PISO_BALCONES"), "ARCILLOSO", "COMPOST", List.of("RECICLADO")))
                .andExpect(status().is3xxRedirection());
        Huerto deA = huertos.findById(a.huerto().getId()).orElseThrow();
        assertThat(List.of(deA.getNombre(), deA.getDiseno(), deA.getTipoSuelo(), deA.getAbonoNatural()))
                .containsExactly(a.huerto().getNombre(), "VERTICAL_PAREDES", "MARGOSO", "BARBECHO");
        assertThat(disenosGuardados(a)).containsExactlyInAnyOrder("VERTICAL_PAREDES", "TIERRA_CAMADAS");
        assertThat(gruposGuardados(a)).containsExactly("METALICO");
        assertThat(disenosGuardados(b)).containsExactly("PISO_BALCONES");
        assertThat(gruposGuardados(b)).containsExactly("RECICLADO");

        // Un participante de A no puede ver ni enviar el formulario.
        Usuario participante = a.participanteUsuarios().get(0);
        mvc.perform(get("/gestor/huerto").with(user(como(participante)))).andExpect(status().isForbidden());
        mvc.perform(guardar(a, participante, List.of("PISO_BALCONES"), "ARCILLOSO", "COMPOST", List.of("RECICLADO")))
                .andExpect(status().isForbidden());
        assertThat(disenosGuardados(a)).containsExactlyInAnyOrder("VERTICAL_PAREDES", "TIERRA_CAMADAS");
        assertThat(gruposGuardados(a)).containsExactly("METALICO");
    }
}
