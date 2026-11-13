package ec.edu.epn.huertos.comunidades.presentacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CuentaArchivadasController {
    private final ComunidadServicio comunidades;
    private final ec.edu.epn.huertos.autenticacion.aplicacion.SesionServicio sesiones;

    public CuentaArchivadasController(ComunidadServicio comunidades, ec.edu.epn.huertos.autenticacion.aplicacion.SesionServicio sesiones) {
        this.comunidades = comunidades;
        this.sesiones = sesiones;
    }

    @GetMapping("/cuenta/archivadas")
    String archivadas(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("archivadas", comunidades.archivadasDe(usuario.getId()));
        return "auth/archivadas";
    }

    @PostMapping("/cuenta/archivadas/{id}/restaurar")
    String restaurar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                     HttpServletRequest request, RedirectAttributes flash) {
        comunidades.restaurarComunidad(usuario.getId(), id);
        var actualizada = sesiones.principalActual(usuario.getId());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(new UsernamePasswordAuthenticationToken(actualizada, null, actualizada.getAuthorities()));
        SecurityContextHolder.setContext(contexto);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, contexto);
        flash.addFlashAttribute("mensaje", "La comunidad está activa otra vez. Sus integrantes deberán recibir y aceptar nuevas invitaciones.");
        return "redirect:/gestor";
    }
}
