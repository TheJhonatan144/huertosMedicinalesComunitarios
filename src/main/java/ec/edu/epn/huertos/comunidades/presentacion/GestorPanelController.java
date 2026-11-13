package ec.edu.epn.huertos.comunidades.presentacion;
import ec.edu.epn.huertos.actividades.aplicacion.ActividadResumen;
import ec.edu.epn.huertos.actividades.aplicacion.ActividadServicio;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import ec.edu.epn.huertos.comunidades.aplicacion.FormularioInvitacion;
import ec.edu.epn.huertos.comunidades.aplicacion.IntegranteVista;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;
import ec.edu.epn.huertos.gamificacion.aplicacion.GamificacionServicio;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoVirtualServicio;
import ec.edu.epn.huertos.huertos.aplicacion.HuertosConsultaServicio;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Panel del gestor de huerto y administración de integrantes de su comunidad. */
@Controller
public class GestorPanelController {

    private final ComunidadServicio comunidades;
    private final HuertosConsultaServicio huertos;
    private final ActividadServicio actividades;
    private final GamificacionServicio gamificacion;
    private final ec.edu.epn.huertos.autenticacion.aplicacion.SesionServicio sesiones;
    private final HuertoVirtualServicio huertoVirtual;

    public GestorPanelController(ComunidadServicio comunidades, HuertosConsultaServicio huertos, ActividadServicio actividades,
                                 GamificacionServicio gamificacion, ec.edu.epn.huertos.autenticacion.aplicacion.SesionServicio sesiones,
                                 HuertoVirtualServicio huertoVirtual) {
        this.comunidades = comunidades;
        this.huertos = huertos;
        this.actividades = actividades;
        this.gamificacion = gamificacion;
        this.sesiones = sesiones;
        this.huertoVirtual = huertoVirtual;
    }

    @GetMapping("/gestor")
    String panel(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        var gestor = comunidades.gestorVistaDe(usuario.getId());
        var comunidad = comunidades.comunidadVista(gestor.getComunidadId());
        var huerto = huertos.huertoDeGestor(usuario.getId());
        List<ActividadResumen> lista = actividades.listar(usuario.getId());
        model.addAttribute("comunidad", comunidad);
        model.addAttribute("eliminacionPendiente", comunidad.isEliminacionPendiente());
        model.addAttribute("huerto", huerto);
        model.addAttribute("espaciosHuerto", huertos.espaciosDeGestor(usuario.getId()));
        model.addAttribute("plantasPlantadas", huertos.plantasPlantadasDeGestor(usuario.getId()));
        model.addAttribute("activos", comunidades.activos(comunidad.getId()));
        model.addAttribute("cupo", comunidades.cupoMaximo(comunidad.id()));
        model.addAttribute("cuposDisponibles", comunidades.cuposDisponibles(comunidad.getId()));
        model.addAttribute("puntosComunitarios", gamificacion.puntosComunitarios(comunidad.getId()));
        model.addAttribute("progreso", gamificacion.progreso(comunidad.getId()));
        model.addAttribute("huertoVirtual", huertoVirtual.paraGestor(usuario.getId()));
        model.addAttribute("misPuntos", gamificacion.resumenIndividual(gestor.getId()));
        model.addAttribute("pendientes", lista.stream().filter(a -> a.estado() == EstadoRevision.PENDIENTE).count());
        model.addAttribute("rechazadas", lista.stream().filter(a -> a.estado() == EstadoRevision.RECHAZADA).count());
        model.addAttribute("aprobadas", lista.stream().filter(a -> a.estado() == EstadoRevision.APROBADA).count());
        return "gestor/panel";
    }

    @GetMapping("/gestor/integrantes")
    String integrantes(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        var gestor = comunidades.gestorVistaDe(usuario.getId());
        model.addAttribute("comunidad", comunidades.comunidadVista(gestor.getComunidadId()));
        model.addAttribute("eliminacionPendiente", comunidades.comunidadVista(gestor.getComunidadId()).isEliminacionPendiente());
        var integrantes = comunidades.integrantesDeMiComunidad(usuario.getId());
        model.addAttribute("integrantesActivos", integrantes.stream().filter(IntegranteVista::activo).toList());
        model.addAttribute("integrantesHistorial", integrantes.stream().filter(i -> !i.activo()).toList());
        model.addAttribute("activos", comunidades.activos(gestor.getComunidadId()));
        model.addAttribute("cupo", comunidades.cupoMaximo(gestor.getComunidadId()));
        model.addAttribute("cuposDisponibles", comunidades.cuposDisponibles(gestor.getComunidadId()));
        if (!model.containsAttribute("formulario")) model.addAttribute("formulario", new FormularioInvitacion());
        model.addAttribute("invitaciones", comunidades.invitacionesEnviadas(usuario.getId()));
        return "gestor/integrantes";
    }

    @PostMapping("/gestor/integrantes/invitar")
    String invitarIntegrante(@AuthenticationPrincipal UsuarioAutenticado usuario,
                             @ModelAttribute("formulario") FormularioInvitacion formulario,
                             RedirectAttributes flash) {
        try {
            comunidades.invitarIntegrante(usuario.getId(), formulario);
            flash.addFlashAttribute("mensaje", "Invitación enviada. La persona debe aceptarla desde su cuenta.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestor/integrantes";
    }

    @PostMapping("/gestor/invitaciones/{id}/cancelar")
    String cancelarInvitacion(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                              RedirectAttributes flash) {
        return ejecutar(flash, () -> comunidades.cancelarInvitacion(usuario.getId(), id), "Invitación cancelada.");
    }

    @PostMapping("/gestor/integrantes/{id}/transferir")
    String proponerGestor(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                          RedirectAttributes flash) {
        return ejecutar(flash, () -> comunidades.invitarTraspasoGestor(usuario.getId(), id),
                "Solicitud de traspaso enviada. La persona debe aceptarla.");
    }

    @PostMapping("/gestor/comunidad/archivar")
    String archivar(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest request,
                    RedirectAttributes flash) {
        comunidades.archivarComunidad(usuario.getId());
        UsuarioAutenticado principalActualizado = sesiones.principalActual(usuario.getId());
        Authentication autenticacionActualizada = new UsernamePasswordAuthenticationToken(
                principalActualizado, null, principalActualizado.getAuthorities());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacionActualizada);
        SecurityContextHolder.setContext(contexto);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, contexto);
        flash.addFlashAttribute("mensaje", "La comunidad y su huerto se archivaron. Puedes consultar su ficha histórica.");
        return "redirect:/cuenta/archivadas";
    }

    @PostMapping("/gestor/integrantes/{id}/baja")
    String baja(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        return ejecutar(flash, () -> comunidades.darDeBaja(usuario.getId(), id), "Integrante dado de baja. Se liberó un cupo.");
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
