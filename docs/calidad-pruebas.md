# Calidad y refactorización de la suite

Esta entrega organiza y refactoriza las pruebas del monolito por capas. No cambia el código de negocio de `src/main/java` ni modifica las migraciones. La guía de ejecución está en [src/test/README.md](../src/test/README.md).

## Cambios de estructura

- Las pruebas de cada función reflejan el módulo y la capa de producción: dominio, aplicación, persistencia o presentación.
- `arquitectura/` reúne las comprobaciones del código y las reglas de organización de la propia suite.
- `recorridos/` contiene el recorrido entre módulos; no se atribuye artificialmente a un único módulo.
- `soporte/datos` separa escenarios persistidos, formularios e imágenes. Las unitarias ya no necesitan la fábrica Spring para construir un PNG.
- `soporte/integracion` concentra configuración de PostgreSQL, limpieza y catálogos aislados.
- `soporte/concurrencia` concentra barreras, futuros, plazos y cierre de ejecutores.
- `soporte/validacion` administra el validador y su reloj fijo.

El archivo `ValidacionFormulariosTest` se divide en dos clases por responsabilidad: cuenta en autenticación y huerto en huertos. Se conservan sus comprobaciones. Los imports, paquetes y ubicaciones de las demás pruebas se actualizan sin eliminar sus escenarios funcionales.

## Criterios verificables

| Criterio | Cambio y evidencia |
|---|---|
| Coherencia arquitectónica | Paquetes por módulo/capa y tres reglas en `OrganizacionPruebasTest`; se mantienen las ocho reglas del código productivo |
| Selección clara | Etiquetas `unitaria`, `web`, `arquitectura` e `integracion`; sufijos `Test`/`IT` coherentes |
| Respuesta rápida durante desarrollo | Surefire ejecuta las rápidas en `test`; Failsafe agrega integración en `verify` |
| Aislamiento | Limpieza del esquema dedicado y archivos antes/después de cada caso; regresión repetida que deja deliberadamente datos confirmados |
| Repetibilidad | Reloj fijo para Bean Validation, imágenes deterministas, instancia JUnit por método y orden aleatorio con semilla |
| Cierre de recursos | ValidatorFactory cerrado, TempDir administrado por JUnit y ejecutores cerrados incluso con error |
| Concurrencia acotada | Barreras y futuros con límite; cancelación de tareas; regresiones de excepción y timeout |
| Fallos observables | Excepciones inesperadas de hilos se propagan; solo los rechazos de negocio esperados se convierten en resultado negativo |
| Respeto de otros datos | Limpieza limitada a `huertos_pruebas`, sin CASCADE; dato de control conservado en `public` durante la verificación local |

Estas comprobaciones no representan una certificación ISO, un porcentaje de cobertura o una garantía absoluta de ausencia de errores. No se midió cobertura ni se ejecutaron pruebas de mutación en esta entrega.

## Before y After con propósito

| Ciclo | Aplicación |
|---|---|
| `BeforeEach` | Formularios nuevos en cuenta/huerto; mocks y servicio de consulta pública; ranking; misión disponible; almacenamiento e imagen; ejecutor concurrente |
| `AfterEach` | Cierre y comprobación del ejecutor; ausencia de temporales del almacenamiento; limpieza de contexto de seguridad/petición en integración |
| `BeforeAll` / `AfterAll` | Apertura y cierre de ValidatorFactory con reloj fijo |
| `AfterTransaction` | Eliminación del esquema de catálogo después de finalizar el rollback |
| `beforeTestMethod` / `afterTestMethod` de Spring | Limpieza de datos y archivos antes de iniciar y después de terminar la transacción de la prueba |

La limpieza de base utiliza un listener con orden 3500, por fuera del listener transaccional de Spring (4000). Esto evita ejecutar un TRUNCATE mientras una prueba transaccional todavía mantiene bloqueos. También permite retirar cambios confirmados por peticiones HTTP o hilos concurrentes que el rollback del hilo principal no puede deshacer.

No se agregan After vacíos, asignaciones a null ni reinicios de mocks que oculten las interacciones de un caso. Los escenarios que necesitan distintos integrantes o estados mantienen su preparación explícita dentro del método.

## Fases y recursos

`mvn test` ejecuta pruebas unitarias, contratos y el slice MVC sin PostgreSQL. `mvn verify` ejecuta lo anterior, empaqueta y comprueba la integración con PostgreSQL real. Los resultados quedan separados en `target/surefire-reports` y `target/failsafe-reports`.

Testcontainers sigue siendo la opción predeterminada de integración. El modo PostgreSQL externo sigue disponible únicamente para bases desechables. Hikari y Flyway apuntan al esquema `huertos_pruebas`; se conservan el historial de migraciones y las nueve plantillas y nueve dependencias de misión. Las 22 tablas operativas actuales se vacían y sus identidades se reinician por caso.

Las pruebas no se ejecutan en paralelo entre clases/métodos porque comparten ese esquema aislado. Las carreras de negocio se comprueban con tareas simultáneas dentro del caso. La semilla de orden puede cambiarse por propiedad para buscar dependencias ocultas.

## Ciclo rojo, verde y refactor

1. Se escribieron dos comprobaciones de organización antes de trasladar y clasificar la suite.
2. Se ejecutaron sobre el estado anterior: **dos pruebas, dos fallos de aserción** por paquetes/ubicaciones y etiquetas. El fallo previsto está registrado en `CALIDAD/tmp/calidad-pruebas-rojo.log`.
3. Se reorganizaron las pruebas y se extrajo soporte reutilizable. Se agregó una tercera comprobación que impide presentar como unitario un test que arranca toda la aplicación o inyecta su contexto.
4. Se añadieron regresiones para concurrencia, cierre, timeout e independencia de datos y archivos.
5. Se ejecutaron las pruebas rápidas, la verificación completa y una repetición de integración con otra semilla.

El ciclo observado aplica a los nuevos contratos y a esta refactorización de la suite. No se afirma que las funcionalidades anteriores fueran desarrolladas originalmente con TDD. Before/After administran el ciclo de vida; por sí solos no constituyen TDD.

Para trabajo nuevo: reproducir una necesidad con una prueba pequeña que falle por la razón prevista; implementar lo mínimo; comprobar la prueba; refactorizar; ejecutar la fase del módulo y finalizar con `verify`.

## Alcance funcional

Esta entrega conserva las reglas y los defectos de negocio que se dejaron pendientes en la refactorización anterior. Una suite verde comprueba sus escenarios declarados; no demuestra que los casos aún no cubiertos estén resueltos. Los pendientes de eliminación, misiones concurrentes, cerramientos, cupos, áreas y estimación de ranking continúan registrados en `cambios-arquitectura.md`.

## Verificación local del 12 de noviembre de 2026

| Ejecución | Casos | Fallos | Errores | Omitidos |
|---|---:|---:|---:|---:|
| Surefire: rápidas | 66 | 0 | 0 | 0 |
| Failsafe: integración, semilla 20261112 | 111 | 0 | 0 | 0 |
| Total de la verificación completa | **177** | **0** | **0** | **0** |
| Repetición de integración, semilla 20261113 | 111 | 0 | 0 | 0 |

`mvn verify` completó con BUILD SUCCESS. Después se repitió la integración sobre la misma base temporal, sin reiniciarla, con otra semilla. Se confirmó que las mismas 17 clases de integración se ejecutaron en un orden distinto. Las 66 rápidas volvieron a pasar tras retirar una aserción duplicada que ahora se ejecuta desde AfterEach.

La comprobación posterior encontró cero filas en las 22 tablas operativas, ninguno de los esquemas temporales de catálogo/migración, nueve plantillas y nueve dependencias de misión conservadas y el dato de control de `public` intacto. El término esquema se refiere a PostgreSQL, no a una carpeta del sistema de archivos.

Entorno: Java 21.0.12.1, Maven 3.9.11 y PostgreSQL 17.11 local desechable. Se utilizó la opción de PostgreSQL externo de PruebaIntegracion; no se ejecutó Docker/Testcontainers en esta verificación. La ruta Testcontainers por defecto se conserva.

Evidencia resumida: [verificacion-pruebas.txt](verificacion-pruebas.txt). Mapa de ubicaciones: [mapa-pruebas.md](mapa-pruebas.md). Los logs detallados están en `CALIDAD/tmp/calidad-pruebas-*.log` y los resultados XML en los directorios de Surefire/Failsafe.

La rama local continúa siendo Ariel_Baldeon, sin nuevos commits ni push. Se conservaron los cambios de arquitectura de la entrega anterior.
