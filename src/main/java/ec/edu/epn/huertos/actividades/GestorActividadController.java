package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.Fechas;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GestorActividadController {

    private final ActividadServicio actividades;
    private final DetalleActividadServicio detalles;

    public GestorActividadController(ActividadServicio actividades, DetalleActividadServicio detalles) {
        this.actividades = actividades;
        this.detalles = detalles;
    }

    @GetMapping("/gestor/actividades")
    String lista(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("actividades", actividades.listar(usuario.getId()));
        return "gestor/actividades";
    }

    @GetMapping("/gestor/actividades/nueva")
    String nueva(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        FormularioActividad form = new FormularioActividad();
        form.setFecha(Fechas.hoy());
        prepararFormulario(model, usuario, form, null);
        return "gestor/actividad-form";
    }

    @PostMapping("/gestor/actividades")
    String crear(@Valid @ModelAttribute("form") FormularioActividad form, BindingResult resultado,
                 @RequestParam(name = "foto", required = false) MultipartFile foto,
                 @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                Long id = actividades.registrar(usuario.getId(), form, foto);
                flash.addFlashAttribute("mensaje", "Actividad registrada. Quedó pendiente de revisión.");
                return "redirect:/gestor/actividades/" + id;
            } catch (ReglaNegocioException e) {
                resultado.reject("regla", e.getMessage());
            }
        }
        prepararFormulario(model, usuario, form, null);
        return "gestor/actividad-form";
    }

    @GetMapping("/gestor/actividades/{id}")
    String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        Actividad actividad = actividades.actividadDeGestor(usuario.getId(), id);
        DetalleActividad detalle = detalles.construir(actividad);
        model.addAttribute("detalle", detalle);
        FormularioActividad form = new FormularioActividad();
        form.setTipo(detalle.tipo());
        form.setDescripcion(detalle.descripcion());
        form.setFecha(detalle.fechaIso());
        model.addAttribute("form", form);
        model.addAttribute("tipos", TipoActividad.values());
        model.addAttribute("hoy", Fechas.hoy());
        return "gestor/actividad-detalle";
    }

    @GetMapping("/gestor/actividades/{id}/editar")
    String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        Actividad actividad = actividades.actividadDeGestor(usuario.getId(), id);
        FormularioActividad form = new FormularioActividad();
        form.setTipo(actividad.getTipo());
        form.setDescripcion(actividad.getDescripcion());
        form.setFecha(actividad.getFechaActividad());
        prepararFormulario(model, usuario, form, detalles.construir(actividad));
        return "gestor/actividad-form";
    }

    @PostMapping("/gestor/actividades/{id}/editar")
    String guardarEdicion(@PathVariable Long id, @Valid @ModelAttribute("form") FormularioActividad form,
                          BindingResult resultado, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model,
                          RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                actividades.editar(usuario.getId(), id, form);
                flash.addFlashAttribute("mensaje", "Actividad actualizada.");
                return "redirect:/gestor/actividades/" + id;
            } catch (ReglaNegocioException e) {
                resultado.reject("regla", e.getMessage());
            }
        }
        prepararFormulario(model, usuario, form, detalles.construir(actividades.actividadDeGestor(usuario.getId(), id)));
        return "gestor/actividad-form";
    }

    @PostMapping("/gestor/actividades/{id}/reenviar")
    String reenviar(@PathVariable Long id, @Valid @ModelAttribute("form") FormularioActividad form,
                    BindingResult resultado, @RequestParam(name = "comentario", required = false) String comentario,
                    @RequestParam(name = "foto", required = false) MultipartFile foto,
                    @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        try {
            if (resultado.hasErrors()) {
                throw new ReglaNegocioException("Revisa los datos de la corrección: "
                        + resultado.getFieldErrors().get(0).getDefaultMessage());
            }
            actividades.reenviar(usuario.getId(), id, form, comentario, foto);
            flash.addFlashAttribute("mensaje", "Corrección reenviada. La actividad volvió a quedar pendiente de revisión.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestor/actividades/" + id;
    }

    private void prepararFormulario(Model model, UsuarioAutenticado usuario, FormularioActividad form,
                                    DetalleActividad existente) {
        model.addAttribute("form", form);
        model.addAttribute("tipos", TipoActividad.values());
        model.addAttribute("hoy", Fechas.hoy());
        model.addAttribute("existente", existente);
        if (existente == null) {
            model.addAttribute("elegibles", actividades.participantesElegibles(usuario.getId()));
        }
    }
}
