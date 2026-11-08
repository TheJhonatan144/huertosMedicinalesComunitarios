package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminTecnicoController {
    private final AdminTecnicoServicio servicio;
    private final ec.edu.epn.huertos.autenticacion.UsuarioRepository usuarios;

    public AdminTecnicoController(AdminTecnicoServicio servicio,
                                  ec.edu.epn.huertos.autenticacion.UsuarioRepository usuarios) {
        this.servicio = servicio; this.usuarios = usuarios;
    }

    @GetMapping("/admin-tecnico")
    String panel(@AuthenticationPrincipal UsuarioAutenticado actor, Model model) {
        model.addAttribute("revisores", servicio.revisores(actor.getId()));
        model.addAttribute("comunidades", servicio.comunidades(actor.getId()));
        model.addAttribute("revisoresActivos", usuarios.findByRolOrderByNombreCompleto(ec.edu.epn.huertos.autenticacion.Rol.ADMIN_MINISTERIO_SALUD)
                .stream().filter(ec.edu.epn.huertos.autenticacion.Usuario::isActivo).toList());
        return "admin-tecnico/panel";
    }

    @PostMapping("/admin-tecnico/revisores")
    String crear(@AuthenticationPrincipal UsuarioAutenticado actor, @RequestParam String nombreUsuario,
                 @RequestParam String nombre, @RequestParam String correo, @RequestParam String claveTemporal,
                 RedirectAttributes flash) {
        try {
            servicio.crearRevisor(actor.getId(), nombreUsuario, nombre, correo, claveTemporal);
            flash.addFlashAttribute("mensaje", "Cuenta MSP creada. Comparte la clave inicial por un canal institucional seguro.");
        } catch (RuntimeException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin-tecnico";
    }

    @PostMapping("/admin-tecnico/comunidades/{id}/reasignar")
    String reasignar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actor,
                     @RequestParam Long revisorId, @RequestParam String motivo, RedirectAttributes flash) {
        try {
            servicio.reasignar(actor.getId(), id, revisorId, motivo);
            flash.addFlashAttribute("mensaje", "Comunidad reasignada; el cambio quedó registrado.");
        } catch (RuntimeException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin-tecnico";
    }
}
