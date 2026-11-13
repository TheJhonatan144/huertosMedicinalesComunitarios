# Cambios locales para cumplir la arquitectura por capas

Fecha: 12 de noviembre de 2026. Rama local: `Ariel_Baldeon`.

## Base y entrega

Se descargó `Develop` y se incorporó a la rama local mediante fast-forward: de `da813370029bb67a86fd1f08d2334977a6046c63` a `9a7bd05bc0c555bfaab7291342ed854660f8094d` (27 commits). No hubo conflictos ni cambios locales previos en este repositorio. Los cambios de arquitectura quedan en la copia de trabajo, sin commit ni push.

La arquitectura conservada es el monolito modular por capas de `docs/arquitectura.md`. Se mantiene Spring Boot, MVC/Thymeleaf, PostgreSQL, Maven y el despliegue como un único JAR. No se reescribieron las pantallas ni se cambió el framework.

## Cambios concretos

| Cambio | Antes | Ahora |
|---|---|---|
| Organización | Clases de todas las capas mezcladas en cada paquete funcional | 144 clases existentes trasladadas a presentacion/aplicacion/dominio/persistencia/configuracion |
| Controladores | Tres controladores consultaban UsuarioRepository | SesionServicio y AdminTecnicoServicio entregan identidad/listas preparadas desde aplicación |
| Modelo de pantalla | Entidades y listas de entidades en varios modelos MVC | Ocho records de lectura y servicios de consulta; Ficha pública también usa records anidados |
| Relación planta/especie | ManyToOne EAGER y cascada PERSIST | especieId escalar, consulta explícita, proyección de lectura y creación explícita de especie |
| Dominio y formularios | Especie y ArticuloBodega recibían formularios de aplicación | DatosEspecie como valor de dominio y parámetros explícitos para bodega |
| SQL | JDBC/EntityManager en servicios de misiones, catálogo, asignación y eliminación | Cuatro adaptadores de persistencia específicos; servicios coordinan dentro de transacciones |
| Eliminación | Coordinación de toda la comunidad dentro del módulo de evidencias | Coordinador y persistencia en comunidades; almacenamiento sigue siendo responsabilidad de evidencias |
| Transacciones | Algunos servicios no declaraban una política por defecto | Lectura por defecto en todos los servicios; comandos existentes conservan escritura |
| Errores | Controladores de solicitudes/administración técnica mostraban RuntimeException.getMessage | Capturan errores de negocio; integridad se traduce en el advice común |
| Frontend | Maven aceptaba una segunda ubicación antigua para vistas | frontend/ como ubicación única, exclusiones de Maven y regla automatizada |
| Contexto Docker | Incluía el árbol bin/ heredado | bin/ excluido del contexto; no se borró trabajo histórico |
| Prueba de ranking | Constructor anterior de cuatro argumentos | Constructor actualizado con la dependencia de misiones; sus siete pruebas vuelven a ejecutarse |
| Pruebas de catálogo | Compartían filas y dependían del orden de ejecución | Esquema temporal aislado por prueba, migrado y eliminado tras rollback |
| Verificación | No había reglas arquitectónicas ejecutables | ArquitecturaTest analiza bytecode, tipos de retorno y genéricos de llamadas de controladores |

Las firmas Java e imports internos cambian por los nuevos paquetes y fronteras. Las rutas HTTP, formularios del navegador, roles, reglas de recompensa, estados y tablas se conservan. No se modificó ninguna migración histórica ni se creó una migración nueva.

El puerto de archivos conserva su contrato. ImagenValidada y VideoValidado pasan a dominio y AlmacenamientoLocal a persistencia. La colección de regiones de una especie sigue siendo una colección de valores enum, no una asociación a otra entidad.

## Archivos principales

- `docs/arquitectura.md`: contrato de arquitectura vigente y diagrama.
- `docs/mapa-clases-arquitectura.md`: ubicación anterior y nueva de cada clase, además de las 16 clases incorporadas.
- `src/test/java/ec/edu/epn/huertos/arquitectura/ArquitecturaTest.java`: ocho comprobaciones de arquitectura.
- `huertos/aplicacion/HuertosConsultaServicio.java` y `actividades/aplicacion/ConsultaActividadServicio.java`: fronteras de lectura MVC.
- `autenticacion/aplicacion/SesionServicio.java`: recarga del principal sin repositorios en controladores.
- `huertos/dominio/Planta.java`, `DatosEspecie.java` y `bodega/dominio/ArticuloBodega.java`: dominio separado de asociaciones/formularios.
- `misiones/persistencia/MisionDependenciaRepository.java`, `huertos/persistencia/FotoCatalogoInicialRepository.java`, `comunidades/persistencia/AsignacionBloqueo.java` y `EliminacionComunidadRepository.java`: consultas y bloqueos fuera de servicios.

Las rutas abreviadas de módulos de esta lista están bajo `src/main/java/ec/edu/epn/huertos/`.

## Verificación realizada

Se ejecutó `mvn verify` sobre el POM del repositorio, sin exclusiones de clases, con Java 21.0.12.1 y PostgreSQL 17.11 en una base desechable. Resultado: **169 pruebas, 0 fallos, 0 errores, 0 omitidas; BUILD SUCCESS**. Incluye las 161 pruebas funcionales existentes y las ocho nuevas comprobaciones arquitectónicas.

Después se reforzó la comprobación de tipos de retorno genéricos usados por los controladores y se volvieron a ejecutar las ocho pruebas arquitectónicas: todas pasaron. No se alteraron fuentes productivas después del verify.

El JAR generado arrancó con el perfil demo en una segunda base vacía. Flyway aplicó las 24 migraciones, Hibernate validó el esquema y se cargaron las cuentas sintéticas. Se comprobaron por HTTP las páginas de visitante, gestor, participante, revisor y administrador técnico; selección de especie y DTO anidado; alta de bodega; registro y aprobación de una misión con foto; recompensa de cinco puntos comunitarios y uno de progreso; evidencia denegada al participante (403).

También se comprobó que el JAR no contiene clases productivas en los paquetes planos anteriores y que no cambió el SQL histórico. No se ejecutó Docker ni se hizo una revisión visual gráfica del navegador.

Artefacto: `target/huertos-medicinales-0.1.0-SNAPSHOT.jar`.
SHA-256: `18cf0c5bf5fda9b807141353eec7a041b67860fa03069842e26d381873ef1dd1`.

## Cómo repetir la validación

Solo reglas arquitectónicas (sin PostgreSQL/Docker):

```powershell
.\mvnw.cmd -Dtest=ArquitecturaTest test
```

Suite completa, con Docker activo:

```powershell
.\mvnw.cmd verify
```

También puede utilizarse una base PostgreSQL 17 desechable existente:

```powershell
.\mvnw.cmd -Dhuertos.test.db.url=jdbc:postgresql://localhost:55439/base_pruebas -Dhuertos.test.db.usuario=usuario_pruebas -Dhuertos.test.db.clave=clave_pruebas verify
```

El modo PostgreSQL externo está únicamente en el soporte de pruebas. No cambia la configuración de ejecución de la aplicación. La base usada debe ser de pruebas: las pruebas crean y modifican datos y esquemas.

## Alcance que no debe confundirse con esta reorganización

El cumplimiento comprobado es el de las capas y fronteras documentadas. No se afirma que los módulos funcionales sean autónomos ni que desaparezcan todas las dependencias entre casos de uso.

No se cambió el comportamiento de negocio para resolver todos los defectos del análisis anterior. Permanecen pendientes: eliminación con las nuevas relaciones de misiones/bodega/auditoría; desbloqueo de misiones bajo aprobaciones simultáneas; sincronización de cerramientos; cupo al cambiar el tipo de comunidad; reparto de áreas pequeñas y estimación del ranking con recompensas variables. El SQL de eliminación se trasladó, conservando su secuencia existente; no se presenta su traslado como una corrección de esas relaciones.

Sí se resolvieron la compilación de la prueba desactualizada y la exposición directa de errores del driver en los dos controladores tratados, porque impedían comprobar la refactorización o saltaban la frontera común de errores.

Al integrar otras ramas del equipo, sus imports deben adaptarse a las ubicaciones nuevas. El mapa de clases facilita esa revisión. El repositorio permanece listo para que se revisen y confirmen localmente los cambios; no se realizó ninguna publicación remota.

## Actualización posterior de la suite

La estructura, fases y soporte de pruebas se reorganizaron después de esta verificación histórica. El contrato vigente y sus resultados están en [calidad de pruebas](calidad-pruebas.md) y [guía de la suite](../src/test/README.md).
