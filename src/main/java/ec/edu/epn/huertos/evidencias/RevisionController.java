package ec.edu.epn.huertos.evidencias;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RevisionController {

    private final RevisionServicio revision;

    public RevisionController(RevisionServicio revision) {
        this.revision = revision;
    }

    @GetMapping("/revision")
    String bandeja(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("items", revision.bandeja(usuario.getId()));
        return "revision/bandeja";
    }

    @GetMapping("/revision/envios/{envioId}")
    String detalle(@PathVariable Long envioId, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("revision", revision.detalle(usuario.getId(), envioId));
        return "revision/detalle";
    }

    @PostMapping("/revision/envios/{envioId}/decision")
    String decidir(@PathVariable Long envioId, @RequestParam EstadoRevision decision,
                   @RequestParam(required = false) String observaciones,
                   @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        try {
            RevisionServicio.Resultado resultado = revision.decidir(usuario.getId(), envioId, decision, observaciones);
            flash.addFlashAttribute("mensaje", resultado.decision() == EstadoRevision.APROBADA
                    ? "Evidencia aprobada. Se registraron los puntos y el progreso."
                    : "Evidencia rechazada. El gestor podrá corregirla y reenviarla.");
            return "redirect:/revision";
        } catch (ConflictoException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/revision";
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/revision/envios/" + envioId;
        }
    }

    @GetMapping("/revision/historial")
    String historial(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("items", revision.historial(usuario.getId()));
        return "revision/historial";
    }
}
