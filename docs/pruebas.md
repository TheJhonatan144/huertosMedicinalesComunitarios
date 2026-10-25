# Pruebas

## Cómo ejecutarlas

```powershell
.\mvnw.cmd test          # JDK 21 + Docker en ejecución (Testcontainers levanta PostgreSQL 17-alpine real)
```

`maven-surefire-plugin` está configurado para ejecutar `*Test` (unitarias) y `*IT` (integración) con `mvn test`.
Las pruebas de integración usan **una instancia real de PostgreSQL** (no H2 ni simulaciones) con las mismas migraciones Flyway que producción,
y **hilos reales** para la concurrencia. Cada prueba crea datos con nombres únicos.

## Resultado de la última ejecución completa (2026-10-23, `mvnw clean test`)

**63 pruebas, 0 fallos, 0 errores, 0 omitidas — BUILD SUCCESS (≈ 50 s en el equipo de desarrollo).**

| Clase | Pruebas | Qué demuestra |
|---|---|---|
| `CupoComunidadIT` | 7 | Máx. 25 activos (BD rechaza el 26.º), bajas liberan cupo, reactivaciones y altas concurrentes (4 hilos) no superan el cupo, permisos del gestor |
| `ActividadServicioIT` | 12 | Registro con participantes/foto, 0 puntos mientras está pendiente, participantes ajenos/inactivos rechazados, lista inmutable (también por SQL), edición solo pendiente, reenvío con historial |
| `RevisionServicioIT` | 13 | Misma recompensa para seleccionados, puntos individuales ≠ comunitarios, rechazo con observaciones, decisión repetida sin duplicar, **8 hilos y 2 revisores aprobando a la vez → 1 recompensa**, aprobar vs. rechazar simultáneos (5 rondas), reenvíos concurrentes, revisor no asignado/rol incorrecto/autorrevisión, ranking y empates, restricciones y triggers de BD |
| `SeguridadWebIT` | 10 | Acceso público, privacidad de fichas, redirección al login, permisos por rol, aislamiento entre comunidades, privacidad de fotos, CSRF, login/hash |
| `RecorridoPrincipalIT` | 1 | Recorrido HTTP completo: visitante → registro → rechazo (sin y con observaciones) → reenvío → aprobación doble → puntos/ranking/historial |
| `PlantasIT` | 3 | Gestión de plantas, ficha pública solo con activas, aislamiento y validación |
| `ValidadorImagenTest` | 10 | PNG/JPEG reales; texto con extensión `.jpg`, GIF, firma PNG + basura, PNG truncado, vacío, tamaño, dimensiones; saneo del nombre |
| `AlmacenamientoLocalTest` | 4 | Nombre interno seguro, sin restos temporales, sobrevive a una instancia nueva, rechaza recorrido de directorios |
| `GamificacionReglasTest` | 3 | Umbrales de niveles, Maestro sin tope, etapas del huerto (valores de demostración) |

## Verificaciones manuales realizadas (no automatizadas)

Contra la aplicación **en ejecución** (local y en contenedor) con `curl` y sesiones reales (CSRF real, formularios multipart reales):

- Páginas públicas 200; privadas 302 al login sin sesión; 403 por rol/comunidad equivocados; POST sin CSRF → 403.
- Archivo falso `.png` rechazado; registro válido; rechazo sin observaciones bloqueado; rechazo con observaciones; reenvío; aprobación; segundo intento de aprobación sin efecto.
- Resultado: participante con 10 puntos y nivel Aprendiz; ranking de la comunidad en 20; progreso 5.
- **Persistencia:** al cambiar de ejecución local a contenedor el ranking conservó sus puntos; tras `docker compose restart app` los puntos (40 acumulados por dos recorridos) y la fotografía creada en el contenedor seguían disponibles.
  (La fotografía de la ejecución local no aparece en el contenedor porque se guardó en `./datos/evidencias` del equipo y no en el volumen Docker: es esperado, no un fallo.)
- `docker compose up -d --build` construye la imagen (Dockerfile multi-etapa) e inicia ambos servicios.

## Hallazgos de las pruebas

El recorrido HTTP real encontró un error de un trigger (ver [avance](avance.md)) que las pruebas posteriores ya cubren.

## Lo que NO se verificó (no se afirma)

- **Navegador:** no se abrió ninguna pantalla en un navegador. El mapa Leaflet con mosaicos de OSM, el diseño responsive y la accesibilidad **no están comprobados visualmente**.
- **Rendimiento:** no se midió nada; no se afirma ningún tiempo de respuesta (por ejemplo, «consultas en 3 segundos»: faltan operación, carga y entorno definidos).
- **Cobertura:** no se calculó; no se afirma ningún porcentaje.
- **Pruebas de mutación y de seguridad ofensiva** (escaneo, fuzzing): no realizadas. La suficiencia de las pruebas se justifica solo por los casos listados.
- **Cumplimiento normativo** (protección de datos, accesibilidad u otras normas): no evaluado.
- Fotografías grandes reales de teléfonos (p. ej. 12–50 MP): no probadas; los límites son provisionales.
- Ejecución en Linux/macOS: solo probado en Windows 11 con Docker Desktop.
