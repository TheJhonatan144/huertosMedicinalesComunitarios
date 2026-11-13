# Plan de mejoras acordadas en reunión

Este plan conserva la aplicación Spring Boot, PostgreSQL, Flyway, Thymeleaf y el flujo actual de autorización. El cambio local de puerto a `8081` ya se configuró por separado.

## Estado actual comprobado

- `Huerto` guarda un solo `diseno`; no modela partes del huerto con configuraciones distintas.
- `Planta` pertenece al huerto, no a un área concreta.
- `EnvioEvidencia` conserva una clave, nombre, MIME, tamaño y hash. `ValidadorImagen` admite JPEG/PNG y `AlmacenamientoLocal` solo resuelve `.jpg` y `.png`.
- La evidencia queda pendiente y un usuario `ADMIN_MINISTERIO_SALUD` asignado la aprueba o rechaza antes de generar puntos.
- Las cuentas MSP no se crean desde el registro público. En desarrollo pueden cargarse cuentas de demostración.
- El balanceo automático de nuevas comunidades ya usa `ASIGNACION_REVISOR`.

## 1. Varios diseños dentro de un huerto

### Modelo propuesto

Agregar `espacio_huerto`, dependiente de un solo `huerto`, con nombre, diseño, área y descripción. Un huerto tendrá uno o más espacios: por ejemplo, “Terraza”, “Macetas” y “Llantas”. Los diseños seguirán usando los códigos existentes (`PISO_MACETAS`, `PISO_LLANTAS_PATIO`, etc.). La comunidad conserva su único huerto y ubicación del mapa.

### Migración y compatibilidad

Una migración Flyway creará la tabla y convertirá el diseño actual de cada huerto en un primer espacio, conservando su nombre, descripción y área. La aplicación pasará a leer y editar los espacios como fuente de verdad; el campo antiguo `huerto.diseno` se mantendrá inicialmente para reducir el riesgo de despliegue y se retirará en una migración posterior cuando no haya lecturas antiguas.

En V1 el alta y la edición permitirán seleccionar varios diseños; el área total se reparte equitativamente entre ellos y los nombres se generan a partir del diseño. Permitir nombres y áreas independientes por espacio queda como mejora posterior.

Las plantas seguirán vinculadas al huerto en esta etapa. Si más adelante se necesita indicar en qué espacios está cada planta, se añadirá una relación explícita planta–espacio; no se asumirá que cada planta pertenece a un único espacio.

## 2. Evidencias de video

### Regla inicial propuesta

- Mantener JPEG/PNG y añadir MP4 (video H.264) con duración máxima de 120 segundos; no exigir una duración mínima. El audio AAC es opcional.
- Límite inicial de 50 MB por video, configurable por propiedades de la aplicación.
- Verificar en servidor el contenedor, códec y duración real. No confiar en extensión, MIME ni metadatos enviados por el navegador.
- Mantener el ciclo actual: guardar envío pendiente, mostrarlo a su revisor MSP asignado, reproducirlo en el detalle, y generar puntos solo tras aprobación.

### Cambios técnicos

Extender `envio_evidencia` con tipo de medio y duración; conservar clave, tamaño, hash, historial de envíos y decisiones. El validador distinguirá imagen/video. El almacenamiento validará UUID y extensiones permitidas, y servirá el MIME correcto con soporte de rangos para reproducción. La subida de video se transmitirá a archivo temporal y no se cargará completa en memoria.

La imagen de ejecución Docker deberá incluir un verificador de medios disponible para validar MP4 y su duración. La carga local requerirá la misma herramienta. Si no se puede verificar un archivo, se rechazará con mensaje claro.

## 3. Perfil de administrador técnico

Agregar `ADMIN_TECNICO` como cuenta institucional, no seleccionable en el registro público. Su alcance será:

- Crear cuentas `ADMIN_MINISTERIO_SALUD` con credencial inicial.
- Consultar administradores y su carga asignada.
- Reasignar una comunidad como excepción, indicando motivo.
- No aprobar solicitudes comunitarias ni revisar evidencias salvo que se autentique separadamente con una cuenta MSP.

El mecanismo normal seguirá asignando comunidades nuevas al MSP activo con menor carga. El administrador técnico será quien gestione excepciones y, si se aprueba, reasigne comunidades a otro MSP activo. Las reasignaciones deben dejar registro de responsable anterior, responsable nuevo, actor técnico, fecha y motivo.

La primera cuenta `ADMIN_TECNICO` se provisionará mediante un mecanismo de inicialización de una sola vez y variables de entorno, nunca mediante una ruta pública ni credenciales fijas en Git. En perfil demo puede cargarse una cuenta ficticia documentada.

## 4. Secuencia de implementación y verificación

1. Zonas de huerto: migración con conversión de datos existentes, entidad/repositorio, alta múltiple, edición y visualización pública/gestor.
2. Videos: almacenamiento y límites, validación en servidor, persistencia, formulario, reproducción para MSP e historial.
3. Administrador técnico: rol y permisos, bootstrap seguro, administración de cuentas MSP, reasignación controlada y auditoría.
4. Verificar cada migración desde una base vacía y desde datos existentes; cubrir autorización, límites y flujo completo. Actualizar guías de ejecución Docker/local en `8081`.

## Decisiones recomendadas para V1

1. Video MP4 hasta 120 segundos, con máximo de 50 MB y sin mínimo de duración.
2. Una planta puede estar en varios espacios; su asignación planta–espacio queda fuera de la primera entrega de zonas para mantenerla sencilla.
3. El administrador técnico inicial se crea solo en despliegue controlado. El administrador técnico puede crear cuentas MSP y cambiar su contraseña temporal; la asignación comunitaria continúa automática y sus cambios manuales requieren motivo.
