package ec.edu.epn.huertos.soporte.concurrencia;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/** Inicio simultáneo, espera acotada y cierre garantizado aunque una tarea o aserción falle. */
public final class EjecucionConcurrente implements AutoCloseable {
    private final ExecutorService ejecutor;
    private final int hilos;
    private final Duration limite;

    public EjecucionConcurrente(int hilos, Duration limite) {
        if (hilos < 1 || limite.isNegative() || limite.isZero())
            throw new IllegalArgumentException("Hilos y límite deben ser positivos");
        this.hilos = hilos;
        this.limite = limite;
        this.ejecutor = Executors.newFixedThreadPool(hilos);
    }

    public <T> List<T> ejecutar(List<Callable<T>> tareas) throws Exception {
        if (tareas.size() > hilos)
            throw new IllegalArgumentException(
                    "Cada tarea necesita un hilo para la barrera de inicio");
        CountDownLatch listos = new CountDownLatch(tareas.size()), salida = new CountDownLatch(1);
        List<Future<T>> futuros = new ArrayList<>();
        long fin = System.nanoTime() + limite.toNanos();
        try {
            for (Callable<T> tarea : tareas)
                futuros.add(
                        ejecutor.submit(
                                () -> {
                                    listos.countDown();
                                    if (!salida.await(limite.toNanos(), TimeUnit.NANOSECONDS))
                                        throw new TimeoutException("Barrera de inicio agotada");
                                    return tarea.call();
                                }));
            if (!listos.await(limite.toNanos(), TimeUnit.NANOSECONDS))
                throw new TimeoutException("Los hilos no llegaron a la barrera");
            salida.countDown();
            List<T> resultados = new ArrayList<>();
            for (Future<T> futuro : futuros)
                resultados.add(
                        futuro.get(Math.max(1, fin - System.nanoTime()), TimeUnit.NANOSECONDS));
            return List.copyOf(resultados);
        } finally {
            salida.countDown();
            futuros.forEach(f -> f.cancel(true));
        }
    }

    public boolean terminada() {
        return ejecutor.isTerminated();
    }

    @Override
    public void close() throws InterruptedException {
        ejecutor.shutdownNow();
        if (!ejecutor.awaitTermination(5, TimeUnit.SECONDS))
            throw new IllegalStateException("Quedaron hilos de prueba activos");
    }

    public static long exitos(List<Callable<Boolean>> tareas) throws Exception {
        try (var ejecucion = new EjecucionConcurrente(tareas.size(), Duration.ofSeconds(30))) {
            return ejecucion.ejecutar(tareas).stream().filter(Boolean.TRUE::equals).count();
        }
    }
}
