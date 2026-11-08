# Arquitectura

**Monolito modular por capas** (sin microservicios). Cada módulo agrupa lo que cambia junto; no hay capas vacías.

## Módulos

| Módulo (paquete) | Contenido |
|---|---|
| `autenticacion` | `Usuario`, `Rol`, `UsuarioAutenticado`, `AutorizacionServicio` (rol verificado contra la BD), login/panel |
| `comunidades` | `Comunidad`, `Integrante`, `AsignacionRevisor`, `ComunidadServicio` (cupo, bajas, reactivaciones), panel del gestor |
| `huertos` | `Huerto`, `Planta`, `HuertoServicio`, formularios y controlador del gestor |
| `actividades` | `Actividad`, `ActividadParticipante`, `ActividadServicio`, `DetalleActividadServicio`, panel del participante |
| `evidencias` | `EnvioEvidencia`, `Revision`, `RevisionServicio`, `EvidenciaServicio`, `ValidadorImagen`, `AlmacenamientoArchivos`/`AlmacenamientoLocal` |
| `gamificacion` | `RecompensaIndividual`, `RecompensaComunitaria`, `GamificacionServicio`, niveles, progreso y ranking |
| `publico` | Mapa, ficha pública y API JSON pública (sin datos personales) |
| `demo` | Cargador de datos sintéticos (`@Profile("demo")`) |
| `compartido` | Seguridad, manejo de errores, excepciones de dominio, formato de fechas |

Capas dentro de cada módulo: **presentación** (controladores + plantillas Thymeleaf) → **aplicación** (servicios `@Transactional`) →
**dominio** (entidades con sus reglas, p. ej. `Actividad.aplicarDecision`) → **persistencia** (repositorios Spring Data + Flyway).
Las entidades guardan identificadores de otras entidades (no asociaciones JPA) para evitar cargas perezosas en la capa web
(`open-in-view` desactivado); las vistas se arman en los servicios con `record`.

## Roles

| Rol funcional | Constante técnica | Notas |
|---|---|---|
| Visitante | (anónimo) | Solo consulta pública |
| Gestor de huerto | `GESTOR_HUERTO` | Su comunidad y huerto |
| Participante | `PARTICIPANTE` | Datos propios y públicos |
| Administrador de evidencias | `ADMIN_EVIDENCIAS` | Solo comunidades asignadas; sin autorrevisión |

## Control de acceso (defensa en capas)

1. **Por URL** (`SeguridadConfig`): `/gestor/**`, `/participante/**`, `/revision/**` por rol; el resto de lo privado exige sesión.
2. **En servicios**: `AutorizacionServicio.exigirRol` consulta el rol en la BD; cada caso de uso verifica comunidad/propiedad del recurso
   (`AccessDeniedException` → 403). Ocultar botones en las plantillas no es control de acceso.
3. **En la base de datos**: claves compuestas `(id, comunidad_id)`, triggers (revisor asignado y no autor, cupo, inmutabilidad) y restricciones únicas.

Además: CSRF activo (formularios y logout por POST), contraseñas con hash (`{bcrypt}` vía `DelegatingPasswordEncoder`),
cabeceras CSP/Referrer-Policy, cookies de sesión `HttpOnly` y `SameSite=Lax`, validación de entrada en servidor (Bean Validation y reglas de servicio).

## Modelo de datos (Flyway V1–V3)

`usuario` · `comunidad` · `integrante` · `asignacion_revisor` · `huerto` · `planta` · `actividad` · `actividad_participante` ·
`envio_evidencia` · `revision` · `recompensa_individual` · `recompensa_comunitaria`.
Los totales (puntos, progreso, nivel) **se derivan** de los movimientos de recompensa: no hay contadores que puedan desincronizarse.

## Cómo se garantizan las reglas críticas

| Regla | Mecanismo en servicio | Respaldo en base de datos |
|---|---|---|
| Máximo 25 integrantes activos (incl. gestor); una baja libera cupo | `ComunidadServicio.reactivar` bloquea la fila de `comunidad` (`FOR UPDATE`) y cuenta | Trigger `validar_cupo_comunidad`: bloquea la misma fila y cuenta con una instantánea nueva; la 2.ª transacción espera y ve el alta de la 1.ª |
| Participantes solo activos y de la comunidad | `ActividadServicio.validarParticipantes` | FK compuestas `(integrante_id, comunidad_id)` y `(actividad_id, comunidad_id)` |
| Lista fija de participantes | Se asigna al registrar y se marca `participantes_fijados` | Triggers: `INSERT` rechazado si está fijada; `UPDATE`/`DELETE` siempre rechazados |
| Una sola decisión por envío | `RevisionServicio.decidir` bloquea el envío (`PESSIMISTIC_WRITE` = `SELECT … FOR UPDATE`) y exige estado `PENDIENTE`; el perdedor recibe `ConflictoException` sin efectos | `UNIQUE (envio_id)` en `revision`; índices parciales: un solo envío `PENDIENTE` y uno `APROBADA` por actividad |
| Rechazo exige observaciones | Validación en servicio | `CHECK` en `revision` |
| Solo revisor asignado, con rol y sin autorrevisión | `actividadRevisable` + `exigirRol` | Trigger `validar_revisor` |
| Pendiente/rechazada no genera puntos ni progreso | `otorgar` solo se invoca tras aprobar | Triggers de recompensa: exigen actividad `APROBADA` y, para individuales, participante de la lista |
| Recompensa única por actividad (sin duplicar por dobles clics, reintentos o concurrencia) | Chequeo idempotente en `GamificacionServicio.otorgar` dentro de la misma transacción | `UNIQUE (actividad_id, integrante_id)` y `UNIQUE (actividad_id)` |
| Historial no se altera | Entidades con columnas `updatable=false` | Triggers de inmutabilidad en `revision`, recompensas, participantes y borrado de envíos |
| Reenvíos concurrentes | `ActividadRepository.bloquearPorId` serializa por actividad | Índice parcial único de envío `PENDIENTE` |

**Transacción de la aprobación:** (1) bloquear envío, (2) validar permisos y estado, (3) insertar `revision`, (4) cambiar estados de envío y
actividad, (5) volcar a la BD (`flush`, porque los triggers consultan el estado persistido), (6) insertar recompensas individuales y comunitaria.
Todo o nada.

## Fotografías

`ValidadorImagen` comprueba tamaño, **firma binaria** (JPEG/PNG), coherencia con el decodificador de `ImageIO`, dimensiones máximas y decodifica la
imagen (con submuestreo para acotar memoria); ignora extensión y `Content-Type` declarados. La clave interna es `AAAA/MM/<uuid>.<ext>`;
`AlmacenamientoLocal` valida la clave con un patrón estricto y comprueba que la ruta resuelta no salga del directorio.
Detrás de la interfaz `AlmacenamientoArchivos` (reemplazable por otro medio). Se guarda **fuera de los recursos estáticos**
(`EVIDENCIAS_DIR`; volumen `huertos_evidencias` en Docker). Se sirven por `/evidencias/envios/{id}/foto` solo al gestor de la comunidad y al
revisor asignado, con `Cache-Control: no-store` y `X-Content-Type-Options: nosniff`. Si la transacción falla, el archivo recién guardado se elimina.

## Interfaz

Thymeleaf con un fragmento de página (`fragmentos/base`) y componentes (`fragmentos/componentes`), Bootstrap 5 local (sin CDN), JavaScript mínimo
(`app.js`: evita dobles envíos y confirma acciones; `mapa.js`: Leaflet). El mapa muestra siempre, debajo, una **lista alternativa** y avisa si Leaflet
o los mosaicos de OpenStreetMap no cargan.

## Perfiles

- *(por defecto)*: sin datos de demostración.
- `demo`: cargador de datos sintéticos si la tabla `usuario` está vacía; plantillas sin caché.
- `test`: usado por las pruebas (directorio temporal de evidencias).
