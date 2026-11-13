package ec.edu.epn.huertos.huertos.presentacion;

import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;
import ec.edu.epn.huertos.huertos.aplicacion.RevisionSiembraServicio;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RevisionSiembraController {

    private final RevisionSiembraServicio servicio;

    public RevisionSiembraController(RevisionSiembraServicio servicio) {

        this.servicio = servicio;
    }

    /** Muestra las siembras pendientes de revisión. */
    @GetMapping("/revision/siembras")
    public String bandeja(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {

        model.addAttribute("items", servicio.pendientes(usuario.getId()));

        return "revision/siembras";
    }

    /** Permite aprobar o rechazar una siembra. */
    @PostMapping("/revision/siembras/{id}/decision")
    public String decidir(
            @PathVariable Long id,
            @RequestParam EstadoRevision decision,
            @RequestParam(required = false) String observaciones,
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            RedirectAttributes flash) {

        try {

            servicio.decidir(usuario.getId(), id, decision, observaciones);

            if (decision == EstadoRevision.APROBADA) {

                flash.addFlashAttribute(
                        "mensaje", "Siembra aprobada. La planta conserva su crecimiento.");

            } else if (decision == EstadoRevision.RECHAZADA) {

                flash.addFlashAttribute(
                        "mensaje", "Siembra rechazada. La planta fue retirada del huerto.");
            }

        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/revision/siembras";
    }
}
