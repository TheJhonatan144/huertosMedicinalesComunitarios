package ec.edu.epn.huertos.bodega.aplicacion;
import ec.edu.epn.huertos.bodega.dominio.ArticuloBodega;
import ec.edu.epn.huertos.bodega.persistencia.ArticuloBodegaRepository;
import ec.edu.epn.huertos.compartido.dominio.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.huertos.aplicacion.HuertoServicio;
import jakarta.validation.Validator;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class BodegaServicio {
 private final HuertoServicio huertos; private final ArticuloBodegaRepository articulos; private final Validator validador;
 public BodegaServicio(HuertoServicio huertos, ArticuloBodegaRepository articulos, Validator validador) { this.huertos = huertos; this.articulos = articulos; this.validador = validador; }
 @Transactional(readOnly = true) public List<ArticuloBodega> listar(Long usuarioId) { return articulos.findByHuertoIdOrderByCategoriaAscNombreAsc(huertos.huertoDeGestor(usuarioId).getId()); }
 @Transactional(readOnly = true) public ArticuloBodega obtener(Long usuarioId, Long id) {
  Long huertoId = huertos.huertoDeGestor(usuarioId).getId();
  var a = articulos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("El artículo no existe."));
  if (!a.getHuertoId().equals(huertoId)) throw new AccessDeniedException("El artículo pertenece a otra comunidad."); return a;
 }
 private void validar(FormularioArticulo f) { if (!validador.validate(f).isEmpty()) throw new ReglaNegocioException("Revisa nombre, categoría y cantidad."); }
 @Transactional public ArticuloBodega crear(Long usuarioId, FormularioArticulo f) { Long h = huertos.huertoOperativoDeGestor(usuarioId); validar(f); return articulos.save(new ArticuloBodega(h, f.getNombre(), f.getCategoria(), f.getCantidad())); }
 @Transactional public void editar(Long usuarioId, Long id, FormularioArticulo f) { huertos.huertoOperativoDeGestor(usuarioId); var a = obtener(usuarioId,id); validar(f); a.actualizar(f.getNombre(), f.getCategoria(), f.getCantidad()); }
 @Transactional public void eliminar(Long usuarioId, Long id) { huertos.huertoOperativoDeGestor(usuarioId); articulos.delete(obtener(usuarioId,id)); }

 @Transactional public void crearArticulo(Long u,FormularioArticulo f){crear(u,f);}
 @Transactional(readOnly=true) public List<ArticuloVista> listarVista(Long u){return listar(u).stream().map(ArticuloVista::de).toList();}
 @Transactional(readOnly=true) public ArticuloVista obtenerVista(Long u,Long id){return ArticuloVista.de(obtener(u,id));}
}
