# Avance

Organizado por **entregables** (sin fechas: la fecha del 13/11/2026 no está confirmada). Última actualización: 2026-10-23.

| Fase | Entregable | Estado | Evidencia |
|---|---|---|---|
| 0. Diagnóstico | Hallazgos, supuestos, alcance provisional, pendientes | Hecha | Este documento, [alcance](alcance-prototipo.md), [pendientes](pendientes.md) |
| 1. Base ejecutable | Proyecto Maven/Spring Boot 4.1.2, Flyway, PostgreSQL, almacenamiento persistente, Docker Compose | Hecha y verificada | App arrancó local y en contenedor, migraciones V1–V3 aplicadas, datos y fotos persisten tras reinicio |
| 2. Autenticación y comunidades | Cuentas, roles, asignación de revisores, comunidades, integrantes, huertos, cupo 25, datos demo | Hecha y verificada | `CupoComunidadIT`, `SeguridadWebIT` |
| 3. Actividades y evidencias | Formularios, lista fija, edición pendiente, fotos persistentes, estados | Hecha y verificada | `ActividadServicioIT`, `ValidadorImagenTest`, `AlmacenamientoLocalTest` |
| 4. Revisión y recompensas | Bandeja, aprobar/rechazar, observaciones, reenvío, historial, concurrencia | Hecha y verificada | `RevisionServicioIT`, `RecorridoPrincipalIT` |
| 5. Consulta pública y progreso | Mapa, fichas, progreso, ranking, lista alternativa | Hecha; **mapa/UX sin revisión en navegador** | `SeguridadWebIT`, `GamificacionReglasTest`; recorrido curl |
| 6. Verificación y entrega | Pruebas, documentación, recorrido completo | Hecha (ver [pruebas](pruebas.md)) | Suite automatizada + recorrido HTTP real + Docker |

## Hallazgos de la fase 0

- El repositorio estaba **vacío** y no era un repositorio Git; se conservó todo lo existente (nada).
- Herramientas: JDK 21 y 25 (se usa 21), Maven 3.9.16, Docker 29.2.1, Node (no necesario). Docker Desktop estaba apagado y se inició.
- Los tres documentos de referencia **no estaban** → trazabilidad provisional.
- Los puertos 5432 y 8080 estaban ocupados por otros contenedores del equipo (no se tocaron): este proyecto usa 5433 y 8081 en el `.env` local.

## Problemas encontrados y corregidos durante la implementación

1. La validación de esquema de Hibernate rechazó `CHAR(64)` para el hash SHA-256 → se cambió a `VARCHAR(64)` en la migración V2 (aún sin uso real).
2. Un trigger compartido por las dos tablas de recompensa fallaba al leer `NEW.integrante_id` en la tabla comunitaria → se separó en dos funciones (hallado en el recorrido HTTP real; luego cubierto por pruebas).
3. Una plantilla usaba `#fields.hasGlobalErrors()` fuera del formulario → se movió dentro.
4. Una aserción de prueba esperaba una URL de redirección absoluta; la real es relativa → se corrigió la prueba, no el código.

Las migraciones se editaron porque aún no existía ninguna base con datos reales; **a partir de ahora cualquier cambio de esquema debe ir en una migración nueva (V4+)**.

## Próximos pasos sugeridos

1. Copiar los documentos de referencia a `docs/referencias/` y completar la matriz RF/RNF/RB.
2. Revisar visualmente en navegador (escritorio y móvil) y en un teléfono real el mapa y las pantallas.
3. Resolver las decisiones P-01 a P-12 de [pendientes](pendientes.md), empezando por RB06 y los límites de fotografía.
4. Elegir el siguiente incremento del [backlog](backlog.md).
