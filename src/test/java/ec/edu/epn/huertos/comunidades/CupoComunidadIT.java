package ec.edu.epn.huertos.comunidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.PruebaIntegracion;
import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.compartido.ReglaNegocioException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

/** Límite de integrantes activos según tipo de comunidad (incluido el gestor), con PostgreSQL real. */
class CupoComunidadIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired ComunidadServicio comunidades;
    @Autowired ComunidadRepository comunidadRepository;
    @Autowired IntegranteRepository integrantes;
    @Autowired UsuarioRepository usuarios;

    /** Gestor + 24 participantes activos = 25 activos. */
    private Escenario comunidadLlena() {
        Escenario e = fabrica.escenario(24);
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
        return e;
    }

    @Test
    void labaseDeDatosRechazaElIntegranteNumero26() {
        Escenario e = comunidadLlena();
        assertThat(comunidades.cupoMaximo(e.comunidad())).isEqualTo(25);
        assertThatThrownBy(() -> fabrica.participanteNuevo(e.comunidad(), true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("cupo_comunidad_excedido");
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
    }

    @Test
    void condominioLimitaA20IntegrantesIncluidoElGestor() {
        Escenario e = fabrica.escenario(18);
        Comunidad comunidad = e.comunidad();
        comunidad.actualizarDatos(comunidad.getNombre(), comunidad.getDescripcion(), "CONDOMINIO",
                comunidad.getProvincia(), comunidad.getCanton(), comunidad.getDireccionExacta(),
                comunidad.getLatitud(), comunidad.getLongitud());
        comunidadRepository.saveAndFlush(comunidad);

        assertThat(comunidades.cupoMaximo(comunidad)).isEqualTo(20);
        assertThat(integrantes.countByComunidadIdAndActivoTrue(comunidad.getId())).isEqualTo(19);

        Usuario invitado = fabrica.usuario("condominioInvitado" + comunidad.getId(), Rol.SIN_ASIGNAR);
        FormularioInvitacion formulario = new FormularioInvitacion();
        formulario.setNombreUsuario(invitado.getNombreUsuario());
        comunidades.invitarIntegrante(e.gestorUsuario().getId(), formulario);
        Long invitacionId = comunidades.invitacionesRecibidas(invitado.getId()).get(0).id();
        comunidades.aceptarInvitacion(invitado.getId(), invitacionId);
        assertThat(integrantes.countByComunidadIdAndActivoTrue(comunidad.getId())).isEqualTo(20);

        Usuario excedente = fabrica.usuario("condominioExcedente" + comunidad.getId(), Rol.SIN_ASIGNAR);
        formulario.setNombreUsuario(excedente.getNombreUsuario());
        assertThatThrownBy(() -> comunidades.invitarIntegrante(e.gestorUsuario().getId(), formulario))
                .isInstanceOf(ConflictoException.class);
        assertThatThrownBy(() -> fabrica.participanteNuevo(comunidad, true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("cupo_comunidad_excedido");
        assertThat(integrantes.countByComunidadIdAndActivoTrue(comunidad.getId())).isEqualTo(20);
    }

    @Test
    void unIntegranteDeBajaNoConsumeCupo() {
        Escenario e = comunidadLlena();
        // Dar de alta un inactivo es posible aunque la comunidad esté llena.
        Integrante inactivo = fabrica.participanteNuevo(e.comunidad(), false);
        assertThat(inactivo.isActivo()).isFalse();
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
    }

    @Test
    void unIntegranteDeBajaDebeAceptarUnaNuevaInvitacionParaVolver() {
        Escenario e = comunidadLlena();
        Integrante integrante = e.participantes().get(0);
        Long usuarioId = e.participanteUsuarios().get(0).getId();
        comunidades.darDeBaja(e.gestorUsuario().getId(), integrante.getId());
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(24);

        FormularioInvitacion formulario = new FormularioInvitacion();
        formulario.setNombreUsuario(e.participanteUsuarios().get(0).getNombreUsuario());
        comunidades.invitarIntegrante(e.gestorUsuario().getId(), formulario);
        assertThat(integrantes.findByUsuarioIdAndActivoTrue(usuarioId)).isEmpty();
        Long invitacionId = comunidades.invitacionesRecibidas(usuarioId).get(0).id();

        comunidades.aceptarInvitacion(usuarioId, invitacionId);
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
        assertThat(integrantes.findByUsuarioIdOrderByAltaEnDesc(usuarioId)).hasSize(2)
                .anySatisfy(i -> assertThat(i.isActivo()).isTrue())
                .anySatisfy(i -> assertThat(i.isActivo()).isFalse());
    }

    @Test
    void aceptacionesConcurrentesNuncaSuperanElCupo() throws Exception {
        Escenario e = fabrica.escenario(23); // 24 activos: queda exactamente un cupo
        List<Integrante> candidatos = List.of(
                fabrica.participanteNuevo(e.comunidad(), false), fabrica.participanteNuevo(e.comunidad(), false),
                fabrica.participanteNuevo(e.comunidad(), false), fabrica.participanteNuevo(e.comunidad(), false));
        List<Long> invitaciones = new ArrayList<>();
        for (Integrante candidato : candidatos) {
            FormularioInvitacion formulario = new FormularioInvitacion();
            formulario.setNombreUsuario(usuarios.findById(candidato.getUsuarioId()).orElseThrow().getNombreUsuario());
            comunidades.invitarIntegrante(e.gestorUsuario().getId(), formulario);
            invitaciones.add(comunidades.invitacionesRecibidas(candidato.getUsuarioId()).get(0).id());
        }

        ExecutorService pool = Executors.newFixedThreadPool(candidatos.size());
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int index = 0; index < candidatos.size(); index++) {
            Integrante c = candidatos.get(index);
            Long invitacionId = invitaciones.get(index);
            resultados.add(pool.submit(() -> {
                salida.await();
                try {
                    comunidades.aceptarInvitacion(c.getUsuarioId(), invitacionId);
                    return true;
                } catch (ConflictoException rechazo) {
                    return false;
                }
            }));
        }
        salida.countDown();
        long exitosos = 0;
        for (Future<Boolean> f : resultados) {
            if (f.get()) {
                exitosos++;
            }
        }
        pool.shutdown();

        assertThat(exitosos).isEqualTo(1);
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
    }

    @Test
    void altasDirectasConcurrentesTambienLasLimitaLaBaseDeDatos() throws Exception {
        Escenario e = fabrica.escenario(23); // 24 activos
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            resultados.add(pool.submit(() -> {
                salida.await();
                try {
                    fabrica.participanteNuevo(e.comunidad(), true);
                    return true;
                } catch (DataIntegrityViolationException rechazo) {
                    return false;
                }
            }));
        }
        salida.countDown();
        long exitosos = 0;
        for (Future<Boolean> f : resultados) {
            if (f.get()) {
                exitosos++;
            }
        }
        pool.shutdown();
        assertThat(exitosos).isEqualTo(1);
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
    }

    @Test
    void elGestorNoPuedeDarseDeBajaNiAdministrarOtraComunidad() {
        Escenario a = fabrica.escenario(2);
        Escenario b = fabrica.escenario(2);

        assertThatThrownBy(() -> comunidades.darDeBaja(a.gestorUsuario().getId(), a.gestor().getId()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> comunidades.darDeBaja(a.gestorUsuario().getId(), b.participantes().get(0).getId()))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(integrantes.findById(b.participantes().get(0).getId()).orElseThrow().isActivo()).isTrue();
    }

    @Test
    void unParticipanteNoPuedeAdministrarIntegrantes() {
        Escenario a = fabrica.escenario(2);
        assertThatThrownBy(() -> comunidades.darDeBaja(a.participanteUsuarios().get(0).getId(), a.participantes().get(1).getId()))
                .isInstanceOf(AccessDeniedException.class);
    }
}
