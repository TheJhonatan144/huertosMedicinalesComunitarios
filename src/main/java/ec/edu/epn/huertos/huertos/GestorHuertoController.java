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

/** Datos del huerto y gestión básica de sus plantas (solo el gestor de la comunidad). */
@Controller
public class GestorHuertoController {

    private final HuertoServicio huertos;

    public GestorHuertoController(HuertoServicio huertos) {
        this.huertos = huertos;
    }

    @GetMapping("/gestor/huerto")
    String huerto(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("form", FormularioHuerto.de(huertos.huertoDeGestor(usuario.getId())));
        return "gestor/huerto-form";
    }

    @PostMapping("/gestor/huerto")
    String guardarHuerto(@Valid @ModelAttribute("form") FormularioHuerto form, BindingResult resultado,
                         @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            return "gestor/huerto-form";
        }
        huertos.actualizarHuerto(usuario.getId(), form);
        flash.addFlashAttribute("mensaje", "Datos del huerto actualizados.");
        return "redirect:/gestor";
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
}
