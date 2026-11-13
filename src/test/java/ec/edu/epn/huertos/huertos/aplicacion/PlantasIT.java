package ec.edu.epn.huertos.huertos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos;
import ec.edu.epn.huertos.soporte.datos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.soporte.integracion.PruebaIntegracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class PlantasIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired HuertoServicio servicio;
    @Autowired CatalogoServicio catalogo;
    @Autowired MockMvc mvc;

    private FormularioPlanta form(String nombre, int cantidad) {
        FormularioPlanta f = new FormularioPlanta();
        f.setNombreComun(nombre);
        f.setCantidad(cantidad);
        return f;
    }

    @Test
    void elGestorAdministraSusPlantasYLaFichaPublicaSoloMuestraLasActivas() throws Exception {
        Escenario e = fabrica.escenario(1);
        Long id1 =
                servicio.crearPlanta(e.gestorUsuario().getId(), form("Hierba de prueba", 4))
                        .getId();
        Long id2 =
                servicio.crearPlanta(e.gestorUsuario().getId(), form("Planta inactiva", 1)).getId();
        // Editar la planta cambia sus datos propios, no el nombre de la especie compartida del
        // catálogo.
        servicio.actualizarPlanta(e.gestorUsuario().getId(), id1, form("Hierba editada", 9));
        assertThat(servicio.plantaDeGestor(e.gestorUsuario().getId(), id1).getCantidad())
                .isEqualTo(9);
        servicio.cambiarEstadoPlanta(e.gestorUsuario().getId(), id2, false);

        assertThat(servicio.plantasDeGestor(e.gestorUsuario().getId())).hasSize(2);
        String publica =
                mvc.perform(get("/comunidades/" + e.comunidad().getId()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(publica)
                .contains("Hierba de prueba")
                .doesNotContain("Hierba editada")
                .doesNotContain("Planta inactiva");
    }

    @Test
    void ungestorNoTocaPlantasDeOtroHuertoNiUnParticipanteAdministraPlantas() throws Exception {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        Long plantaDeA =
                servicio.crearPlanta(a.gestorUsuario().getId(), form("Planta de A", 2)).getId();

        assertThatThrownBy(
                        () ->
                                servicio.actualizarPlanta(
                                        b.gestorUsuario().getId(), plantaDeA, form("Robada", 1)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(
                        () ->
                                servicio.cambiarEstadoPlanta(
                                        b.gestorUsuario().getId(), plantaDeA, false))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(
                        () ->
                                servicio.crearPlanta(
                                        a.participanteUsuarios().isEmpty()
                                                ? a.revisor().getId()
                                                : a.participanteUsuarios().get(0).getId(),
                                        form("No permitido", 1)))
                .isInstanceOf(AccessDeniedException.class);

        mvc.perform(
                        post("/gestor/plantas/" + plantaDeA)
                                .with(user(new UsuarioAutenticado(b.gestorUsuario())))
                                .with(csrf())
                                .param("nombreComun", "Robada")
                                .param("cantidad", "1"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        get("/gestor/plantas")
                                .with(
                                        user(
                                                new UsuarioAutenticado(
                                                        a.participanteUsuarios().get(0)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarUnaPlantaSoloQuitaElRegistroDeSuHuerto() throws Exception {
        Escenario a = fabrica.escenario(1);
        Escenario b = fabrica.escenario(1);
        var form = form("Planta para eliminar", 2);
        form.setPlantada(true);
        var planta = servicio.crearPlanta(a.gestorUsuario().getId(), form);
        Long especieId = planta.getEspecieId();
        var siembra = new SiembraPlanta();
        siembra.setCantidad(7);
        siembra.setPlantada(true);
        var otra = catalogo.seleccionar(b.gestorUsuario().getId(), especieId, siembra);
        String ruta = "/gestor/plantas/" + planta.getId() + "/eliminar";
        var gestorA = new UsuarioAutenticado(a.gestorUsuario());
        var gestorB = new UsuarioAutenticado(b.gestorUsuario());

        String lista =
                mvc.perform(get("/gestor/plantas").with(user(gestorA)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(lista).contains("action=\"" + ruta + "\"", "data-confirmar=");
        mvc.perform(post(ruta).with(user(gestorB)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(ruta).with(user(gestorA))).andExpect(status().isForbidden());
        assertThat(servicio.plantasPlantadasDeGestor(a.gestorUsuario().getId())).hasSize(1);
        mvc.perform(post(ruta).with(user(gestorA)).with(csrf()))
                .andExpect(redirectedUrl("/gestor/plantas"));

        assertThat(servicio.plantasDeGestor(a.gestorUsuario().getId())).isEmpty();
        assertThat(servicio.plantasPlantadasDeGestor(a.gestorUsuario().getId())).isEmpty();
        assertThat(servicio.plantaDeGestor(b.gestorUsuario().getId(), otra.getId()).getCantidad())
                .isEqualTo(7);
        var registradaDeNuevo =
                catalogo.seleccionar(a.gestorUsuario().getId(), especieId, new SiembraPlanta());
        assertThat(registradaDeNuevo.getEspecieId()).isEqualTo(especieId);
    }

    @Test
    void elGestorNoPuedeCrearUnaEspeciePorElFormularioAnterior() throws Exception {
        Escenario e = fabrica.escenario(1);
        mvc.perform(
                        post("/gestor/plantas")
                                .with(user(new UsuarioAutenticado(e.gestorUsuario())))
                                .with(csrf())
                                .param("nombreComun", "Especie no autorizada")
                                .param("cantidad", "1"))
                .andExpect(status().isForbidden());
        assertThat(servicio.plantasDeGestor(e.gestorUsuario().getId())).isEmpty();
    }
}
