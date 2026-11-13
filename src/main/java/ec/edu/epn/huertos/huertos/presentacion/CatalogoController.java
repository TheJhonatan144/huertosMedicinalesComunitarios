package ec.edu.epn.huertos.huertos.presentacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.huertos.aplicacion.CatalogoServicio;
import ec.edu.epn.huertos.huertos.aplicacion.FichaEspecie;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoServicio;
import ec.edu.epn.huertos.huertos.aplicacion.SiembraPlanta;
import ec.edu.epn.huertos.huertos.dominio.RegionPlanta;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CatalogoController {
 private final CatalogoServicio catalogo;
 private final HuertoServicio huertos;
    private final ec.edu.epn.huertos.huertos.aplicacion.HuertosConsultaServicio consultas;
 public CatalogoController(CatalogoServicio catalogo, HuertoServicio huertos, ec.edu.epn.huertos.huertos.aplicacion.HuertosConsultaServicio consultas) { this.catalogo = catalogo; this.huertos = huertos; this.consultas = consultas; }
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
  model.addAttribute("especies", catalogo.listarVista(usuario.getId(), q, region, provincia, altitud));
  model.addAttribute("q", q); model.addAttribute("region", region); model.addAttribute("provincia", provincia); model.addAttribute("altitud", altitud);
  model.addAttribute("vista", "lista".equals(vista) ? "lista" : "cuadricula");
  model.addAttribute("seleccionadas", editorCatalogo(usuario) ? java.util.List.of()
      : consultas.plantasDeGestor(usuario.getId()).stream().map(p -> p.getEspecie().getId()).toList());
  return "gestor/catalogo";
 }
 @GetMapping("/gestor/plantas/catalogo/nueva")
 String nueva(Model model) { model.addAttribute("form", new FichaEspecie()); model.addAttribute("accion", "/gestor/plantas/catalogo"); return "gestor/especie-form"; }
 @GetMapping("/gestor/plantas/catalogo/{id}")
 String ficha(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
  model.addAttribute("especie", catalogo.obtenerVista(usuario.getId(), id)); model.addAttribute("form", new SiembraPlanta());
  if (!editorCatalogo(usuario)) {
   model.addAttribute("registrada", consultas.plantasDeGestor(usuario.getId()).stream()
       .filter(p -> p.getEspecie().getId().equals(id)).findFirst().orElse(null));
  }
  return "gestor/especie-detalle";
 }
 @GetMapping("/gestor/plantas/catalogo/{id}/editar")
 String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
  model.addAttribute("form", FichaEspecie.de(catalogo.obtenerVista(usuario.getId(), id)));
  model.addAttribute("accion", "/gestor/plantas/catalogo/" + id); return "gestor/especie-form";
 }
 @PostMapping({"/gestor/plantas/catalogo", "/gestor/plantas/catalogo/{id}"})
 String guardar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") FichaEspecie form, BindingResult errores,
       @RequestParam(required = false) MultipartFile foto, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
  if (!errores.hasErrors()) {
   try {
    Long guardadaId = catalogo.guardarFicha(usuario.getId(), id, form, foto);
    flash.addFlashAttribute("mensaje", "Ficha del catálogo común guardada.");
    return "redirect:/gestor/plantas/catalogo/" + guardadaId;
   } catch (ReglaNegocioException e) { errores.reject("ficha", e.getMessage()); }
  }
  model.addAttribute("accion", id == null ? "/gestor/plantas/catalogo" : "/gestor/plantas/catalogo/" + id);
  return "gestor/especie-form";
 }
 @PostMapping("/gestor/plantas/catalogo/{id}/seleccionar")
 String seleccionar(@PathVariable Long id, @Valid @ModelAttribute("form") SiembraPlanta form, BindingResult errores,
       @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
  if (errores.hasErrors()) {
   model.addAttribute("especie", catalogo.obtenerVista(usuario.getId(), id)); model.addAttribute("registrada", null);
   return "gestor/especie-detalle";
  }
  catalogo.seleccionarParaHuerto(usuario.getId(), id, form); flash.addFlashAttribute("mensaje", "Especie agregada a tu comunidad.");
  return "redirect:/gestor/plantas";
 }
 @GetMapping("/gestor/plantas/catalogo/{id}/foto")
 ResponseEntity<Resource> foto(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
  var e = catalogo.obtenerVista(usuario.getId(), id);
  Resource recurso = catalogo.foto(usuario.getId(), id);
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(e.getFotoTipo())).cacheControl(CacheControl.noStore())
    .header("X-Content-Type-Options", "nosniff").body(recurso);
 }
 @GetMapping("/gestor/plantas/{id}/siembra")
 String siembra(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
  var p = consultas.plantaDeGestor(usuario.getId(), id); var f = new SiembraPlanta(); f.setCantidad(p.getCantidad()); f.setPlantada(p.isPlantada());
  model.addAttribute("planta", p); model.addAttribute("form", f); return "gestor/siembra-form";
 }
 @PostMapping("/gestor/plantas/{id}/siembra")
 String siembraGuardar(@PathVariable Long id, @Valid @ModelAttribute("form") SiembraPlanta form, BindingResult errores,
       @AuthenticationPrincipal UsuarioAutenticado usuario, Model model, RedirectAttributes flash) {
  var p = consultas.plantaDeGestor(usuario.getId(), id);
  if (errores.hasErrors()) { model.addAttribute("planta", p); return "gestor/siembra-form"; }
  huertos.actualizarSiembra(usuario.getId(), id, form); flash.addFlashAttribute("mensaje", "Siembra y cantidad de tu comunidad actualizadas.");
  return "redirect:/gestor/plantas";
 }
 public record PlantaVirtual(Long id, Long especieId, String nombreComun, String nombreCientifico, int cantidad, boolean plantada, boolean activa, String fotoUrl) {}
 @GetMapping("/gestor/plantas/plantadas") @ResponseBody
 java.util.List<PlantaVirtual> plantadas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
  return consultas.plantasPlantadasDeGestor(usuario.getId()).stream().map(p -> new PlantaVirtual(p.getId(), p.getEspecie().getId(),
   p.getNombreComun(), p.getNombreCientifico(), p.getCantidad(), p.isPlantada(), p.isActiva(),
   p.getEspecie().getFotoClave() == null ? "/img/planta-referencia.svg" : "/gestor/plantas/catalogo/"+p.getEspecie().getId()+"/foto")).toList();
 }
}
