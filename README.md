# Huertos Medicinales Comunitarios — prototipo funcional

Proyecto universitario de **Calidad de Software** (Escuela Politécnica Nacional, Quito). Aplicación web educativa y gamificada para
apoyar la gestión de huertos medicinales comunitarios del Ecuador.

**Recorrido principal:** mapa público → comunidad y huerto → actividad con participantes → fotografía como evidencia →
revisión humana autorizada → aprobación o rechazo (con observaciones y reenvío) → puntos y progreso → ranking e historial.

> Alcance, supuestos y pendientes: [docs/alcance-prototipo.md](docs/alcance-prototipo.md), [docs/pendientes.md](docs/pendientes.md).
> El riego es una **actividad manual**; no hay sensores ni automatización. Los datos son **sintéticos** y los textos de plantas son
> **ilustrativos**, sin recomendaciones clínicas.

## Tecnología

Java 21 · Spring Boot 4.1.2 · Maven Wrapper · Thymeleaf + Bootstrap 5 + JavaScript · Spring Security · PostgreSQL 17 ·
Spring Data JPA/Hibernate + Flyway · Docker Compose · JUnit 5 + Testcontainers · Leaflet + OpenStreetMap.
Detalle de versiones en [docs/decisiones.md](docs/decisiones.md).

## Requisitos

- JDK 21 (para compilar y ejecutar fuera de Docker; el proyecto compila con `--release 21`).
- Docker Desktop en ejecución (base de datos, despliegue y pruebas de integración con Testcontainers).

## Iniciar con Docker Compose (aplicación + base de datos)

```powershell
Copy-Item .env.example .env        # ajusta DB_CLAVE y, si hay conflictos, DB_PUERTO / APP_PUERTO
docker compose up -d --build
```

Abre <http://localhost:8080> (o el `APP_PUERTO` que definas). Con `PERFIL=demo` se cargan datos sintéticos **solo si la base está vacía**.
Los datos viven en los volúmenes `huertos_datos-postgres` (base) y `huertos_evidencias` (fotografías) y **sobreviven a los reinicios**.

## Iniciar en el equipo (solo la base en Docker)

```powershell
Copy-Item .env.example .env
.\scripts\iniciar-local.ps1        # levanta la base con compose y ejecuta la aplicación con Maven
```

Las fotografías se guardan en `./datos/evidencias` (ignorado por Git) o en la ruta de `EVIDENCIAS_DIR`.

> Si el puerto 5432 u 8080 ya está ocupado en tu equipo (por ejemplo, por otros contenedores), cambia `DB_PUERTO` y `APP_PUERTO` en `.env`.

## Ejecutar las pruebas

```powershell
.\mvnw.cmd test        # requiere Docker en ejecución (PostgreSQL real con Testcontainers)
```

Resultados verificados y su alcance: [docs/pruebas.md](docs/pruebas.md).

## Cuentas de demostración (solo perfil `demo`)

Contraseña de todas: `Demo-2026!` (configurable con `DEMO_CLAVE`). **Son credenciales conocidas: no usar fuera de la demostración.**

| Usuario | Rol funcional (constante técnica) | Comunidad |
|---|---|---|
| `gestor_a` | Gestor de huerto (`GESTOR_HUERTO`) | Sumak Allpa (A) |
| `participante_a1`, `a2`, `a3` | Participante (`PARTICIPANTE`) | A |
| `participante_a4` | Participante **de baja** (no seleccionable) | A |
| `revisor_a` | Administrador de evidencias (`ADMIN_EVIDENCIAS`) | revisa solo A |
| `gestor_b` | Gestor de huerto | Pachamama Wasi (B) |
| `participante_b1`, `b2` | Participante | B |
| `revisor_b` | Administrador de evidencias | revisa solo B |

Provisión provisional de usuarios: no hay registro público ni recuperación por correo. Las cuentas las crea el cargador de demostración
([DatosDemo](src/main/java/ec/edu/epn/huertos/demo/DatosDemo.java)); en otro entorno deben insertarse por el equipo (pendiente de definir).

## Guion corto de demostración

Guion completo en [docs/guion-demostracion.md](docs/guion-demostracion.md). Resumen:

1. Como visitante: `/mapa` → comunidad A → `/ranking`.
2. `gestor_a`: **Registrar actividad** (selecciona participantes activos y sube una foto JPEG/PNG).
3. `revisor_a`: Bandeja → **Rechazar** (observaciones obligatorias).
4. `gestor_a`: abre la actividad, lee las observaciones y **Reenvía** con otra foto.
5. `revisor_a`: **Aprobar** → puntos y progreso se asignan una sola vez.
6. `participante_a1` (10 puntos, nivel Aprendiz), `/ranking` (comunidad A con 20 puntos) y el historial del gestor.
7. Denegaciones: `gestor_b` o `revisor_b` intentan abrir la actividad o su foto → 403.

## Reiniciar los datos de demostración (manual; el sistema nunca borra datos por sí solo)

```powershell
docker compose down -v                 # BORRA la base y las fotos de los volúmenes Docker de este proyecto
Remove-Item -Recurse .\datos           # solo si ejecutaste la aplicación fuera de Docker
docker compose up -d --build           # se vuelve a cargar la demo
```

## Estructura

```
src/main/java/ec/edu/epn/huertos/
  autenticacion/  usuarios, roles, inicio de sesión, autorización por rol en servicios
  comunidades/    comunidades, integrantes (cupo 25), asignación de revisores, panel del gestor
  huertos/        huerto y plantas
  actividades/    actividades, participantes fijados, formularios, panel del participante
  evidencias/     fotografías (validación y almacenamiento), envíos, revisiones
  gamificacion/   recompensas, niveles, progreso del huerto, ranking
  publico/        mapa, ficha pública y API pública
  demo/           datos sintéticos (perfil demo)
  compartido/     seguridad, errores, utilidades
src/main/resources/db/migration/   migraciones Flyway V1–V3
docs/                              documentación académica y técnica
```

## Documentación

[alcance](docs/alcance-prototipo.md) · [arquitectura](docs/arquitectura.md) · [decisiones](docs/decisiones.md) ·
[pendientes](docs/pendientes.md) · [backlog](docs/backlog.md) · [trazabilidad](docs/trazabilidad.md) ·
[pruebas](docs/pruebas.md) · [guion](docs/guion-demostracion.md) · [avance](docs/avance.md)

Atribución del mapa: © colaboradores de [OpenStreetMap](https://www.openstreetmap.org/copyright), visible en el propio mapa.
