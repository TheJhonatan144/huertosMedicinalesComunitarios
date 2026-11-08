package ec.edu.epn.huertos.huertos;

import static org.assertj.core.api.Assertions.*;
import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.PruebaIntegracion;
import jakarta.validation.Validator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CatalogoBloqueDosIT extends PruebaIntegracion {
 @Autowired JdbcTemplate jdbc;
 @Autowired EspecieRepository especies;
 @Autowired CatalogoServicio catalogo;
 @Autowired FabricaDatos fabrica;
 @Autowired Validator validator;

 private void cargar() throws Exception {
  jdbc.execute(Files.readString(Path.of("docs/catalogo/carga-bloque-02.sql")));
 }
 private String plantas() {
  return jdbc.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(p) ORDER BY id)::text,'[]') FROM planta p",String.class);
 }
 @Test void completaEquivalenciasVaciasSinDuplicarNiCambiarSiembras() throws Exception {
  var a=fabrica.escenario(0);
  var manzanilla=especies.saveAndFlush(new Especie("Manzanilla","Matricaria chamomilla"));
  var hierbabuena=especies.saveAndFlush(new Especie("Menta","Mentha spicata"));
  jdbc.update("INSERT INTO planta(huerto_id,especie_id,nombre_comun,nombre_cientifico,cantidad,activa,plantada) VALUES(?,?,?,?,?,?,?)",
   a.huerto().getId(),manzanilla.getId(),"Manzanilla","Matricaria chamomilla",12,false,true);
  String antes=plantas();
  int total=jdbc.queryForObject("SELECT count(*) FROM especie",Integer.class);
  cargar();
  assertThat(plantas()).isEqualTo(antes);
  assertThat(jdbc.queryForObject("SELECT count(*) FROM especie",Integer.class)).isEqualTo(total+9);
  assertThat(jdbc.queryForObject("SELECT nombre_cientifico FROM especie WHERE id=?",String.class,manzanilla.getId())).isEqualTo("Matricaria chamomilla L.");
  assertThat(jdbc.queryForObject("SELECT nombre_comun FROM especie WHERE id=?",String.class,hierbabuena.getId())).isEqualTo("Hierbabuena");
 }
 @Test void onceFichasValidasConservanEspeciesSeparadasYDatosDesconocidos() throws Exception {
  cargar();
  var nombres=Set.of("Ilex guayusa Loes.","Mentha spicata L.","Zingiber officinale Roscoe",
   "Plantago major L.","Plantago lanceolata L.","Matricaria chamomilla L.","Ambrosia arborescens Mill.",
   "Piper aduncum L.","Mentha × piperita L.","Origanum vulgare L.","Salvia rosmarinus Spenn.");
  var fichas=especies.findAll().stream().filter(e -> nombres.contains(e.getNombreCientifico())).toList();
  assertThat(fichas).hasSize(11);
  assertThat(fichas).filteredOn(e -> e.getNombreCientifico().startsWith("Plantago ")).hasSize(2);
  assertThat(fichas).filteredOn(e -> e.getNombreCientifico().equals("Salvia rosmarinus Spenn.")).hasSize(1);
  assertThat(fichas).filteredOn(e -> e.getNombreCientifico().equals("Mentha × piperita L.")).hasSize(1);
  for (var e:fichas) {
   if (e.getFuenteDocumental()==null) continue;
   assertThat(validator.validate(FichaEspecie.de(e))).as(e.getNombreComun()).isEmpty();
   assertThat(e.getAltitudMinima()).isNull();
   assertThat(e.getAltitudMaxima()).isNull();
   assertThat(e.getObservacionesAltitud()).isNotBlank();
  }
 }
 @Test void repetirCargaConservaUnaFichaDocumentadaEditada() throws Exception {
  cargar();
  jdbc.update("UPDATE especie SET nombre_comun='Guayusa revisada',version=version+1 WHERE nombre_cientifico='Ilex guayusa Loes.'");
  var antes=jdbc.queryForList("SELECT * FROM especie ORDER BY id");
  cargar();
  assertThat(jdbc.queryForList("SELECT * FROM especie ORDER BY id")).isEqualTo(antes);
 }
 @Test void equivalenciaAmbiguaDetieneLaCarga() {
  especies.saveAndFlush(new Especie("Hierbabuena uno","Mentha spicata"));
  especies.saveAndFlush(new Especie("Hierbabuena dos","Mentha spicata L."));
  assertThatThrownBy(this::cargar).isInstanceOf(DataAccessException.class).hasMessageContaining("equivalencia ambigua");
 }
 @Test void fichaParcialNoSeSobrescribe() {
  var e=especies.saveAndFlush(new Especie("Menta","Mentha spicata"));
  jdbc.update("UPDATE especie SET familia_botanica='Dato previo' WHERE id=?",e.getId());
  assertThatThrownBy(this::cargar).isInstanceOf(DataAccessException.class).hasMessageContaining("datos previos sin fuente");
 }
 @Test void busquedaEncuentraAcentosYSinonimosSinCambiarElTextoGuardado() throws Exception {
  var a=fabrica.escenario(0);
  cargar();
  assertThat(catalogo.listar(a.gestorUsuario().getId(),"  OREGANO  ",null,null,null))
   .extracting(Especie::getNombreComun).containsExactly("Orégano");
  assertThat(catalogo.listar(a.gestorUsuario().getId(),"llanten",null,null,null)).hasSize(2);
  assertThat(catalogo.listar(a.gestorUsuario().getId(),"rosmarinus officinalis",null,null,null))
   .extracting(Especie::getNombreCientifico).containsExactly("Salvia rosmarinus Spenn.");
  assertThat(catalogo.listar(a.gestorUsuario().getId(),"",null,"MORONA SANTIAGO",null))
   .extracting(Especie::getNombreComun).contains("Guayusa","Jengibre");
 }
}
