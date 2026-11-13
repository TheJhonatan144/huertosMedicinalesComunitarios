package ec.edu.epn.huertos.huertos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.huertos.persistencia.EspecieRepository;
import ec.edu.epn.huertos.huertos.persistencia.PlantaRepository;
import ec.edu.epn.huertos.soporte.concurrencia.EjecucionConcurrente;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.soporte.integracion.PruebaIntegracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

/** Comprueba la integración de la novedad de Develop con DTO, permisos y transacciones locales. */
@AutoConfigureMockMvc
class RevisionSiembraServicioIT extends PruebaIntegracion {
    @Autowired FabricaDatos fabrica;
    @Autowired CatalogoServicio catalogo;
    @Autowired HuertoServicio huertos;
    @Autowired HuertosConsultaServicio consultas;
    @Autowired HuertoVirtualServicio virtual;
    @Autowired RevisionSiembraServicio revisiones;
    @Autowired PlantaRepository plantas;
    @Autowired EspecieRepository especies;
    @Autowired MockMvc mvc;

    private Planta sembrar(Escenario e) {
        FichaEspecie ficha = new FichaEspecie();
        ficha.setNombreComun("Menta de integración");
        ficha.setNombreCientifico("Mentha piperita L.");
        var especie = catalogo.guardar(e.revisor().getId(), null, ficha, null);
        SiembraPlanta form = new SiembraPlanta();
        form.setCantidad(3);
        form.setPlantada(true);
        return catalogo.seleccionar(e.gestorUsuario().getId(), especie.getId(), form);
    }

    private Planta recargar(Long id) {
        return plantas.findById(id).orElseThrow();
    }

    @Test
    void pendienteApareceEnHuertoBandejaYDtoDelGestor() throws Exception {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        assertThat(revisiones.pendientes(e.revisor().getId()))
                .extracting(RevisionSiembraServicio.Item::id)
                .containsExactly(p.getId());
        assertThat(consultas.plantaDeGestor(e.gestorUsuario().getId(), p.getId()).estadoRevision())
                .isEqualTo(EstadoRevision.PENDIENTE);
        assertThat(virtual.paraGestor(e.gestorUsuario().getId()).plantas())
                .extracting(HuertoVirtual.PlantaVirtual::estadoRevision)
                .containsExactly("PENDIENTE");
        String html =
                mvc.perform(
                                get("/gestor/plantas")
                                        .with(user(new UsuarioAutenticado(e.gestorUsuario()))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(html).contains("En revisión");
        mvc.perform(get("/revision/siembras").with(user(new UsuarioAutenticado(e.revisor()))))
                .andExpect(status().isOk())
                .andExpect(view().name("revision/siembras"));
    }

    @Test
    void aprobarPorHttpConservaFechaYRetiraDePendientes() throws Exception {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        var fecha = recargar(p.getId()).getPlantadaEn();
        mvc.perform(
                        post("/revision/siembras/{id}/decision", p.getId())
                                .with(csrf())
                                .with(user(new UsuarioAutenticado(e.revisor())))
                                .param("decision", "APROBADA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/revision/siembras"))
                .andExpect(flash().attributeExists("mensaje"));
        var aprobada = recargar(p.getId());
        assertThat(aprobada.isPlantada()).isTrue();
        assertThat(aprobada.getPlantadaEn()).isEqualTo(fecha);
        assertThat(aprobada.getEstadoRevision()).isEqualTo(EstadoRevision.APROBADA);
        assertThat(revisiones.pendientes(e.revisor().getId())).isEmpty();
        assertThat(virtual.paraGestor(e.gestorUsuario().getId()).plantas())
                .extracting(HuertoVirtual.PlantaVirtual::estadoRevision)
                .containsExactly("APROBADA");
    }

    @Test
    void rechazarPorHttpOcultaLaSiembraYPermiteReenviarla() throws Exception {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        mvc.perform(
                        post("/revision/siembras/{id}/decision", p.getId())
                                .with(csrf())
                                .with(user(new UsuarioAutenticado(e.revisor())))
                                .param("decision", "RECHAZADA")
                                .param("observaciones", "Corregir ubicación"))
                .andExpect(status().is3xxRedirection());
        assertThat(recargar(p.getId()).isPlantada()).isFalse();
        assertThat(recargar(p.getId()).getObservacionesRevision()).isEqualTo("Corregir ubicación");
        assertThat(virtual.paraGestor(e.gestorUsuario().getId()).plantas()).isEmpty();
        String html =
                mvc.perform(
                                get("/gestor/plantas")
                                        .with(user(new UsuarioAutenticado(e.gestorUsuario()))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(html).contains("Rechazada");
        huertos.cambiarPlantada(e.gestorUsuario().getId(), p.getId(), true);
        assertThat(recargar(p.getId()).getEstadoRevision()).isEqualTo(EstadoRevision.PENDIENTE);
        assertThat(recargar(p.getId()).getObservacionesRevision()).isNull();
        assertThat(revisiones.pendientes(e.revisor().getId())).hasSize(1);
    }

    @Test
    void revisorAjenoNoVeNiDecideSiembrasDeOtraComunidad() {
        var propia = fabrica.escenario(0);
        var ajena = fabrica.escenario(0);
        var p = sembrar(ajena);
        assertThat(revisiones.pendientes(propia.revisor().getId())).isEmpty();
        assertThatThrownBy(
                        () ->
                                revisiones.decidir(
                                        propia.revisor().getId(),
                                        p.getId(),
                                        EstadoRevision.APROBADA,
                                        null))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(recargar(p.getId()).getEstadoRevision()).isEqualTo(EstadoRevision.PENDIENTE);
    }

    @Test
    void decisionesInvalidasNoConsumenLaRevision() throws Exception {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        assertThatThrownBy(
                        () ->
                                revisiones.decidir(
                                        e.revisor().getId(),
                                        p.getId(),
                                        EstadoRevision.PENDIENTE,
                                        null))
                .isInstanceOf(ReglaNegocioException.class);
        mvc.perform(
                        post("/revision/siembras/{id}/decision", p.getId())
                                .with(csrf())
                                .with(user(new UsuarioAutenticado(e.revisor())))
                                .param("decision", "RECHAZADA")
                                .param("observaciones", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
        assertThat(recargar(p.getId()).getEstadoRevision()).isEqualTo(EstadoRevision.PENDIENTE);
    }

    @Test
    void dobleDecisionNoSobrescribeLaPrimera() throws Exception {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        revisiones.decidir(e.revisor().getId(), p.getId(), EstadoRevision.APROBADA, null);
        mvc.perform(
                        post("/revision/siembras/{id}/decision", p.getId())
                                .with(csrf())
                                .with(user(new UsuarioAutenticado(e.revisor())))
                                .param("decision", "RECHAZADA")
                                .param("observaciones", "Reintento"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
        assertThat(recargar(p.getId()).getEstadoRevision()).isEqualTo(EstadoRevision.APROBADA);
    }

    @Test
    void dosDecisionesConcurrentesSoloConfirmanUna() throws Exception {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        long exitos =
                EjecucionConcurrente.exitos(
                        List.of(
                                () -> intentarDecidir(e, p, EstadoRevision.APROBADA),
                                () -> intentarDecidir(e, p, EstadoRevision.RECHAZADA)));
        assertThat(exitos).isEqualTo(1);
        assertThat(recargar(p.getId()).getEstadoRevision())
                .isIn(EstadoRevision.APROBADA, EstadoRevision.RECHAZADA);
    }

    private boolean intentarDecidir(Escenario e, Planta p, EstadoRevision decision) {
        try {
            revisiones.decidir(e.revisor().getId(), p.getId(), decision, "Decisión concurrente");
            return true;
        } catch (ConflictoException esperada) {
            return false;
        }
    }

    @Test
    void bandejaUsaNombreVigenteDelCatalogoSinRelacionJpa() {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        var especie = especies.findById(p.getEspecieId()).orElseThrow();
        especie.cambiarNombre("Nombre corregido", especie.getNombreCientifico());
        especies.saveAndFlush(especie);
        assertThat(revisiones.pendientes(e.revisor().getId()))
                .extracting(RevisionSiembraServicio.Item::planta)
                .containsExactly("Nombre corregido");
    }

    @Test
    void gestorNoPuedeAccederNiDecidirComoRevisorYSeExigeCsrf() throws Exception {
        var e = fabrica.escenario(0);
        var p = sembrar(e);
        mvc.perform(get("/revision/siembras").with(user(new UsuarioAutenticado(e.gestorUsuario()))))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/revision/siembras/{id}/decision", p.getId())
                                .with(csrf())
                                .with(user(new UsuarioAutenticado(e.gestorUsuario())))
                                .param("decision", "APROBADA"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/revision/siembras/{id}/decision", p.getId())
                                .with(user(new UsuarioAutenticado(e.revisor())))
                                .param("decision", "APROBADA"))
                .andExpect(status().isForbidden());
        assertThatThrownBy(
                        () ->
                                revisiones.decidir(
                                        e.gestorUsuario().getId(),
                                        p.getId(),
                                        EstadoRevision.APROBADA,
                                        null))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(recargar(p.getId()).getEstadoRevision()).isEqualTo(EstadoRevision.PENDIENTE);
    }
}
