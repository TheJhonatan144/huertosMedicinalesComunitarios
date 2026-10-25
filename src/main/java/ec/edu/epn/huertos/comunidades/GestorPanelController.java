package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.actividades.ActividadResumen;
import ec.edu.epn.huertos.actividades.ActividadServicio;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import ec.edu.epn.huertos.evidencias.EstadoRevision;
import ec.edu.epn.huertos.gamificacion.GamificacionServicio;
import ec.edu.epn.huertos.huertos.Huerto;
import ec.edu.epn.huertos.huertos.HuertoServicio;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Panel del gestor de huerto y administración de integrantes de su comunidad. */
@Controller
public class GestorPanelController {

    private final ComunidadServicio comunidades;
    private final HuertoServicio huertos;
    private final ActividadServicio actividades;
    private final GamificacionServicio gamificacion;

    public GestorPanelController(ComunidadServicio comunidades, HuertoServicio huertos, ActividadServicio actividades,
                                 GamificacionServicio gamificacion) {
        this.comunidades = comunidades;
        this.huertos = huertos;
        this.actividades = actividades;
        this.gamificacion = gamificacion;
    }

    @GetMapping("/gestor")
    String panel(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        Integrante gestor = comunidades.gestorDe(usuario.getId());
        Comunidad comunidad = comunidades.comunidad(gestor.getComunidadId());
        Huerto huerto = huertos.huertoDeGestor(usuario.getId());
        List<ActividadResumen> lista = actividades.listar(usuario.getId());
        model.addAttribute("comunidad", comunidad);
        model.addAttribute("huerto", huerto);
        model.addAttribute("activos", comunidades.activos(comunidad.getId()));
        model.addAttribute("cupo", comunidades.cupoMaximo());
        model.addAttribute("puntosComunitarios", gamificacion.puntosComunitarios(comunidad.getId()));
        model.addAttribute("progreso", gamificacion.progreso(comunidad.getId()));
        model.addAttribute("misPuntos", gamificacion.resumenIndividual(gestor.getId()));
        model.addAttribute("pendientes", lista.stream().filter(a -> a.estado() == EstadoRevision.PENDIENTE).count());
        model.addAttribute("rechazadas", lista.stream().filter(a -> a.estado() == EstadoRevision.RECHAZADA).count());
        model.addAttribute("aprobadas", lista.stream().filter(a -> a.estado() == EstadoRevision.APROBADA).count());
        return "gestor/panel";
    }

    @GetMapping("/gestor/integrantes")
    String integrantes(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        Integrante gestor = comunidades.gestorDe(usuario.getId());
        model.addAttribute("comunidad", comunidades.comunidad(gestor.getComunidadId()));
        model.addAttribute("integrantes", comunidades.integrantesDeMiComunidad(usuario.getId()));
        model.addAttribute("activos", comunidades.activos(gestor.getComunidadId()));
        model.addAttribute("cupo", comunidades.cupoMaximo());
        return "gestor/integrantes";
    }

    @PostMapping("/gestor/integrantes/{id}/baja")
    String baja(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        return ejecutar(flash, () -> comunidades.darDeBaja(usuario.getId(), id), "Integrante dado de baja. Se liberó un cupo.");
    }

    @PostMapping("/gestor/integrantes/{id}/reactivar")
    String reactivar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        return ejecutar(flash, () -> comunidades.reactivar(usuario.getId(), id), "Integrante reactivado.");
    }

    private String ejecutar(RedirectAttributes flash, Runnable accion, String exito) {
        try {
            accion.run();
            flash.addFlashAttribute("mensaje", exito);
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestor/integrantes";
    }
}
