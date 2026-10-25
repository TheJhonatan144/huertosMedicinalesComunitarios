package ec.edu.epn.huertos.evidencias;

/** Imagen cuyo contenido real fue verificado; el tipo es el detectado, nunca el declarado por el navegador. */
public record ImagenValidada(byte[] contenido, String tipoContenido, String extension, String sha256,
                             String nombreOriginal) {

    public long tamano() {
        return contenido.length;
    }
}
