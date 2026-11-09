# Revisión de campos y preparación de la primera carga

El bloque 01 contiene diez fichas del catálogo común. La primera versión conserva como alcance los 29 nombres del documento 01, en bloques de 10, 10 y 9. Esta entrega prepara y prueba la carga de las primeras diez; no la aplica a la base local.

## Correspondencia de los campos

| Información de la ficha | Campo de especie | Tratamiento para la carga |
| --- | --- | --- |
| Nombre común | `nombre_comun` | Texto de hasta 100 caracteres. |
| Nombre científico | `nombre_cientifico` | Hasta 120 caracteres; se usa para detectar coincidencias en este lote. |
| Otros nombres | `otros_nombres` | Hasta 250 caracteres; conservar idiomas y contexto. |
| Familia botánica | `familia_botanica` | Hasta 120 caracteres. |
| Región de referencia | `especie_region` | Ejemplo regional del MSP, sin afirmar distribución exclusiva. |
| Provincias documentadas | `provincias` | Hasta 600 caracteres, con las limitaciones de las listas parciales. |
| Clima | `clima` | Hasta 600 caracteres; distinguir referencia general y localidad. |
| Intervalo de altitud | `altitud_minima`, `altitud_maxima` | Enteros entre 0 y 6500 m, mínimo menor o igual al máximo; pueden quedar nulos. |
| Alcance y datos puntuales de altitud | `observaciones_altitud` | Nuevo, hasta 1000 caracteres. No convertir una colecta en rango nacional. |
| Ciclo | `ciclo` | `CORTO` o `LARGO`. |
| Criterio del ciclo | `observaciones_ciclo` | Nuevo, hasta 1000 caracteres; duración del ejemplar y tiempo hasta cosecha se distinguen. |
| Cualidad tradicional | `cualidad` | `CALIENTE`, `FRIA`, `FRESCA`, `VARIABLE` o nulo para sin documentar. |
| Localidad, variante y tradición | `contexto_cualidad` | Nuevo, hasta 1500 caracteres; obligatorio si la cualidad es variable. |
| Usos atribuidos | `usos_medicinales` | Hasta 3000 caracteres; sin dosis ni procedimientos de tratamiento. |
| Saberes y contexto cultural | `saberes` | Hasta 3000 caracteres; incluye las propuestas de identificación cuando corresponden. |
| Respaldo | `fuente_documental` | Hasta 2000 caracteres; se conserva sin exigir una nueva revisión bibliográfica para este paso. |
| Fotografía | `foto_clave`, `foto_tipo`, `foto_fuente` | Sin fotografía en este bloque; se mantiene el marcador genérico existente. |

La migración V5 añade las tres aclaraciones y admite fresca y variable. No reescribe V1–V4, no incorpora fichas reales por sí misma y no altera tablas de comunidad, siembra o bodega. Se aplicará mediante Flyway cuando se arranque la aplicación con el código actualizado.

## Decisiones visibles en el bloque

| Ficha | Ciclo de trabajo | Cualidad | Altitud numérica |
| --- | --- | --- | --- |
| Achiote | Largo | Fría, contexto de Amaluza | Sin rango; observaciones de localidades. |
| Aguacate | Largo | Caliente, contexto de Loja | 1800–2600 m, ámbito de cultivo de la guía. |
| Ajo sacha | Largo | Sin documentar | Sin rango; colectas y ensayo separados. |
| Albahaca | Corto, manejo anual | Variable: caliente en MSP y fría para morada en Saraguro | Sin rango; registro de localidad. |
| Algodón | Largo, manejo multianual propuesto | Sin documentar | Sin rango; dos puntos documentados. |
| Café arábigo | Largo | Sin documentar | Sin rango nacional; referencias regionales en observaciones. |
| Canela amazónica | Largo | Sin documentar para este taxón | 0–1000 m, distribución publicada. |
| Ceibo costeño | Largo | Sin documentar | Sin rango nacional; etiqueta de Laipuna en observaciones. |
| Chilca | Largo | Caliente en MSP | 1000–4000 m, distribución publicada. |
| Escancel | Largo, material perenne de trabajo | Fresca para el nombre común en MSP | 0–3500 m, rango publicado; contexto y registro puntual conservados. |

Las identidades café arábigo, canela amazónica, ceibo costeño e Iresine diffusa son propuestas para concretar cuatro nombres del listado. No identifican ejemplares reales de una comunidad. Las limitaciones permanecen en las fichas, no se convierten en afirmaciones universales.

El filtro de altitud sigue consultando intervalos documentados; las seis fichas con puntos aislados no aparecen al activar ese filtro. Todas se muestran sin filtro de altitud y conservan sus datos puntuales en el detalle. El filtro no determina aptitud de siembra.

## Archivos preparados

- `bloque-01.json`: contenido legible de las diez fichas y criterios del lote. No contiene cantidades, comunidades ni estado plantada.
- `carga-bloque-01.sql`: carga manual PostgreSQL, fuera de las migraciones y de los recursos que se ejecutan al arrancar. Inserta únicamente especies y sus regiones.
- `revisar-bloque-01.sql`: simulación para `psql`; abre transacción, ejecuta el lote, muestra las fichas y finaliza con `ROLLBACK`. Requiere que la base de destino ya tenga V5.

La carga es atómica: cualquier error revierte el bloque. Si ya existe un nombre científico exacto, ignorando mayúsculas y espacios exteriores, se conserva esa ficha íntegra y se informa la omisión. No se completan ni se sobrescriben fichas existentes automáticamente. La repetición del lote no duplica esos nombres científicos. Si hay más de una coincidencia científica, o el mismo nombre común sin identificación, se detiene para revisar la ambigüedad. Esta comprobación no resuelve sinónimos botánicos ni variantes de autoría.

La simulación revierte las filas; las secuencias de identificadores pueden avanzar, como ocurre normalmente en PostgreSQL. No se deben interpretar huecos de identificadores como plantas faltantes.

Para aplicar después de revisar, el lote manual debe ejecutarse en una transacción con `COMMIT` sobre la base seleccionada. No ejecutar el archivo de carga directamente si se pretende solo una simulación. Este paso se deja preparado, no ejecutado.

## Datos propios de la comunidad

`planta` mantiene `cantidad`, `activa` y `plantada` de forma independiente. La carga no crea filas en esa tabla ni modifica las existentes. Una especie solo aparecerá sembrada cuando un gestor la seleccione para su huerto y marque ese estado. Bodega no interviene en la carga ni en la determinación de qué está plantado.

## Verificación

`mvn test` ejecuta las pruebas existentes y `CatalogoCargaInicialIT` en PostgreSQL aislado mediante Testcontainers. Se comprueban la carga de las diez fichas, las validaciones, las aclaraciones, la repetición sin duplicados ni sobrescritura, las ambigüedades y el formulario y detalle. La base local de desarrollo no se usa en estas pruebas.

Resultado de esta preparación: 77 pruebas aprobadas, 0 fallos y 0 errores. Incluye cinco pruebas nuevas de la carga inicial. Se verificó también que el contenido JSON coincide con el lote SQL. La comprobación de pantallas fue mediante HTTP; no se afirma una revisión visual en navegador.
