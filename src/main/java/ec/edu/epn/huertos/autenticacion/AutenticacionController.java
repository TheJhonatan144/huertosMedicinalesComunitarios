package ec.edu.epn.huertos.autenticacion;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutenticacionController {

    @GetMapping("/login")
    String login(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario != null ? "redirect:/panel" : "auth/login";
    }

    /** Redirige al panel que corresponde al rol del usuario. */
    @GetMapping("/panel")
    String panel(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return switch (usuario.getRol()) {
            case GESTOR_HUERTO -> "redirect:/gestor";
            case PARTICIPANTE -> "redirect:/participante";
            case ADMIN_EVIDENCIAS -> "redirect:/revision";
        };
    }
}
