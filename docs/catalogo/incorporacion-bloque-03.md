# Incorporación del bloque 03 al catálogo local

Aplicada el 4 de noviembre de 2026. Incluye nueve fichas: orquídea ecuatoriana, tomillo, toronjil, taraxaco, ortiga menor, violeta, paico, yuca y uña de gato. Cada ficha tiene fotografía referencial, créditos y fuentes documentales.

## Resultado y alcance

- Nueve registros nuevos. El catálogo local pasó de 23 a 32 fichas, con 30 fotografías.
- Los bloques 01, 02 y 03 cubren los 29 nombres previstos en 30 registros botánicos, al separar los dos llantenes. Taraxaco representa un conjunto taxonómico, no una microspecie confirmada.
- Ruda y hierba luisa siguen como dos fichas de demostración anteriores, conservadas sin completar ni fotografiar en este bloque.
- Las cinco plantas comunitarias existentes conservan todas sus columnas: identificadores, especie, cantidad, activa y plantada, entre otras. La huella completa de la tabla planta antes y después fue `ddc2e6e8aee7b92168898181729cc8a2`.
- No se agregaron siembras comunitarias ni se modificó la bodega. La presencia en el catálogo no significa que una especie esté plantada.

Respaldo completo anterior: `datos/respaldos/catalogo-antes-bloque03-20261104.dump` (76849 bytes). Es local y está fuera del control de versiones; no reemplazarlo por un respaldo posterior si se necesita recuperar el estado previo.

## Decisiones documentales

Orquídeas se representa con `Epidendrum quitensium Rchb.f.`, elegida con autorización del usuario entre especies ecuatorianas documentadas. Kew respalda el nombre aceptado y la tesis UPS de Molina Sandoval (2020), tabla 13, documenta un ejemplar de Quito, provincias, distribución altitudinal y un uso tradicional atribuido. El resultado antioxidante de laboratorio no se presenta como eficacia clínica. La mención genérica de orquídeas para la Costa en el MSP no demuestra la aptitud de esta especie para esa región.

Taraxaco se registra como `Taraxacum sect. Taraxacum F.H.Wigg.`: POWO trata `T. officinale F.H.Wigg.` como sinónimo de esa sección. El nombre histórico permanece disponible para búsqueda; la foto representa ese conjunto y no determina una microspecie ecuatoriana.

Paico se registra con el nombre aceptado `Dysphania ambrosioides (L.) Mosyakin & Clemants`, conservando `Chenopodium ambrosioides L.` como sinónimo. Ortiga menor se limita a `Urtica urens L.`; violeta a `Viola odorata L.`; uña de gato a `Uncaria tomentosa (Willd. ex Schult.) DC.`. No se mezclan fichas de otros taxones que comparten esos nombres.

Las provincias son listas parciales de evidencias identificables. Puntos de colecta e intervalos contradictorios se conservan en observaciones, sin convertirlos en límites nacionales o agronómicos. La orquídea tiene 1600-2700 m como distribución recopilada por la tesis, no garantía de adaptación. Los otros ocho registros tienen mínimo y máximo nulos; por ello no aparecen al activar un filtro numérico de altitud.

La cualidad tradicional queda caliente para toronjil, ortiga y paico; fresca para taraxaco y violeta, con su atribución vernacular al MSP. Orquídea, tomillo, yuca y uña de gato quedan sin categoría térmica confirmada. Los nombres populares no acreditan por sí solos la identidad de ejemplares locales.

Corto/largo se usa como categoría de trabajo de duración botánica, diferenciada del plazo hasta cosecha. Las inferencias están señaladas; la orquídea conserva ciclo nulo porque no se confirmó su duración. No se inventan datos para completar casillas. Usos y saberes conservan fuentes y nivel de atribución, sin recetas, diagnósticos ni prescripciones.

## Archivos

- `bloque-03-depurado.json`: nueve fichas, matriz de fuentes y decisiones de revisión.
- `bloque-03-depurado.md`: versión de lectura con campos y bibliografía.
- `carga-bloque-03.sql`: carga manual fuera de Flyway; no se ejecuta al arrancar el proyecto.
- `fotografias/bloque-03/`: nueve JPEG, manifiesto SHA-256, dimensiones y créditos con enlaces y licencias.
- `scripts/cargar-fotos-catalogo.ps1 -Bloque 03`: validación de imágenes; con `-Aplicar`, adjunta fotos mediante los formularios autenticados existentes. Conserva cualquier fotografía que ya exista. El bloque predeterminado sigue siendo 01.
- `CatalogoBloqueTresIT`: cuatro pruebas de integración en PostgreSQL aislado.

La carga SQL conserva fichas documentadas existentes y solo completa una ficha previa si está enteramente vacía de información botánica adicional, regiones y fotografía. Rechaza nombres científicos ambiguos o fichas parciales sin fuente; no aplica equivalencias automáticas de nombres antiguos. No modifica tablas de plantas comunitarias, huertos, comunidades o bodega.

## Reproducción en otro entorno local

Este entorno ya está cargado. Para reproducirlo en otro equipo se necesitan V5 aplicada, un respaldo de ese equipo, PostgreSQL de Compose y la aplicación local funcionando. Desde la raíz del proyecto, en PowerShell 7:

```powershell
$loteCatalogo = Get-Content docs/catalogo/carga-bloque-03.sql -Raw
# Simulación: revisar los avisos antes de guardar.
('BEGIN;' + $loteCatalogo + 'ROLLBACK;') |
    docker compose exec -T db sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
# Aplicar solo si la simulación terminó correctamente.
('BEGIN;' + $loteCatalogo + 'COMMIT;') |
    docker compose exec -T db sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
.\scripts\cargar-fotos-catalogo.ps1 -Bloque 03
.\scripts\cargar-fotos-catalogo.ps1 -Bloque 03 -Aplicar
```

No se necesitan cambios de esquema ni reconstruir la aplicación para esta carga; se utiliza el modelo y los formularios ya implementados.

## Verificación realizada

La ejecución completa de `mvn test` verificó las 83 pruebas anteriores y tres de las cuatro nuevas. Se corrigió la preparación de metadatos de foto de una prueba nueva y se ejecutó `mvn -Dtest=CatalogoBloqueTresIT test`: cuatro aprobadas, cero fallos y cero errores. Los informes finales suman 87 pruebas aprobadas y ninguna omitida. No se probó sobre la base local: Testcontainers creó PostgreSQL aislado.

Las cuatro pruebas nuevas verifican carga de nueve fichas válidas conservando siembras y catálogo previo; conservación de ediciones y fotos al repetir; rechazo de identificación científica ambigua; y búsqueda de orquídea sin tilde y de los sinónimos históricos de paico y taraxaco. Se comprueban además los datos desconocidos y la altitud explícita de la orquídea.

En PostgreSQL local se simuló con ROLLBACK antes del COMMIT. Las nueve fotografías se guardaron con CSRF y sesión de gestor; cada archivo recuperado por HTTP coincidió con su hash SHA-256. Una segunda ejecución del SQL conservó las nueve fichas y dejó idéntica la huella de todo el catálogo, incluyendo fotografías. El total final fue 32 fichas y 30 fotos; la huella de todas las columnas de las cinco plantas comunitarias permaneció igual.

En navegador se comprobaron las 32 tarjetas, la búsqueda `orquidea` con un resultado y la ficha de Epidendrum quitensium con foto, créditos, fuentes, datos desconocidos visibles y control independiente «Ya está plantada» sin marcar. No se guardó una siembra de prueba. El PDF del bloque se renderizó y revisó visualmente: 13 páginas, con cada una de las nueve fichas completa en una página.
