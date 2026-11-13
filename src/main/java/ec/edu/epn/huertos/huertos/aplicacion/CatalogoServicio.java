package ec.edu.epn.huertos.huertos.aplicacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.AutorizacionServicio;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.compartido.dominio.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.evidencias.aplicacion.ValidadorImagen;
import ec.edu.epn.huertos.evidencias.dominio.AlmacenamientoArchivos;
import ec.edu.epn.huertos.evidencias.dominio.ImagenValidada;
import ec.edu.epn.huertos.huertos.dominio.Especie;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.huertos.dominio.RegionPlanta;
import ec.edu.epn.huertos.huertos.persistencia.EspecieRepository;
import ec.edu.epn.huertos.huertos.persistencia.FotoCatalogoInicialRepository;
import jakarta.validation.Validator;
import java.text.Normalizer;
import java.util.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

/** Datos compartidos de especies. La siembra pertenece exclusivamente a Planta/huerto. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class CatalogoServicio {
 private static final String FOTO_INICIAL = "catalogo-inicial/";
 private final EspecieRepository especies;
 private final AutorizacionServicio permisos;
 private final HuertoServicio huertos;
 private final ValidadorImagen imagenes;
 private final AlmacenamientoArchivos archivos;
 private final Validator validador;
 private final FotoCatalogoInicialRepository fotosIniciales;
 public CatalogoServicio(EspecieRepository especies, AutorizacionServicio permisos, HuertoServicio huertos,
        ValidadorImagen imagenes, AlmacenamientoArchivos archivos, Validator validador, FotoCatalogoInicialRepository fotosIniciales) {
  this.especies = especies; this.permisos = permisos; this.huertos = huertos;
  this.imagenes = imagenes; this.archivos = archivos; this.validador = validador;
  this.fotosIniciales = fotosIniciales;
 }
 private void autorizarConsulta(Long usuarioId) {
  permisos.exigirAlgunRol(usuarioId, Rol.GESTOR_HUERTO, Rol.ADMIN_MINISTERIO_SALUD);
 }
 @Transactional(readOnly = true)
 public List<Especie> listar(Long usuarioId, String texto, RegionPlanta region, String provincia, Integer altitud) {
  autorizarConsulta(usuarioId);
  String q = normalizarBusqueda(texto);
  String prov = normalizarBusqueda(provincia);
  return especies.findAll().stream()
   .filter(e -> normalizarBusqueda(e.getNombreComun() + " " + Objects.toString(e.getNombreCientifico(), "") + " " + Objects.toString(e.getOtrosNombres(), "")).contains(q))
   .filter(e -> region == null || e.getRegiones().contains(region))
   .filter(e -> prov.isEmpty() || normalizarBusqueda(e.getProvincias()).contains(prov))
   .filter(e -> altitud == null || (e.getAltitudMinima() != null && e.getAltitudMaxima() != null && altitud >= e.getAltitudMinima() && altitud <= e.getAltitudMaxima()))
   .sorted(Comparator.comparing(Especie::getNombreComun, String.CASE_INSENSITIVE_ORDER)).toList();
 }
 private static String normalizarBusqueda(String valor) {
  if (valor == null) return "";
  return Normalizer.normalize(valor.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
   .replaceAll("\\p{M}+", "").replaceAll("\\s+", " ");
 }
 @Transactional(readOnly = true)
 public Especie obtener(Long usuarioId, Long id) {
  autorizarConsulta(usuarioId);
  return especies.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("La especie no existe en el catálogo."));
 }
 @Transactional
 public Especie guardar(Long usuarioId, Long id, FichaEspecie ficha, MultipartFile foto) {
  permisos.exigirRol(usuarioId, Rol.ADMIN_MINISTERIO_SALUD);
  if (!validador.validate(ficha).isEmpty()) throw new ReglaNegocioException("Revisa los datos de la ficha y sus fuentes.");
  Especie especie = id == null ? new Especie(ficha.getNombreComun(), ficha.getNombreCientifico()) : obtener(usuarioId, id);
  if (id != null && especie.getVersion() != ficha.getVersion()) throw new ConflictoException("La ficha cambió. Vuelve a abrirla antes de editar.");
  especie.actualizar(ficha.datosDominio());
  if (foto != null && !foto.isEmpty()) {
   ImagenValidada imagen = imagenes.validar(foto);
   String anterior = especie.getFotoClave(); String nueva = archivos.guardar(imagen);
   TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
    @Override public void afterCompletion(int estado) {
     if (estado != STATUS_COMMITTED) archivos.eliminar(nueva);
     else if (anterior != null && !anterior.startsWith(FOTO_INICIAL)) archivos.eliminar(anterior);
    }
   });
   especie.cambiarFoto(nueva, imagen.tipoContenido());
  }
  return especies.save(especie);
 }
 @Transactional
 public Planta seleccionar(Long usuarioId, Long especieId, SiembraPlanta form) {
  permisos.exigirRol(usuarioId, Rol.GESTOR_HUERTO);
  if (!validador.validate(form).isEmpty()) throw new ReglaNegocioException("Revisa la cantidad de plantas.");
  return huertos.seleccionarEspecie(usuarioId, obtener(usuarioId, especieId), form.getCantidad(), form.isPlantada());
 }
 @Transactional(readOnly = true)
 public Resource foto(Long usuarioId, Long id) {
  Especie e = obtener(usuarioId, id);
  if (e.getFotoClave() == null) throw new RecursoNoEncontradoException("Fotografía pendiente de registrar.");
  String clave = e.getFotoClave();
  if (clave.startsWith(FOTO_INICIAL)) {
   return fotosIniciales.contenido(clave).map(ByteArrayResource::new)
    .orElseThrow(() -> new RecursoNoEncontradoException("La fotografía no está disponible."));
  }
  Resource recurso = archivos.cargar(clave);
  if (!recurso.exists()) throw new RecursoNoEncontradoException("La fotografía no está disponible.");
  return recurso;
 }

 @Transactional(readOnly=true) public List<EspecieVista> listarVista(Long u,String q,RegionPlanta r,String p,Integer a){return listar(u,q,r,p,a).stream().map(EspecieVista::de).toList();}
 @Transactional(readOnly=true) public EspecieVista obtenerVista(Long u,Long id){return EspecieVista.de(obtener(u,id));}
 @Transactional public Long guardarFicha(Long u,Long id,FichaEspecie f,MultipartFile foto){return guardar(u,id,f,foto).getId();}
 @Transactional public void seleccionarParaHuerto(Long u,Long id,SiembraPlanta f){seleccionar(u,id,f);}
}
