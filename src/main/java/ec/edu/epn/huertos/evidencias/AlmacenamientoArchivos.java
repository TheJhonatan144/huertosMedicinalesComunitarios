package ec.edu.epn.huertos.evidencias;

import org.springframework.core.io.Resource;

/** Puerto de almacenamiento de fotografías; permite sustituir el disco local por otro medio sin tocar el dominio. */
public interface AlmacenamientoArchivos {

    /** Guarda la imagen y devuelve la clave interna (nunca derivada de datos del usuario). */
    String guardar(ImagenValidada imagen);

    Resource cargar(String clave);

    void eliminar(String clave);
}
