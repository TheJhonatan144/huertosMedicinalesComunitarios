package ec.edu.epn.huertos.evidencias;

import java.nio.file.Path;

public record VideoValidado(Path temporal, String nombreOriginal, long tamano, String sha256, int duracionSegundos) { }
