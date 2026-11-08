package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
public class ParticipanteController {

    private final ParticipanteServicio participantes;
    private final ComunidadServicio comunidades;

    public ParticipanteController(ParticipanteServicio participantes, ComunidadServicio comunidades) {
        this.participantes = participantes;
        this.comunidades = comunidades;
    }

    @GetMapping("/participante")
    String panel(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("panel", participantes.panel(usuario.getId()));
        return "participante/panel";
    }

    @PostMapping("/participante/comunidad/salir")
    String salir(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest request,
                 HttpServletResponse response, Authentication authentication) {
        comunidades.dejarComunidad(usuario.getId());
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?comunidadDejada";
    }
}
