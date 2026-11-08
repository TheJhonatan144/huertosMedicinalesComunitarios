package ec.edu.epn.huertos.huertos;
import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.*;
import org.springframework.core.io.Resource;

@Controller
public class CatalogoController {
 private final CatalogoServicio catalogo;
 private final HuertoServicio huertos;
 public CatalogoController(CatalogoServicio catalogo, HuertoServicio huertos) { this.catalogo = catalogo; this.huertos = huertos; }
 @ModelAttribute("regiones") RegionPlanta[] regiones() { return RegionPlanta.values(); }
 @ModelAttribute("editorCatalogo")
 boolean editorCatalogo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
  return usuario != null && usuario.getRol() == Rol.ADMIN_MINISTERIO_SALUD;
 }
 @GetMapping("/gestor/plantas/catalogo")
 String catalogo(@AuthenticationPrincipal UsuarioAutenticado usuario, @RequestParam(defaultValue = "") String q,
       @RequestParam(required = false) RegionPlanta region, @RequestParam(defaultValue = "") String provincia,
       @RequestParam(required = false) Integer altitud,
       @RequestParam(defaultValue = "cuadricula") String vista, Model model) {
  model.addAttribute("especies", catalogo.listar(usuario.getId(), q, region, provincia, altitud));
  model.addAttribute("q", q); model.addAttribute("region", region); model.addAttribute("provincia", provincia); model.addAttribute("altitud", altitud);
  model.addAttribute("vista", "lista".equals(vista) ? "lista" : "cuadricula");
  model.addAttribute("seleccionadas", editorCatalogo(usuario) ? java.util.List.of()
      : huertos.plantasDeGestor(usuario.getId()).stream().map(p -> p.getEspecie().getId()).toList());
  return "gestor/catalogo";
 }
 @GetMapping("/gestor/plantas/catalogo/nueva")
 String nueva(Model model) { model.addAttribute("form", new FichaEspecie()); model.addAttribute("accion", "/gestor/plantas/catalogo"); return "gestor/especie-form"; }
 @GetMapping("/gestor/plantas/catalogo/{id}")
 String ficha(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
  model.addAttribute("especie", catalogo.obtener(usuario.getId(), id)); model.addAttribute("form", new SiembraPlanta());
  if (!editorCatalogo(usuario)) {
   model.addAttribute("registrada", huertos.plantasDeGestor(usuario.getId()).stream()
       .filter(p -> p.getEspecie().getId().equals(id)).findFirst().orElse(null));
  }
  return "gestor/especie-detalle";
 }
 @GetMapping("/gestor/plantas/catalogo/{id}/editar")
 String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
  model.addAttribute("form", FichaEspecie.de(catalogo.obtener(usuario.getId(), id)));
  model.addAttribute("accion", "/gestor/plantas/catalogo/" + id); return "gestor/especie-form";
 }
 @PostMapping({"/gestor/plantas/catalogo", "/gestor/plantas/catalogo/{id}"})
 String guardar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") FichaEspecie form, BindingResult errores,
       @RequestParam(required = false) MultipartFile foto, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
  if (!errores.hasErrors()) {
   try {
    Especie especie = catalogo.guardar(usuario.getId(), id, form, foto);
    flash.addFlashAttribute("mensaje", "Ficha del catálogo común guardada.");
    return "redirect:/gestor/plantas/catalogo/" + especie.getId();
   } catch (ReglaNegocioException e) { errores.reject("ficha", e.getMessage()); }
  }
  model.addAttribute("accion", id == null ? "/gestor/plantas/catalogo" : "/gestor/plantas/catalogo/" + id);
  return "gestor/especie-form";
 }
 @PostMapping("/gestor/plantas/catalogo/{id}/seleccionar")
 String seleccionar(@PathVariable Long id, @Valid @ModelAttribute("form") SiembraPlanta form, BindingResult errores,
       @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
  if (errores.hasErrors()) {
   model.addAttribute("especie", catalogo.obtener(usuario.getId(), id)); model.addAttribute("registrada", null);
   return "gestor/especie-detalle";
  }
  catalogo.seleccionar(usuario.getId(), id, form); flash.addFlashAttribute("mensaje", "Especie agregada a tu comunidad.");
  return "redirect:/gestor/plantas";
 }
 @GetMapping("/gestor/plantas/catalogo/{id}/foto")
 ResponseEntity<Resource> foto(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
  Especie e = catalogo.obtener(usuario.getId(), id);
  Resource recurso = catalogo.foto(usuario.getId(), id);
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(e.getFotoTipo())).cacheControl(CacheControl.noStore())
    .header("X-Content-Type-Options", "nosniff").body(recurso);
 }
 @GetMapping("/gestor/plantas/{id}/siembra")
 String siembra(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
  Planta p = huertos.plantaDeGestor(usuario.getId(), id); var f = new SiembraPlanta(); f.setCantidad(p.getCantidad()); f.setPlantada(p.isPlantada());
  model.addAttribute("planta", p); model.addAttribute("form", f); return "gestor/siembra-form";
 }
 @PostMapping("/gestor/plantas/{id}/siembra")
 String siembraGuardar(@PathVariable Long id, @Valid @ModelAttribute("form") SiembraPlanta form, BindingResult errores,
       @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
  Planta p = huertos.plantaDeGestor(usuario.getId(), id);
  if (errores.hasErrors()) { model.addAttribute("planta", p); return "gestor/siembra-form"; }
  huertos.actualizarSiembra(usuario.getId(), id, form); flash.addFlashAttribute("mensaje", "Siembra y cantidad de tu comunidad actualizadas.");
  return "redirect:/gestor/plantas";
 }
 public record PlantaVirtual(Long id, Long especieId, String nombreComun, String nombreCientifico, int cantidad, boolean plantada, boolean activa, String fotoUrl) {}
 @GetMapping("/gestor/plantas/plantadas") @ResponseBody
 java.util.List<PlantaVirtual> plantadas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
  return huertos.plantasPlantadasDeGestor(usuario.getId()).stream().map(p -> new PlantaVirtual(p.getId(), p.getEspecie().getId(),
   p.getNombreComun(), p.getNombreCientifico(), p.getCantidad(), p.isPlantada(), p.isActiva(),
   p.getEspecie().getFotoClave() == null ? "/img/planta-referencia.svg" : "/gestor/plantas/catalogo/"+p.getEspecie().getId()+"/foto")).toList();
 }
}
