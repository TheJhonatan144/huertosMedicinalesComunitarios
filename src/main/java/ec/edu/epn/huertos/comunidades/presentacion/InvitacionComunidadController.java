package ec.edu.epn.huertos.comunidades.presentacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InvitacionComunidadController {
    private final ComunidadServicio comunidades;
    public InvitacionComunidadController(ComunidadServicio comunidades) { this.comunidades = comunidades; }

    @GetMapping("/cuenta/invitaciones")
    String invitaciones(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("invitaciones", comunidades.invitacionesRecibidas(usuario.getId()));
        return "cuenta/invitaciones";
    }

    @PostMapping("/cuenta/invitaciones/{id}/aceptar")
    String aceptar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                   HttpServletRequest request, HttpServletResponse response, Authentication authentication,
                   RedirectAttributes flash) {
        try {
            comunidades.aceptarInvitacion(usuario.getId(), id);
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?invitacionAceptada";
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta/invitaciones";
        }
    }

    @PostMapping("/cuenta/invitaciones/{id}/rechazar")
    String rechazar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                    RedirectAttributes flash) {
        comunidades.rechazarInvitacion(usuario.getId(), id);
        flash.addFlashAttribute("mensaje", "Invitación rechazada.");
        return "redirect:/cuenta/invitaciones";
    }
}
