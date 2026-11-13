package ec.edu.epn.huertos.bodega.presentacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.bodega.aplicacion.BodegaServicio;
import ec.edu.epn.huertos.bodega.aplicacion.FormularioArticulo;
import ec.edu.epn.huertos.bodega.dominio.CategoriaArticulo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BodegaController {
 private final BodegaServicio bodega;
 public BodegaController(BodegaServicio bodega) { this.bodega = bodega; }
 @ModelAttribute("categorias") CategoriaArticulo[] categorias() { return CategoriaArticulo.values(); }
 @GetMapping("/gestor/bodega") String lista(@AuthenticationPrincipal UsuarioAutenticado u, Model m) { m.addAttribute("articulos", bodega.listarVista(u.getId())); return "gestor/bodega"; }
 @GetMapping("/gestor/bodega/nuevo") String nuevo(Model m) { m.addAttribute("form", new FormularioArticulo()); m.addAttribute("accion", "/gestor/bodega"); return "gestor/bodega-form"; }
 @GetMapping("/gestor/bodega/{id}/editar") String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u, Model m) {
  m.addAttribute("form", FormularioArticulo.de(bodega.obtenerVista(u.getId(),id))); m.addAttribute("accion", "/gestor/bodega/"+id); return "gestor/bodega-form";
 }
 @PostMapping({"/gestor/bodega", "/gestor/bodega/{id}"}) String guardar(@PathVariable(required = false) Long id,
       @Valid @ModelAttribute("form") FormularioArticulo f, BindingResult errores, @AuthenticationPrincipal UsuarioAutenticado u, Model m, RedirectAttributes flash) {
  if (id != null) bodega.obtenerVista(u.getId(),id);
  if (errores.hasErrors()) { m.addAttribute("accion", id == null ? "/gestor/bodega" : "/gestor/bodega/"+id); return "gestor/bodega-form"; }
  if (id == null) bodega.crearArticulo(u.getId(),f); else bodega.editar(u.getId(),id,f);
  flash.addFlashAttribute("mensaje", "Artículo guardado."); return "redirect:/gestor/bodega";
 }
 @PostMapping("/gestor/bodega/{id}/eliminar") String eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u, RedirectAttributes flash) {
  bodega.eliminar(u.getId(),id); flash.addFlashAttribute("mensaje", "Artículo eliminado."); return "redirect:/gestor/bodega";
 }
}
