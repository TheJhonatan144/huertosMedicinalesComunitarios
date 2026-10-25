# Registro de decisiones

Cada fila indica su **tipo**:

- **Aprobada** — decisión registrada como aprobada por el equipo (según el encargo; la matriz Excel no estaba disponible para confirmarla).
- **Propuesta técnica** — elección de implementación hecha en este prototipo.
- **Pendiente** — requiere decisión del equipo.
- **Supuesto de demostración** — valor provisional para poder mostrar el funcionamiento.
- **Implementada y verificada** — se indica la evidencia en [pruebas](pruebas.md).

## Reglas de negocio

| ID | Decisión | Tipo | Estado de implementación |
|---|---|---|---|
| RB05 | El progreso/efecto solo procede de una evidencia **aprobada** (según el encargo) | Aprobada | Implementada y verificada |
| RB09 | Una actividad pendiente o rechazada no genera puntos ni progreso (según el encargo) | Aprobada | Implementada y verificada |
| RB06 | Valores de recompensa, incrementos de progreso y umbrales de nivel | **Pendiente** (no aprobada) | Configurables (`app.gamificacion.*`), marcados «de demostración» |
| D-01 | Máximo 25 integrantes activos por comunidad, incluido el gestor; una baja libera cupo | Regla obligatoria del encargo (identificador RB no verificado) | Implementada y verificada (servicio + trigger) |
| D-02 | Lista de participantes fijada al registrar; no se agregan, quitan ni sustituyen | Regla obligatoria del encargo | Implementada y verificada (trigger) |
| D-03 | Actividad editable solo mientras esté pendiente, sin tocar participantes | Regla obligatoria del encargo | Implementada y verificada |
| D-04 | Rechazo exige observaciones; reenvío conserva envíos, revisiones y observaciones | Regla obligatoria del encargo | Implementada y verificada |
| D-05 | Todos los seleccionados reciben la misma recompensa individual | Regla obligatoria del encargo | Implementada y verificada |
| D-06 | Puntos individuales y comunitarios independientes; ranking solo con comunitarios | Regla obligatoria del encargo | Implementada y verificada |
| D-07 | Niveles Aprendiz, Oficial, Maestro; Maestro sin cupo máximo | Regla obligatoria del encargo (umbrales: ver RB06) | Implementada y verificada (reglas puras) |
| D-08 | Revisor solo de comunidades asignadas y sin autorrevisión | Regla obligatoria del encargo | Implementada y verificada |
| D-09 | Sin duplicación de recompensas por reintentos, dobles clics, reenvíos o revisiones concurrentes | Regla obligatoria del encargo | Implementada y verificada con PostgreSQL real |

## Contradicción RF10 vs RB05/RB09 (registrada, no corregida en la fuente)

- **Qué dice el encargo:** RF10 conserva un comportamiento **provisional** que contradice RB05 y RB09 (aprobadas). El texto exacto de RF10 no pudo consultarse (falta el Excel).
- **Interpretación de trabajo:** se aplican RB05/RB09: el progreso se actualiza **exclusivamente después de aprobar** la evidencia (`GamificacionServicio.otorgar` solo se invoca desde la aprobación; triggers de BD lo refuerzan).
- **Propuesta de corrección de RF10 (para que el equipo la revise):** «El progreso del huerto virtual y los puntos se actualizan únicamente cuando una evidencia es aprobada por un administrador de evidencias autorizado; el registro, el envío o el reenvío de una actividad no modifican el progreso.»
- **Acción pendiente:** que el equipo modifique la fuente (Excel). **No** se modificó ningún documento de referencia.

## Decisiones técnicas (propuestas)

| ID | Decisión | Motivo |
|---|---|---|
| T-01 | Spring Boot **4.1.2** (último estable consultado el 2026-10-23 en `api.github.com/repos/spring-projects/spring-boot/releases`; 4.2.0-RC1 es *pre-release*) | Versión estable vigente con el mayor soporte restante |
| T-02 | Versiones gestionadas por el BOM de Boot 4.1.2 (leído de Maven Central): Hibernate 7.4.13.Final, Flyway 12.4.0, PostgreSQL JDBC 42.7.14, Spring Security 7.1.2, Testcontainers 2.0.5, Thymeleaf 3.1.5.RELEASE | Compatibilidad garantizada por el BOM |
| T-03 | Compilación con `--release 21` usando JDK 21.0.9 (Temurin). El equipo también tiene JDK 25; se evitó para no depender de él | El encargo exige Java 21 |
| T-04 | PostgreSQL **17-alpine**, Bootstrap **5.3.3** y Leaflet **1.9.4** (local, en `static/vendor`), Maven Wrapper 3.9.11 | Versiones estables conocidas; **no se verificó** en fuentes oficiales si existen versiones más recientes → revisar |
| T-05 | Triggers PL/pgSQL como última defensa de reglas críticas (cupo, inmutabilidad, revisor, recompensas) | Las restricciones declarativas no alcanzan para reglas entre tablas; la concurrencia debe resolverse en la BD |
| T-06 | Bloqueo pesimista (`FOR UPDATE`) del envío al decidir | Serializa decisiones; el perdedor falla limpio sin efectos |
| T-07 | Totales derivados de movimientos (sin contadores) | Imposible desincronizar puntos |
| T-08 | Entidades con identificadores en vez de asociaciones JPA | Evita cargas perezosas; consultas explícitas |
| T-09 | Fotografías JPEG/PNG únicamente (WebP/GIF excluidos) | `ImageIO` estándar puede verificar su contenido real sin dependencias nuevas |
| T-10 | Un rechazo de acceso a un recurso de otra comunidad responde **403**; un identificador inexistente responde **404** | Mensajes claros y pruebas inequívocas |
| T-11 | Rol verificado contra la BD en cada servicio (no solo en la sesión) | Cumple «autorización en servidor y servicios»; permite pruebas sin web |
| T-12 | Bootstrap y Leaflet servidos localmente | Sin CDN, funciona sin red salvo los mosaicos de OSM |

## Supuestos temporales de demostración

| ID | Supuesto | Dónde |
|---|---|---|
| S-01 | 10 puntos individuales, 20 puntos comunitarios y 5 de progreso por actividad aprobada | `application.yml` → `app.gamificacion` |
| S-02 | Oficial desde 50 puntos, Maestro desde 150; etapas del huerto en 0/10/30/60 de progreso; meta 100 | idem |
| S-03 | Fotografía: 5 MiB máx., 8000 px por lado, 25 MP | `app.almacenamiento` |
| S-04 | Cuentas conocidas con contraseña `Demo-2026!` solo con el perfil `demo` | `application-demo.yml`, `.env.example` |
| S-05 | Un huerto por comunidad; un usuario en una sola comunidad | Modelo de datos |
| S-06 | Tipos de actividad: riego manual, siembra, deshierbe, abonado orgánico, cosecha, mantenimiento | `TipoActividad` |
| S-07 | Las fotografías las ven solo el gestor de la comunidad y el revisor asignado (los participantes no) | `EvidenciaServicio` |

## Fechas

No se elabora cronograma con la fecha del 13 de noviembre de 2026 porque su vigencia no fue confirmada con el equipo. Las fases del [avance](avance.md) se organizan por entregables.
