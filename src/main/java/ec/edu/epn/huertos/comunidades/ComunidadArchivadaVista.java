package ec.edu.epn.huertos.comunidades;

public record ComunidadArchivadaVista(Long comunidadId, String comunidad, String huerto,
                                      String provincia, String canton, Long archivadaPor,
                                      boolean eliminacionPendiente) {
}
