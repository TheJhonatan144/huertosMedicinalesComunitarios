package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class AltaComunidadController {
    private final AltaComunidadServicio altas;
    private final SolicitudComunidadServicio solicitudes;
    private static final List<List<String>> DISENOS = List.of(
            List.of("TIERRA_SURCOS", "En tierra · surcos"), List.of("TIERRA_CAMADAS", "En tierra · camadas"),
            List.of("TIERRA_CAMELLONES", "En tierra · camellones"), List.of("TIERRA_TERRAZAS", "En tierra · terrazas"),
            List.of("VERTICAL_PAREDES", "Vertical · paredes"), List.of("COLGANTE_TECHOS", "Colgante · techos"),
            List.of("PISO_MACETAS", "En piso · macetas"), List.of("PISO_BOTELLAS_PATIO", "En piso · botellas en patio"),
            List.of("PISO_LLANTAS_PATIO", "En piso · llantas en patio"), List.of("PISO_TERRAZA", "En piso · terraza"),
            List.of("PISO_GRADAS", "En piso · gradas"), List.of("PISO_BALCONES", "En piso · balcones"),
            List.of("PISO_FILOS_VENTANA", "En piso · filos de ventanas"));
    private static final List<List<String>> SUELOS = List.of(
            List.of("ARCILLOSO", "Arcilloso"), List.of("LIMOSO", "Limoso"), List.of("ARENOSO", "Arenoso"),
            List.of("MARGOSO", "Margoso"), List.of("GREDOSO", "Gredoso"), List.of("PANTANOSO", "Pantanoso"));
    private static final List<List<String>> CERRAMIENTOS = List.of(
            List.of("VIVO_HIGUERILLA", "Vivo · higuerilla"), List.of("VIVO_FLORIPONDIO", "Vivo · floripondio"),
            List.of("VIVO_AGAVE", "Vivo · agave"), List.of("VIVO_LAVANDA", "Vivo · lavanda"),
            List.of("VIVO_NARANJOS", "Árbol frutal · naranjos"), List.of("VIVO_CAPULIES", "Árbol frutal · capulíes"),
            List.of("VIVO_TOCTE", "Árbol frutal · tocte"), List.of("VIVO_GUABO", "Árbol frutal · guabo"),
            List.of("NATURAL_CANA_GUADUA", "Natural · caña guadua"), List.of("NATURAL_BAHAREQUE", "Natural · bahareque"),
            List.of("NATURAL_TAPIAL", "Natural · tapial"), List.of("RECICLADO_TABLAS", "Reciclado · tablas"),
            List.of("RECICLADO_BOTELLAS", "Reciclado · botellas plásticas"), List.of("RECICLADO_NEUMATICOS", "Reciclado · llantas"),
            List.of("METALICO_ALAMBRE_PUAS", "Metálico · alambre de púas"), List.of("CONVENCIONAL_PIEDRA", "Convencional · piedra"),
            List.of("CONVENCIONAL_LADRILLO", "Convencional · ladrillo"), List.of("CONVENCIONAL_BLOQUE", "Convencional · bloque"));
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
