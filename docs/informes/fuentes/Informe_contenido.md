# ALLPANA - Arquitectura y diseño técnico

Fuente editable de contenido; los diagramas vectoriales se entregan en SVG.


## Página 1

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
Arquitectura y diseño técnico
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
INFORME  /  01
HUERTOS MEDICINALES COMUNITARIOS
ALLPANA
Informe de la arquitectura implementada, el modelo de clases y los flujos críticos del
prototipo.
Monolito modular · Java 21 · Spring Boot · PostgreSQL
Ariel | Integrante 2
Versión 1.0 | Corte documental: 12 de noviembre de 2026
Rama local: Ariel_Baldeon | Base de Develop: 9a7bd05
Incluye 3 láminas de clases UML, 2 secuencias y despliegue local.


## Página 2

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
01. Alcance y criterio de lectura
La documentación representa el código local refactorizado, incluidos cambios aún sin commit.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
CONTEXTO  /  02
Objetivo
Explicar cómo está construido ALLPANA, qué responsabilidades tiene
cada parte y cómo colaboran sus clases. El informe cubre los cinco
puntos de la actividad: arquitectura, componentes, clases, secuencias y
despliegue.
Método de comprobación
Se contrastaron clases de dominio, servicios, controladores, migraciones,
configuración y archivos de despliegue. Los resultados de pruebas se
toman de la verificación local registrada; esta entrega documental no
vuelve a ejecutar la suite ni altera el negocio.
Qué implementa el prototipo
Gestiona comunidades, miembros, huertos, catálogo de especies,
plantas, actividades con evidencias, revisión humana, misiones, puntos,
ranking y una bodega básica. La interfaz pública muestra mapa y fichas;
las áreas privadas dependen del rol y del recurso autorizado.
Cómo leer los diagramas
Las clases muestran una selección de atributos y operaciones reales. Se
omiten getters repetitivos, constructores y campos secundarios. Las
asociaciones de dominio son referencias por ID/FK; no significan
navegación entre entidades JPA. Los elementos repetidos entre láminas
se marcan como referencia.
Estado observado: implementación local. Un diagrama describe su diseño; no certifica que todo requisito o caso límite esté resuelto.


## Página 3

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
02. Estilo arquitectónico y justificación
Un único proyecto Maven, un JAR de aplicación y una base de datos compartida.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
ARQUITECTURA  /  03
Monolito modular por capas
Los módulos agrupan capacidades del negocio y se dividen en
presentación, aplicación, dominio, persistencia y configuración cuando
las necesitan. Se despliegan juntos y se comunican mediante llamadas
Java síncronas. Thymeleaf y los recursos del navegador se empaquetan
en el mismo JAR.
Por qué encaja con el prototipo
Permite coordinar revisión, actividad, recompensas y misión dentro de
una transacción de PostgreSQL. Reduce la operación de infraestructura y
conserva el estilo de programación existente. La separación por capas
delimita cambios sin exigir reescribir el proyecto.
Costo y restricciones
La base y el despliegue son compartidos. Los módulos no son servicios
autónomos y existen dependencias entre sus casos de uso. Cambiar una
entidad o una migración puede afectar varias capacidades; por ello se
conservan contratos de lectura y pruebas de integración.
Precisión del término
No se presenta como arquitectura hexagonal pura: el dominio mantiene
anotaciones JPA y los servicios de aplicación usan repositorios concretos.
El puerto de archivos permite sustituir ese adaptador, pero no convierte
por sí solo todo el sistema en puertos y adaptadores.


## Página 4

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
03. Componentes y responsabilidades
La división funcional coincide con los paquetes productivos bajo ec.edu.epn.huertos.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
MÓDULOS  /  04
Módulo
Responsabilidad principal
autenticacion
Registro, usuarios, roles, autorización y recarga de identidad de sesión.
comunidades
Membresías, invitaciones, cupos, solicitudes, asignación de revisores y archivo/restauración.
huertos
Huerto real, espacios, cerramientos, especies compartidas, plantas y lectura del huerto virtual.
actividades / evidencias
Registro y participantes; validación y almacenamiento de archivos; envíos y revisión humana.
misiones / gamificacion
Plantillas y dependencias, estados por huerto, recompensas individuales/comunitarias y ranking.
bodega / publico
Inventario básico por huerto; portada, mapa y fichas para visitantes.
compartido / demo
Fechas, excepciones y errores comunes; carga sintética condicional del perfil demo.
Los módulos colaboran en aplicación. No hay una base de datos separada ni un proceso independiente por módulo.


## Página 5

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
04. Diagrama de arquitectura
Vista de ejecución y comunicación; todas las capas internas pertenecen al mismo proceso Java.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
COMPONENTES  /  05
Navegador
HTML, CSS y JavaScript
Bootstrap · Leaflet
Aplicación Spring Boot / JAR
Presentación
Spring MVC · controladores · Thymeleaf
Aplicación
Servicios, autorización, DTO y transacciones
Dominio
Entidades, estados y valores
Persistencia
JPA / JDBC / archivos
PostgreSQL 17
Datos + restricciones
Esquema por Flyway
Archivos privados
Fotos y videos
Fuera de static
OpenStreetMap
Teselas del mapa
Solicitadas por Leaflet
HTTP
 JDBC
HTTPS
Configuración conecta Security y propiedades. FFprobe es un proceso local para validar video; no un servicio remoto.


## Página 6

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
05. Fronteras y organización física
Las reglas se aplican por capa dentro de cada módulo funcional.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
CAPAS  /  06
Capa
Contrato y dependencias permitidas
presentacion
Adapta HTTP. Consume servicios y DTO de aplicación. No usa repositorios, SQL ni entidades JPA, tampoco anidadas.
aplicacion
Coordina casos de uso, permisos y transacciones. Usa dominio y persistencia. No depende de controladores ni ejecuta
JDBC/EntityManager.
dominio
Contiene entidades, enums, valores y reglas. No depende de las otras capas; conserva las anotaciones JPA existentes.
persistencia
Implementa consultas, joins, bloqueos y archivos. Usa dominio; puede devolver proyecciones de consulta de aplicación.
configuracion
Conecta infraestructura, propiedades y seguridad. No sustituye a los servicios del negocio.
Ubicación: src/main/java/ec/edu/epn/huertos/<modulo>/<capa>/. Las migraciones Java están en db/catalogo y las SQL en src/main/resources/db/migration. Las vistas
activas están en frontend/templates y los recursos en frontend/static.
La excepción de proyecciones persistencia → aplicación está documentada. No se declara un grafo acíclico de módulos.


## Página 7

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
06. Clases: comunidad y huerto
Atributos principales y métodos seleccionados. Asociación sólida = relación referencial por ID/FK.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
UML 1/3  /  07
autenticacion · entidad
Usuario
- id: Long
- nombreUsuario: String
- rol: Rol
- activo: boolean
+ cambiarRol(rol: Rol): void
comunidades · entidad
Integrante
- id, usuarioId: Long
- comunidadId: Long
- activo, esGestor: boolean
+ darDeBaja(): void
+ asumirGestion(): void
comunidades · entidad
Comunidad
- id: Long
- tipoComunidad: String
- activa: boolean
+ archivar(id: Long): void
+ restaurar(): void
huertos · entidad
Especie
- id: Long
- nombreComun: String
- regiones: Set<RegionPlanta>
+ actualizar(DatosEspecie): void
+ cambiarFoto(...): void
huertos · entidad
Planta
- id, huertoId: Long
- especieId: Long
- cantidad: int
+ cambiarSiembra(...): void
+ cambiarActiva(...): void
huertos · entidad
Huerto
- id, comunidadId: Long
- nombre: String
- areaM2: BigDecimal
+ actualizar(...): void
1
0..*
0..*
1
1
0..1
1
0..*
0..*
1
Usuario puede tener membresías históricas; solo una activa. Planta usa especieId, sin @ManyToOne.


## Página 8

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
07. Clases: actividades y evidencias
El historial conserva envíos anteriores; cada envío admite una única revisión.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
UML 2/3  /  08
comunidades · referencia
Integrante
- id: Long
- comunidadId: Long
+ isActivo(): boolean
actividades · entidad
ActividadParticipante
- id: Long
- actividadId: Long
- integranteId: Long
- comunidadId: Long
+ getIntegranteId(): Long
actividades · entidad
Actividad
- id, huertoId: Long
- registradaPor: Long
- estado: EstadoRevision
+ aplicarDecision(...): void
+ reabrirPorReenvio(): void
autenticacion · referencia
Usuario
- id: Long
- rol: Rol
+ getRol(): Rol
evidencias · entidad
Revision
- id, envioId: Long
- revisorId: Long
- decision: EstadoRevision
+ getDecision(): EstadoRevision
evidencias · entidad
EnvioEvidencia
- id, actividadId: Long
- numero: int
- claveAlmacenamiento: String
+ aplicarDecision(...): void
1
0..*
0..*
1
1
0..*
1
0..*
0..1
1
Una alta correcta crea el primer envío. Se muestra 0..* como cardinalidad estructural; la revisión no se edita.


## Página 9

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
08. Clases: misiones y recompensas
Referencias cruzadas explícitas por identificador; las recompensas son movimientos persistidos.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
UML 3/3  /  09
misiones · entidad
MisionPlantilla
- id: Long
- codigo: String
- puntosComunitarios: int
+ getRecurrencia(): RecurrenciaMision
misiones · entidad
MisionHuerto
- id, huertoId: Long
- plantillaId, actividadId: Long
- estado: EstadoMision
+ iniciar(id: Long): void
+ completar(): void
huertos · referencia
Huerto
- id: Long
- comunidadId: Long
+ getId(): Long
gamificacion · entidad
RecompensaIndividual
- id, actividadId: Long
- integranteId, comunidadId: Long
- puntos: int
+ getPuntos(): int
actividades · referencia
Actividad
- id: Long
- misionHuertoId: Long
- estado: EstadoRevision
+ aplicarDecision(...): void
gamificacion · entidad
RecompensaComunitaria
- id, actividadId: Long
- comunidadId: Long
- puntos, progreso: int
+ getProgreso(): int
1
0..*
0..*
1
0..*
1
1
0..1
0..1
0..1
Dos FK opcionales y únicas vinculan misión-actividad. Recompensa individual: máximo una por actividad/integrante.


## Página 10

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
09. Interpretación y clases complementarias
Las multiplicidades expresan estructura; las restricciones de estado completan el modelo.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
MODELO  /  10
Relación o clase
Interpretación y responsabilidad
Comunidad - Huerto
Cada huerto pertenece a una comunidad; comunidad_id es único en huerto. Una comunidad puede existir antes de registrar
su huerto.
Integrante / ActividadParticipante
Integrante representa una membresía. ActividadParticipante resuelve la participación en actividades y conserva comunidadId
para consistencia.
Especie - Planta
Especie conserva el catálogo compartido. Planta pertenece al huerto y aporta cantidad, activa y plantada. Los nombres de
catálogo se resuelven al construir las vistas.
EspacioHuerto / HuertoCerramiento
Distribución física y cerramientos del huerto. EspacioHuerto registra huertoId, diseño y área. No se dibuja una asignación
Planta-Espacio que el modelo no persiste.
ArticuloBodega
id y huertoId: Long; nombre: String; categoria: CategoriaArticulo; cantidad: int. actualizar(nombre, categoria, cantidad) no
recibe formularios.
AsignacionRevisor / SolicitudComunidad
La primera relaciona revisor y comunidad. La segunda conserva el expediente y decisión de alta o eliminación; no es una
subclase de Comunidad.
Las láminas son vistas parciales del modelo. No se emplea composición UML porque la eliminación en cascada no es su contrato.


## Página 11

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
10. Colaboración técnica de las clases
El controlador no entrega entidades al HTML: la frontera de lectura utiliza records de aplicación.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
DISEÑO  /  11
GestorHuertoController
Presentación: adapta petición, sesión y
respuesta MVC.
HuertosConsultaServicio
Aplicación: autoriza, consulta y construye
datos de pantalla.
HuertoVista / PlantaVista
Records de aplicación: valores y EspecieVista
anidada, sin entidades.
ActividadServicio
Aplicación: usa el puerto
AlmacenamientoArchivos para guardar
evidencias.
«interface»
AlmacenamientoArchivos
guardar(ImagenValidada): String
guardar(VideoValidado): String
cargar(String): Resource
eliminar(String): void
AlmacenamientoLocal
Persistencia: implementa el puerto. Escribe
claves seguras fuera del directorio público.
realiza
La fila superior resume una dependencia y su resultado. La inferior muestra el puerto existente para archivos. Spring Resource sigue formando parte de ese contrato;
no se afirma independencia total del framework.
Lecturas dentro de la transacción + open-in-view: false. No se delegan consultas diferidas a las plantillas.


## Página 12

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
11. Secuencia: registrar actividad
Flujo síncrono resumido; el servicio delimita la transacción de escritura.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
SECUENCIA  /  12
Gestor / MVC
ActividadServicio
Comunidad / misión
Repositorios
Puerto de archivos
1. registrar(gestorId, form, archivo, misionId?)
2. Verificar gestor, comunidad operativa
y misión disponible si corresponde
3. Buscar huerto; validar participantes
4. Validar fecha y archivo
5. Guardar Actividad y participantes; fijarlos
6. guardar(imagen/video validado)
7. Guardar EnvioEvidencia n.º 1
8. Retornar ID; confirmar transacción
Consistencia: el vínculo con la misión se registra si fue seleccionada. Una subida fallida cancela la operación; si la transacción revierte después de guardar el archivo, se
programa su compensación. Disco y base no forman una transacción distribuida.


## Página 13

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
12. Secuencia: decidir una evidencia
El servicio valida rol y decisión antes del bloqueo; POST /revision/envios/{envioId}/decision.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
SECUENCIA  /  13
RevisionController
RevisionServicio
Envios / revisiones
GamificacionServicio
MisionServicio
1. decidir(revisorId, envioId, decision, observaciones)
2. bloquearPorId(envioId)
SELECT ... FOR UPDATE
3. Verificar asignación, autoría y estado
4. Guardar Revision; actualizar estados; flush
5. Si APROBADA: otorgar(actividad)
6. Si tiene misión: completarPorActividad(...)
7. flush; commit al salir del servicio
8. Resultado y redirección MVC
Alternativas: un envío decidido produce conflicto; rechazar exige observaciones y no genera recompensas. El reenvío crea otra evidencia y conserva el historial. El bloqueo
protege la revisión del envío; no resuelve el pendiente de misiones distintas aprobadas a la vez.


## Página 14

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
13. Diagrama de despliegue
Escenario definido en compose.yaml y Dockerfile. Es documentación de configuración, no un despliegue nuevo.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
INFRAESTRUCTURA  /  14
Cliente web
Navegador del usuario
HTML / CSS / JavaScript
Equipo local / Docker Compose
Contenedor app
JRE Java 21 · usuario sin privilegios
Spring Boot + Thymeleaf
FFprobe para validar video
Puerto interno 8080
Contenedor db
postgres:17-alpine
Puerto interno 5432
Esquema migrado por Flyway
Volumen evidencias
Montado en /datos/evidencias
Volumen datos-postgres
Montado en /var/lib/postgresql/data
HTTP
:8081*
JDBC
* Puertos predeterminados: host 8081 → app 8080; base expuesta solo en 127.0.0.1:5432. El archivo .env aporta la configuración y las credenciales. La aplicación espera el
estado saludable de PostgreSQL.


## Página 15

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
14. Seguridad, persistencia y operación
Controles existentes y consecuencias del diseño.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
ATRIBUTOS DE CALIDAD  /  15
Seguridad en tres niveles
Spring Security aplica rutas, sesión y CSRF. Los servicios verifican usuario
activo, rol, membresía y propiedad/asignación del recurso. PostgreSQL
respalda las reglas con claves, índices y triggers. El rol
ADMIN_MINISTERIO_SALUD pertenece al prototipo; no implica
integración externa con un ministerio.
Esquema y transacciones
Flyway versiona la base y Hibernate valida el esquema con ddl-auto:
validate. Las entidades se consultan dentro de los servicios; open-in-view
está desactivado. Las transacciones de escritura conservan bloqueos e
idempotencia para revisión y recompensas.
Privacidad de archivos
Fotos y videos no se sirven como archivos estáticos públicos. El acceso
pasa por autorización y el adaptador valida claves/rutas. La eliminación
utiliza una cola persistida con reintentos. La recuperación ante fallos
requiere considerar juntos base y archivos; no se ha verificado aquí un
procedimiento de restauración.
Despliegue externo descrito
render.yaml declara una aplicación Docker y PostgreSQL. No configura
un disco persistente de evidencias. Por tanto, la conservación de
imágenes y videos no queda resuelta en ese archivo. Este informe no
afirma que se haya desplegado en Render ni verifica las condiciones
comerciales del proveedor.


## Página 16

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
15. Calidad de pruebas y trazabilidad
Resultados registrados el 12/11/2026; no se han vuelto a ejecutar durante la elaboración del informe.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
VERIFICACIÓN  /  16
Fase / comprobación
Resultado y propósito
Surefire: mvn test
66 casos rápidos: unitarias, slice web y arquitectura. Sin fallos, errores ni omitidos.
Failsafe: mvn verify
111 casos de integración; PostgreSQL 17.11 externo desechable. Verificación completa: 177 casos y BUILD SUCCESS.
Repetición de integración
111 casos, segunda semilla de orden y misma base temporal. Cero fallos; no es una prueba adicional contada dentro de
los 177.
Arquitectura productiva
8 reglas: fronteras de capas, SQL, entidades por ID, records, transacciones y recursos únicos.
Organización de la suite
3 reglas: paquetes y rutas coherentes, etiquetas/sufijos Test-IT y unitarias sin contexto completo Spring.
Before / After
Preparación nueva por caso, limpieza de datos y archivos fuera de la transacción, cierre de validadores y ejecutores.
TDD observado: dos contratos de organización se ejecutaron primero en rojo y luego pasaron tras refactorizar. No se atribuye TDD histórico al negocio. No se midió
cobertura ni mutación; el resultado verde se limita a los escenarios declarados.


## Página 17

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
16. Decisiones y límites de la versión
La conformidad de capas no equivale a ausencia de defectos funcionales.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
EVALUACIÓN  /  17
Decisión conservada
Consecuencia de diseño
IDs en lugar de relaciones JPA
Joins y proyecciones explícitos; sin cascadas implícitas entre entidades. Aplicación debe resolver datos relacionados.
Servicios + DTO de lectura
La interfaz no accede a repositorios ni recibe entidades. Los permisos y la materialización de vistas se centralizan.
Monolito con transacción compartida
Revisión y recompensas pueden confirmarse juntas. Crece el acoplamiento de coordinación entre módulos.
Archivo local con puerto
Puede reemplazarse el adaptador manteniendo su contrato. La durabilidad depende del volumen y la estrategia de
respaldo.
Pendientes funcionales documentados
Persisten casos de eliminación con relaciones nuevas; desbloqueo de misiones bajo aprobaciones simultáneas; sincronización de cerramientos; cupo al
cambiar el tipo de comunidad; reparto de áreas pequeñas y estimación del ranking con recompensas variables. Requieren corrección y regresiones específicas
antes de afirmar que están resueltos.
Las recompensas y umbrales de demostración son provisionales. Enumerar recurrencia no demuestra ejecución recurrente de misiones.


## Página 18

ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO
17. Fuentes y correspondencia de entrega
Fuentes primarias de la copia local, revisadas para este documento.
V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local
REFERENCIAS  /  18
Fuente dentro del repositorio
Uso en el informe
docs/arquitectura.md
Estilo, módulos, dependencias permitidas, entidades por ID, DTO y límites arquitectónicos.
src/main/java/ec/edu/epn/huertos/
Clases de dominio, firmas de métodos, servicios y controladores. Láminas UML y secuencias contrastadas con estas fuentes.
src/main/resources/db/migration/
Restricciones y esquema. Junto con anotaciones JPA, respalda la lectura de cardinalidades.
pom.xml; frontend/; application.yml
Java 21, Spring Boot 4.1.2, empaquetado de interfaz, persistencia y configuración de archivos.
compose.yaml; Dockerfile; render.yaml
Topología de despliegue, puertos, volúmenes y límites del almacenamiento externo descrito.
docs/calidad-pruebas.md;
docs/verificacion-pruebas.txt
Evidencia de las 177 pruebas, repetición e infraestructura efectivamente utilizada.
docs/cambios-arquitectura.md
Alcance de la refactorización y defectos funcionales pendientes.
Correspondencia con la actividad: arquitectura del software: secciones 02, 03, 05 y 14; diagrama de arquitectura: 04; diagrama de clases: 06-10; secuencias: 11-12;
despliegue: 13. Las láminas de clases se entregan también juntas en PDF y separadas en SVG para los aportes al documento 03_Diseño_V1.
