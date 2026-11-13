package ec.edu.epn.huertos.comunidades.presentacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.comunidades.aplicacion.AltaComunidadServicio;
import ec.edu.epn.huertos.comunidades.aplicacion.FormularioAltaComunidad;
import ec.edu.epn.huertos.comunidades.aplicacion.SolicitudComunidadServicio;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AltaComunidadController {
    private final AltaComunidadServicio altas;
    private final SolicitudComunidadServicio solicitudes;
    private static final List<List<String>> DISENOS = List.of(
            List.of("TIERRA_SURCOS", "En tierra · surcos", "TIERRA_SURCOS.jpg"),
            List.of("TIERRA_CAMADAS", "En tierra · camadas", "TIERRA_CAMADAS.jpg"),
            List.of("TIERRA_CAMELLONES", "En tierra · camellones", "TIERRA_CAMELLONES.jpg"),
            List.of("TIERRA_TERRAZAS", "En tierra · terrazas", "TIERRA_TERRAZAS.jpg"),
            List.of("VERTICAL_PAREDES", "Vertical · paredes", "VERTICAL_PAREDES.jpg"),
            List.of("COLGANTE_TECHOS", "Colgante · techos", "COLGANTE_TECHOS.jpg"),
            List.of("PISO_MACETAS", "En piso · macetas", "PISO_MACETAS.jpg"),
            List.of("PISO_BOTELLAS_PATIO", "En piso · botellas en patio", "PISO_BOTELLAS_PATIO.jpg"),
            List.of("PISO_LLANTAS_PATIO", "En piso · llantas en patio", "PISO_LLANTAS_PATIO.jpg"),
            List.of("PISO_TERRAZA", "En piso · terraza", "PISO_TERRAZA.jpg"),
            List.of("PISO_GRADAS", "En piso · gradas", "PISO_GRADAS.jpg"),
            List.of("PISO_BALCONES", "En piso · balcones", "PISO_BALCONES.jpg"),
            List.of("PISO_FILOS_VENTANA", "En piso · filos de ventanas", "PISO_FILOS_VENTANA.jpg"));
    private static final List<List<String>> SUELOS = List.of(
            List.of("ARCILLOSO", "Arcilloso", "ARCILLOSO.jpg"), List.of("LIMOSO", "Limoso", "LIMOSO.jpg"),
            List.of("ARENOSO", "Arenoso", "ARENOSO.jpg"), List.of("MARGOSO", "Margoso", "MARGOSO.jpg"),
            List.of("GREDOSO", "Gredoso", "GREDOSO.jpg"), List.of("PANTANOSO", "Pantanoso", "PANTANOSO.jpg"));
    private static final List<List<String>> CERRAMIENTOS = List.of(
            List.of("VIVA", "Cerca viva", "Higuerilla, floripondio, agave, lavanda, naranjos, capulí, tocte o guabo", "VIVA.jpg"),
            List.of("NATURAL", "Material natural", "Caña guadua, bahareque o tapial", "NATURAL.jpg"),
            List.of("RECICLADO", "Material reciclado", "Tablas, botellas plásticas o neumáticos", "RECICLADO.jpg"),
            List.of("METALICO", "Material metálico", "Alambre de púas", "METALICO.jpg"),
            List.of("CONVENCIONAL", "Material convencional", "Piedra, ladrillo o bloque", "CONVENCIONAL.jpg"));
    private static final List<List<String>> ABONOS = List.of(List.of("BARBECHO", "Barbecho"),
            List.of("RESTOS_VEGETALES", "Restos vegetales"), List.of("HOJA", "Hoja"), List.of("COMPOST", "Compost"));
    private static final List<List<String>> TIPOS_COMUNIDAD = List.of(List.of("PERSONA", "Solo yo · máximo 1 integrante"),
            List.of("FAMILIA", "Familia · máximo 10 integrantes"), List.of("CONDOMINIO", "Condominio · máximo 20 integrantes"), List.of("BARRIO", "Barrio · máximo 25 integrantes"));
    public AltaComunidadController(AltaComunidadServicio altas, SolicitudComunidadServicio solicitudes) {
        this.altas = altas;
        this.solicitudes = solicitudes;
    }

    @GetMapping("/cuenta/comunidad/nueva")
    String nueva(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        if (usuario.getRol() != Rol.SIN_ASIGNAR) return "error/403";
        if (!model.containsAttribute("formulario")) model.addAttribute("formulario", new FormularioAltaComunidad());
        agregarOpciones(model);
        return "cuenta/comunidad-form";
    }

    @PostMapping("/cuenta/comunidad")
    String crear(@Valid @ModelAttribute("formulario") FormularioAltaComunidad formulario, BindingResult resultado,
                 @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        if (resultado.hasErrors()) { agregarOpciones(model); return "cuenta/comunidad-form"; }
        try {
            solicitudes.solicitarCreacion(usuario.getId(), formulario);
            return "redirect:/cuenta/solicitudes-comunidad?creacionEnviada";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            agregarOpciones(model);
            return "cuenta/comunidad-form";
        }
    }

    private void agregarOpciones(Model model) {
        model.addAttribute("disenos", DISENOS);
        model.addAttribute("suelos", SUELOS);
        model.addAttribute("cerramientos", CERRAMIENTOS);
        model.addAttribute("abonos", ABONOS);
        model.addAttribute("tiposComunidad", TIPOS_COMUNIDAD);
    }
}
