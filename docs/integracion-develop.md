# Integración de las nuevas funcionalidades de Develop

La integración combina `Ariel_Baldeon` en `92ddf99` con `origin/Develop` en `a6def9a`. Se realiza mediante un merge normal sobre Ariel_Baldeon: no reescribe el historial ni modifica las ramas Develop o main.

## Funcionalidades conservadas

- Revisión de siembras: bandeja de pendientes, aprobación, rechazo con observaciones y autorización por revisor asignado.
- Fecha de siembra y estado de revisión. Una pendiente permanece visible; aprobar conserva la fecha, rechazar retira la siembra del dibujo y volver a sembrar abre una nueva revisión.
- Indicadores de revisión en el huerto virtual y en las plantas del gestor, junto con el enlace a la nueva bandeja.
- Once ilustraciones adicionales y sus reglas de selección: orégano, orquídea, ortiga, paico, romero, ruda, tomillo, toronjil, uña de gato, violeta y yuca. La tabla registra ahora 31 archivos.
- Migración `V2026111102__revision_siembra_plantas.sql`, con el contenido exacto de Develop. Las migraciones anteriores no cambian.
- Corrección del constructor de GamificacionServicio en la prueba de ranking. La rama local ya tenía el mismo constructor corregido; conserva su preparación con BeforeEach.

## Resolución de conflictos

Había cinco conflictos de contenido: Planta, PlantaVirtualLectura, PosicionRankingTest, HuertoVirtualIT y SpritePlantaTest. Además de resolverlos se adaptaron las clases nuevas que Develop introducía en los paquetes planos anteriores.

| Elemento | Resolución |
|---|---|
| Planta | Conserva especieId como referencia escalar y agrega estadoRevision, plantadaEn, observacionesRevision y las transiciones nuevas. No recupera ManyToOne ni cascadas. |
| PlantaVirtualLectura | Mantiene el join explícito por ID y agrega el estado a la proyección y el filtro de rechazo. Corrige los nombres completos de DTO y enum en JPQL. |
| RevisionSiembraServicio | Se ubica en huertos/aplicacion, con transacción por defecto de lectura y escritura explícita al decidir. Conserva el bloqueo pesimista y los permisos. Resuelve el nombre vigente de especie mediante repositorio. |
| RevisionSiembraController | Se ubica en huertos/presentacion y solo consume el servicio y sus records. Los conflictos de negocio se manejan mediante la excepción base existente. |
| PlantaVista | Lleva los metadatos de revisión hasta las plantillas, sin exponer la entidad. Esto evita que la página del gestor falle al consultar estadoRevision. |
| Pruebas de ilustraciones | Conservan las etiquetas, paquetes y soporte local; adoptan los archivos y expectativas nuevas de Develop. Ajo sacha sustituye a romero en el caso de ilustración genérica. |

Los 16 archivos de interfaz, ilustraciones y migración SQL afectados por Develop se compararon con esa rama y se conservaron íntegros. Las clases Java se adaptaron a las capas existentes. El SQL histórico no se reescribe para simular el nuevo estado.

## Regresiones añadidas

- `huertos/dominio/PlantaTest`: seis casos sobre fecha, referencia a especie, cantidad, aprobación, rechazo, nueva siembra y decisiones inválidas o repetidas.
- `huertos/aplicacion/RevisionSiembraServicioIT`: nueve casos con PostgreSQL y MVC para bandeja, DTO, renderizado, aprobación y rechazo HTTP, aislamiento por comunidad, permisos, CSRF, doble decisión, concurrencia y lectura del nombre vigente del catálogo.

Las pruebas nuevas reutilizan el soporte de aislamiento y concurrencia. No crean una segunda infraestructura de pruebas ni alteran los datos de una aplicación real. Los resultados de esta integración se registran en `verificacion-integracion-develop.txt`.

## Documentación anterior

El informe PDF V1 y la evidencia anterior de 177 pruebas mantienen el corte de la entrega previa. No se presentan como documentación actualizada de estas nuevas funciones. Para la arquitectura vigente consultar `arquitectura.md` y este documento.

La integración no pretende corregir los otros pendientes funcionales registrados en `cambios-arquitectura.md`.
