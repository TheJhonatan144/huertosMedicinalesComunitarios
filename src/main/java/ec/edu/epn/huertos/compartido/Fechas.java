package ec.edu.epn.huertos.compartido;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Formato de fechas para las vistas (zona horaria de Ecuador continental). */
public final class Fechas {

    public static final ZoneId ZONA = ZoneId.of("America/Guayaquil");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Fechas() {
    }

    public static String hora(Instant instante) {
        return instante == null ? "" : HORA.format(instante.atZone(ZONA));
    }

    public static String dia(LocalDate fecha) {
        return fecha == null ? "" : DIA.format(fecha);
    }

    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }
}
