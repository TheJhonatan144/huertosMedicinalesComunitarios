package ec.edu.epn.huertos.comunidades;

import java.time.Instant;

public record InvitacionVista(Long id, String comunidad, String invitador, TipoInvitacion tipo, Instant creadaEn) { }
