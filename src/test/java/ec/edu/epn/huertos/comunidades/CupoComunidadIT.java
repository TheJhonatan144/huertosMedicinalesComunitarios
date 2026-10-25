package ec.edu.epn.huertos.comunidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.edu.epn.huertos.FabricaDatos;
import ec.edu.epn.huertos.FabricaDatos.Escenario;
import ec.edu.epn.huertos.PruebaIntegracion;
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

/** Límite de 25 integrantes activos por comunidad (incluido el gestor), con PostgreSQL real. */
class CupoComunidadIT extends PruebaIntegracion {

    @Autowired FabricaDatos fabrica;
    @Autowired ComunidadServicio comunidades;
    @Autowired IntegranteRepository integrantes;

    /** Gestor + 24 participantes activos = 25 activos. */
    private Escenario comunidadLlena() {
        Escenario e = fabrica.escenario(24);
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
        return e;
    }

    @Test
    void labaseDeDatosRechazaElIntegranteNumero26() {
        Escenario e = comunidadLlena();
        assertThatThrownBy(() -> fabrica.participanteNuevo(e.comunidad(), true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("cupo_comunidad_excedido");
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
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
    void unaBajaLiberaCupoYLaReactivacionConLimiteLlenoSeRechaza() {
        Escenario e = comunidadLlena();
        Integrante inactivo = fabrica.participanteNuevo(e.comunidad(), false);

        assertThatThrownBy(() -> comunidades.reactivar(e.gestorUsuario().getId(), inactivo.getId()))
                .isInstanceOf(ConflictoException.class).hasMessageContaining("25");

        comunidades.darDeBaja(e.gestorUsuario().getId(), e.participantes().get(0).getId());
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(24);

        comunidades.reactivar(e.gestorUsuario().getId(), inactivo.getId());
        assertThat(integrantes.countByComunidadIdAndActivoTrue(e.comunidad().getId())).isEqualTo(25);
    }

    @Test
    void reactivacionesConcurrentesNuncaSuperanElCupo() throws Exception {
        Escenario e = fabrica.escenario(23); // 24 activos: queda exactamente un cupo
        List<Integrante> candidatos = List.of(
                fabrica.participanteNuevo(e.comunidad(), false), fabrica.participanteNuevo(e.comunidad(), false),
                fabrica.participanteNuevo(e.comunidad(), false), fabrica.participanteNuevo(e.comunidad(), false));

        ExecutorService pool = Executors.newFixedThreadPool(candidatos.size());
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (Integrante c : candidatos) {
            resultados.add(pool.submit(() -> {
                salida.await();
                try {
                    comunidades.reactivar(e.gestorUsuario().getId(), c.getId());
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
