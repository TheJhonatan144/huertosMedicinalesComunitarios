package ec.edu.epn.huertos.evidencias.dominio;

import java.nio.file.Path;

public record VideoValidado(
        Path temporal, String nombreOriginal, long tamano, String sha256, int duracionSegundos) {}
