package ec.edu.epn.huertos.actividades;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ParticipanteController {

    private final ParticipanteServicio participantes;

    public ParticipanteController(ParticipanteServicio participantes) {
        this.participantes = participantes;
    }

    @GetMapping("/participante")
    String panel(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("panel", participantes.panel(usuario.getId()));
        return "participante/panel";
    }
}
