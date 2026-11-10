package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

/** Datos del huerto y gestión básica de sus plantas (solo el gestor de la comunidad). */
@Controller
public class GestorHuertoController {

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

    private final HuertoServicio huertos;

    public GestorHuertoController(HuertoServicio huertos) {
        this.huertos = huertos;
    }

    @GetMapping("/gestor/huerto")
    String huerto(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        var huerto = huertos.huertoDeGestor(usuario.getId());
        List<String> disenosGuardados = huertos.espaciosDeGestor(usuario.getId()).stream().map(EspacioHuerto::getDiseno).toList();
        FormularioHuerto formulario = FormularioHuerto.de(huerto, huertos.comunidadDeGestor(usuario.getId()), disenosGuardados,
                huertos.gruposCerramientoDeGestor(usuario.getId()));
        model.addAttribute("form", formulario);
        model.addAttribute("disenosGuardados", disenosGuardados);
        agregarOpciones(model);
        return "gestor/huerto-form";
    }

    @PostMapping("/gestor/huerto")
    String guardarHuerto(@Valid @ModelAttribute("form") FormularioHuerto form, BindingResult resultado,
                         @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            agregarOpciones(model);
            return "gestor/huerto-form";
        }
        huertos.actualizarHuerto(usuario.getId(), form);
        flash.addFlashAttribute("mensaje", "Datos del huerto actualizados.");
        return "redirect:/gestor";
    }

    private void agregarOpciones(Model model) {
        model.addAttribute("disenos", DISENOS); model.addAttribute("suelos", SUELOS);
        model.addAttribute("cerramientos", CERRAMIENTOS);
        model.addAttribute("abonos", ABONOS);
    }

    @GetMapping("/gestor/plantas")
    String plantas(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("plantas", huertos.plantasDeGestor(usuario.getId()));
        return "gestor/plantas";
    }

    @GetMapping("/gestor/plantas/nueva")
    String nueva(Model model) {
        return "redirect:/gestor/plantas/catalogo";
    }

    @GetMapping("/gestor/plantas/{id}/editar")
    String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        huertos.plantaDeGestor(usuario.getId(), id);
        return "redirect:/gestor/plantas/" + id + "/siembra";
    }

    @PostMapping("/gestor/plantas/{id}")
    String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") FormularioPlanta form,
                      BindingResult resultado, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model,
                      RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            model.addAttribute("accion", "/gestor/plantas/" + id);
            return "gestor/planta-form";
        }
        huertos.actualizarPlanta(usuario.getId(), id, form);
        flash.addFlashAttribute("mensaje", "Planta actualizada.");
        return "redirect:/gestor/plantas";
    }

    @PostMapping("/gestor/plantas/{id}/estado")
    String estado(@PathVariable Long id, @RequestParam boolean activa,
                  @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        huertos.cambiarEstadoPlanta(usuario.getId(), id, activa);
        flash.addFlashAttribute("mensaje", activa ? "Planta activada." : "Planta desactivada.");
        return "redirect:/gestor/plantas";
    }

    @PostMapping("/gestor/plantas/{id}/plantada")
    String plantada(@PathVariable Long id, @RequestParam boolean plantada,
                    @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        try {
            huertos.cambiarPlantada(usuario.getId(), id, plantada);
            flash.addFlashAttribute("mensaje", plantada ? "Planta marcada como plantada." : "La planta ya no está marcada como plantada.");
        } catch (ec.edu.epn.huertos.compartido.ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestor/plantas";
    }

    @PostMapping("/gestor/plantas/{id}/eliminar")
    String eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                    RedirectAttributes flash) {
        huertos.eliminarPlanta(usuario.getId(), id);
        flash.addFlashAttribute("mensaje", "Planta eliminada de tu huerto.");
        return "redirect:/gestor/plantas";
    }
}
