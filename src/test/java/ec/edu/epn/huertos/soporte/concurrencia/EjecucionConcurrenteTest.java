package ec.edu.epn.huertos.soporte.concurrencia;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Tag("unitaria")
@Timeout(8)
class EjecucionConcurrenteTest {
    private EjecucionConcurrente ejecucion;

    @BeforeEach
    void prepararEjecutor() {
        ejecucion = new EjecucionConcurrente(2, Duration.ofSeconds(2));
    }

    @AfterEach
    void cerrarEjecutor() throws Exception {
        ejecucion.close();
        assertThat(ejecucion.terminada()).isTrue();
    }

    @Test
    void iniciaTodasLasTareasYRecogeSusResultados() throws Exception {
        CountDownLatch dentro = new CountDownLatch(2);
        AtomicInteger contador = new AtomicInteger();
        Callable<Integer> tarea =
                () -> {
                    dentro.countDown();
                    if (!dentro.await(1, TimeUnit.SECONDS)) throw new TimeoutException();
                    return contador.incrementAndGet();
                };
        assertThat(ejecucion.ejecutar(List.of(tarea, tarea))).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void unaExcepcionNoDejaHilosBloqueados() {
        assertThatThrownBy(
                        () -> {
                            try (var recurso = ejecucion) {
                                ejecucion.ejecutar(
                                        List.of(
                                                () -> {
                                                    throw new IllegalStateException(
                                                            "fallo previsto");
                                                },
                                                () -> {
                                                    new CountDownLatch(1).await();
                                                    return true;
                                                }));
                            }
                        })
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
        assertThat(ejecucion.terminada()).isTrue();
    }

    @Test
    void elLimiteCancelaUnaEsperaQueNoTermina() throws Exception {
        try (var corta = new EjecucionConcurrente(1, Duration.ofMillis(80))) {
            assertThatThrownBy(
                            () ->
                                    corta.ejecutar(
                                            List.of(
                                                    () -> {
                                                        new CountDownLatch(1).await();
                                                        return true;
                                                    })))
                    .isInstanceOf(TimeoutException.class);
        }
    }
}
