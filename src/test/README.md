# Organización y ejecución de pruebas

La suite conserva los casos funcionales existentes y separa su responsabilidad de la infraestructura que los prepara.

```text
java/ec/edu/epn/huertos/
  arquitectura/              reglas del código productivo y de esta suite
  <modulo>/<capa>/           pruebas del mismo módulo y capa de producción
  recorridos/                recorrido completo entre módulos
  soporte/
    datos/                   escenarios Spring, formularios e imágenes sintéticas
    integracion/             PostgreSQL, aislamiento, almacenamiento temporal y catálogos
    concurrencia/            tareas simultáneas con plazo y cierre garantizado
    validacion/              ValidatorFactory y reloj de validación
resources/
  junit-platform.properties ciclo por método, orden aleatorio reproducible y límites
```

## Tipos y fases

| Tipo | Convención | Etiqueta | Ejecución |
|---|---|---|---|
| Unitaria | `*Test` | `unitaria` | Surefire en `test` |
| Slice MVC sin BD | `*Test` | `web` | Surefire en `test` |
| Contrato de arquitectura | `*Test` | `arquitectura` | Surefire en `test` |
| Integración con PostgreSQL | `*IT` | `integracion`, heredada de la base | Failsafe en `integration-test` y `verify` |

```powershell
.\mvnw.cmd test                            # rápidas, sin Docker/PostgreSQL
.\mvnw.cmd verify                          # rápidas + integración; Docker por defecto
.\mvnw.cmd -Dtest=OrganizacionPruebasTest test
.\mvnw.cmd -Dit.test=RevisionServicioIT verify
```

Las clases `Test` no deben heredar la base de integración. Los helpers no tienen nombres que Maven confunda con clases ejecutables. `OrganizacionPruebasTest` comprueba ubicaciones/paquetes, etiquetas y que las unitarias no arranquen toda la aplicación ni inyecten beans de su contexto.

## Preparación y cierre

- `@BeforeEach`: crea formularios independientes, mocks, servicio bajo prueba, misión o almacenamiento cuando son comunes a los casos de la clase.
- `@AfterEach`: cierra ejecutores, comprueba la ausencia de archivos temporales y libera contexto de seguridad/petición según corresponda.
- `@BeforeAll` / `@AfterAll`: abre y cierra `ValidatorFactory`. Su reloj fijo evita que una restricción temporal cambie con la fecha de ejecución.
- `@TempDir`: JUnit entrega y retira el directorio del almacenamiento unitario.
- `@AfterTransaction`: retira esquemas de catálogo después del rollback; hacerlo antes podría bloquearse con la transacción todavía abierta.

No se agregan métodos vacíos de cierre ni se asigna `null` a campos para aparentar limpieza. Un objeto local o un mock sin recursos no necesita un `AfterEach` artificial. Se mantiene la instancia de prueba por método.

## Integración aislada

`PruebaIntegracion` comparte el servidor PostgreSQL, no los datos de los casos. El esquema de trabajo es exclusivamente `huertos_pruebas`. Las conexiones Hikari y Flyway apuntan a él.

`AislamientoIntegracion` se ejecuta antes de iniciar y después de terminar la transacción de cada prueba. Vacía únicamente las tablas operativas del esquema, reinicia sus identidades y conserva el historial de Flyway y las plantillas/dependencias de misiones. No utiliza `CASCADE`, por lo que no extiende el borrado a esquemas ajenos. También retira los archivos de un directorio temporal exclusivo de la suite. Su raíz se retira al cerrar normalmente la JVM.

Este orden es necesario: una prueba HTTP o concurrente confirma transacciones en otros hilos, y un rollback del hilo de JUnit no las deshace. Una prueba anotada con `@Transactional`, en cambio, debe finalizar primero para no mantener bloqueos durante la limpieza.

Las cargas de catálogo utilizan además un esquema independiente por caso. `AislamientoIntegracionIT` repite un caso que deja deliberadamente una cuenta confirmada y un archivo: cada repetición debe empezar sin ambos.

Para una base PostgreSQL 17 **desechable** ya iniciada:

```powershell
.\mvnw.cmd -Dhuertos.test.db.url=jdbc:postgresql://localhost:55439/base_pruebas -Dhuertos.test.db.usuario=usuario_pruebas -Dhuertos.test.db.clave=clave_pruebas verify
```

No utilizar una base con datos reales para ejecutar la suite. Además del esquema principal, las pruebas de migraciones crean y retiran sus propios esquemas temporales. La configuración de la aplicación productiva no se cambia.

## Concurrencia y repetibilidad

`EjecucionConcurrente` inicia las tareas con una barrera, acota las esperas y cancela futuros al terminar. Se cierra con try-with-resources, incluso ante una excepción. Los errores inesperados se propagan a JUnit; los rechazos de negocio previstos se distinguen en cada caso.

La suite no ejecuta clases o métodos de integración en paralelo porque comparte un esquema que se limpia por caso. La concurrencia de negocio sí se ejercita expresamente dentro de cada prueba.

Clases y métodos se ordenan aleatoriamente con semilla reproducible. La semilla predeterminada está en `junit-platform.properties`; puede cambiarse:

```powershell
.\mvnw.cmd -Djunit.jupiter.execution.order.random.seed=20261113 verify
```

`ImagenesPrueba` es una utilidad pura y determinista, independiente de Spring y del contador de escenarios. `FabricaDatos` solo se usa cuando se requieren escenarios persistidos. `FormulariosPrueba` devuelve objetos nuevos en cada llamada.

## TDD y refactorización

Agregar Before/After no convierte pruebas antiguas en TDD. Para esta reorganización se escribió primero un contrato de organización, se observó su fallo sobre la estructura anterior y se refactorizó hasta hacerlo pasar. Se incorporaron también regresiones para cierre, timeout y aislamiento.

Para cambios funcionales futuros: definir un caso pequeño, ejecutarlo y observar el fallo por la razón prevista; implementar lo mínimo; ejecutar el caso y la fase correspondiente; refactorizar conservando el resultado; finalizar con `verify`. Una prueba que nunca falló no demuestra por sí sola que pueda detectar la regresión pretendida.

## Resultados

Surefire escribe `target/surefire-reports`; Failsafe, `target/failsafe-reports`. La fase `verify` debe completar ambas. El resumen de la verificación local y el alcance de las afirmaciones están en `docs/calidad-pruebas.md`.
