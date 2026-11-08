# Catálogo común, plantas de la comunidad y bodega

## Alcance de esta entrega

Implementación limitada al catálogo y registro de plantas y a la bodega mínima. No cambia actividades, revisión de evidencias, puntos, ranking ni el dibujo del huerto virtual. No altera la navegación global: se entra por **Plantas** y desde allí al catálogo y a bodega.

Decisiones confirmadas: catálogo común a todas las comunidades; solo gestores administran; cantidad y siembra pertenecen a cada comunidad. Un gestor que edita una ficha de especie modifica el catálogo compartido, tal como se informa en el formulario.

## Datos y migración

- `especie`: identidad botánica, otros nombres, familia, intervalo de altitud, provincias, clima, ciclo, cualidad tradicional, usos documentados, saberes, fuentes y foto referencial con atribución.
- `especie_region`: varias regiones por especie (Costa, Sierra, Amazonía, Insular). No se infiere la región a partir de un nombre o provincia. Los filtros sirven para consultar datos documentados, no para afirmar aptitud de siembra.
- `planta`: vínculo de una especie con un huerto; cantidad, `activa`, `plantada` y descripción local original. Las columnas históricas de nombres se conservan por compatibilidad; los nombres presentados proceden de la especie compartida.
- `articulo_bodega`: huerto, nombre, categoría y cantidad en unidades. No se vincula con `planta` ni cambia siembra, puntos o progreso.

V4 preserva plantas anteriores, sus cantidades, estado activo y descripción local. Agrupa las coincidencias exactas de nombre común/científico en el catálogo. Todas las plantas anteriores empiezan con `plantada = false`: no hay información para asumir una siembra. Las descripciones anteriores no se convierten en usos medicinales.

No se reescriben migraciones V1–V3. La actualización se aplica al siguiente arranque mediante Flyway.

## Pantallas

- `/gestor/plantas`: lista de la comunidad y activación de ficha.
- `/gestor/plantas/catalogo`: tarjetas y filtros por nombre, región, provincia y altitud.
- `/gestor/plantas/catalogo/nueva`: registrar especie común.
- `/gestor/plantas/catalogo/{id}`: consultar ficha y agregarla al huerto.
- `/gestor/plantas/catalogo/{id}/editar`: actualizar ficha común, conservando la foto cuando no se adjunta otra.
- `/gestor/plantas/{id}/siembra`: editar únicamente cantidad y estado plantada de la comunidad.
- `/gestor/bodega`: consulta, registro, edición y eliminación de artículos básicos.

Una especie no se selecciona dos veces para la misma comunidad a través del flujo de selección; se edita su registro existente. Selecciones concurrentes se serializan bloqueando la fila del huerto. No se borran posibles duplicados históricos.

Los campos desconocidos pueden quedar sin documentar. Usos medicinales, saberes y cualidad tradicional requieren una referencia. Las fotos se validan por contenido JPEG/PNG usando el componente existente, sin modificarlo; se guardan fuera de recursos estáticos. La imagen genérica local es un marcador claramente identificado, no una fotografía de una especie.

## Integración para la persona 3

`HuertoServicio.plantasPlantadasDeGestor(usuarioId)` devuelve las plantas del huerto del gestor con `plantada = true`. No filtra por activa ni por cantidad y no consulta bodega.

`GET /gestor/plantas/plantadas` ofrece el mismo conjunto en JSON para una sesión de gestor:

```json
[{"id": 1, "especieId": 2, "nombreComun": "Nombre registrado", "nombreCientifico": null,
  "cantidad": 3, "plantada": true, "activa": true,
  "fotoUrl": "/gestor/plantas/catalogo/2/foto"}]
```

Los identificadores son ilustrativos. El renderizador del huerto virtual queda a cargo de la persona 3. La foto y el endpoint requieren una sesión autorizada; no se introduce una nueva API pública.

## Preparación del contenido inicial

La revisión y los archivos para el primer bloque de diez están en [catalogo/primera-carga.md](catalogo/primera-carga.md). El objetivo inicial son los 29 nombres del documento 01, por bloques. El contenido real se prepara como una carga manual, fuera de Flyway; no se incorpora al arrancar. Las fotografías siguen pendientes y se conservan los registros existentes.

V5 añade observaciones de altitud, criterio del ciclo y contexto de cualidad tradicional, junto con las opciones fresca y variable. Los datos parciales de altitud se conservan como observaciones; no se fuerzan mínimos y máximos nacionales a partir de puntos de colecta. No modifica siembras ni cantidades.

Referencias recibidas: Definición del proyecto (p. 1: campos de plantas y bodega), presentación Huertos Medicinales Comunitarios y MSP 2017, sección 1.6 (plantas según región, clima y altura). No se incluyen los PDF personales en el repositorio.

## Verificación

`mvn clean test` ejecuta la suite existente y `CatalogoBodegaIT`: especie compartida entre dos comunidades con siembras distintas, edición compartida sin alterar cantidades, conservación de foto, filtros regionales, fuentes y altitud, renderizado HTTP de pantallas, contrato JSON, bodega independiente, acceso por rol/comunidad, CSRF, fotografía falsa y migración con datos preexistentes.

La revisión visual en navegador es adicional al renderizado HTTP de las plantillas; no se afirma accesibilidad ni rendimiento medidos.
