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

    private final HuertoServicio huertos;

    public GestorHuertoController(HuertoServicio huertos) {
        this.huertos = huertos;
    }

    @GetMapping("/gestor/huerto")
    String huerto(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        var huerto = huertos.huertoDeGestor(usuario.getId());
        model.addAttribute("form", FormularioHuerto.de(huerto,
                huertos.comunidadDeGestor(usuario.getId())));
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
        model.addAttribute("form", new FormularioPlanta());
        model.addAttribute("accion", "/gestor/plantas");
        return "gestor/planta-form";
    }

    @PostMapping("/gestor/plantas")
    String crear(@Valid @ModelAttribute("form") FormularioPlanta form, BindingResult resultado,
                 @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            model.addAttribute("accion", "/gestor/plantas");
            return "gestor/planta-form";
        }
        huertos.crearPlanta(usuario.getId(), form);
        flash.addFlashAttribute("mensaje", "Planta registrada.");
        return "redirect:/gestor/plantas";
    }

    @GetMapping("/gestor/plantas/{id}/editar")
    String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("form", FormularioPlanta.de(huertos.plantaDeGestor(usuario.getId(), id)));
        model.addAttribute("accion", "/gestor/plantas/" + id);
        return "gestor/planta-form";
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
            flash.addFlashAttribute("mensaje", plantada ? "La planta aparecerá en el huerto virtual." : "La planta se retiró del huerto virtual.");
        } catch (ec.edu.epn.huertos.compartido.ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestor/plantas";
    }
}
