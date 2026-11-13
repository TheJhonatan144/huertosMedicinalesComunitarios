package ec.edu.epn.huertos.comunidades.aplicacion;
import ec.edu.epn.huertos.comunidades.dominio.TipoInvitacion;
import java.time.Instant;

public record InvitacionVista(Long id, String comunidad, String invitador, TipoInvitacion tipo, Instant creadaEn) { }
