package ec.edu.epn.huertos.comunidades.presentacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.comunidades.aplicacion.FormularioRechazoSolicitud;
import ec.edu.epn.huertos.comunidades.aplicacion.FormularioSolicitudEliminacion;
import ec.edu.epn.huertos.comunidades.aplicacion.SolicitudComunidadServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SolicitudesComunidadController {
    private final SolicitudComunidadServicio solicitudes;

    public SolicitudesComunidadController(SolicitudComunidadServicio solicitudes) { this.solicitudes = solicitudes; }

    @GetMapping("/revision/solicitudes-comunidad")
    String pendientes(Model model) {
        model.addAttribute("solicitudes", solicitudes.pendientes());
        return "revision/solicitudes-comunidad";
    }

    @PostMapping("/revision/solicitudes-comunidad/{id}/aprobar")
    String aprobar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        try {
            solicitudes.aprobar(usuario.getId(), id);
            flash.addFlashAttribute("mensaje", "Solicitud aprobada y procesada.");
        } catch (ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException | IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/revision/solicitudes-comunidad";
    }

    @PostMapping("/revision/solicitudes-comunidad/{id}/rechazar")
    String rechazar(@PathVariable Long id, @Valid @ModelAttribute("formulario") FormularioRechazoSolicitud formulario,
                    BindingResult resultado, @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            flash.addFlashAttribute("error", "Debes indicar el motivo del rechazo.");
            return "redirect:/revision/solicitudes-comunidad";
        }
        try {
            solicitudes.rechazar(usuario.getId(), id, formulario.getMotivo());
            flash.addFlashAttribute("mensaje", "Solicitud rechazada. El motivo quedó registrado para la persona solicitante.");
        } catch (ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException | IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/revision/solicitudes-comunidad";
    }

    @GetMapping("/cuenta/solicitudes-comunidad")
    String deUsuario(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("solicitudes", solicitudes.deUsuario(usuario.getId()));
        return "cuenta/solicitudes-comunidad";
    }

    @PostMapping("/comunidades/{id}/solicitar-eliminacion")
    String solicitarEliminacion(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
            @ModelAttribute FormularioSolicitudEliminacion formulario, RedirectAttributes flash) {
        try {
            solicitudes.solicitarEliminacion(usuario.getId(), id, formulario.getMotivo());
            flash.addFlashAttribute("mensaje", "La solicitud de eliminación se envió al Ministerio para su aprobación.");
            return "redirect:/cuenta/solicitudes-comunidad";
        } catch (ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta/solicitudes-comunidad";
        }
    }
}
