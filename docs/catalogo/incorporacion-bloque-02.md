# Incorporación del bloque 02 al catálogo local

Aplicada el 4 de noviembre de 2026. Esta incorporación utiliza las once fichas de `bloque-02-depurado.json`, correspondientes a diez nombres del documento 01. Llantén mayor y llantén de hoja estrecha permanecen separados.

El JSON y el PDF de depuración conservan el estado histórico de la revisión documental anterior. Este archivo registra la incorporación posterior: los datos ya están en PostgreSQL local y las once fotos están adjuntas. Los datos sin documentar y las limitaciones de altitud, ciclo y cualidad se conservan.

## Resultado de la carga

- Nueve especies nuevas y dos fichas botánicas vacías completadas.
- Manzanilla pasó de `Matricaria chamomilla` a `Matricaria chamomilla L.` conservando su identificador.
- La ficha de demostración «Menta», identificada como `Mentha spicata`, se completó como hierbabuena `Mentha spicata L.`. El híbrido `Mentha × piperita L.` tiene ficha independiente.
- Veintitrés fichas totales: veintiuna documentadas de los bloques 01 y 02 y dos fichas de demostración conservadas, ruda y hierba luisa. Estas últimas aún no tienen fotografía ni ficha botánica completa.
- Las cinco plantas comunitarias existentes mantienen los mismos identificadores, referencias a especie, cantidades, activa y plantada. La comparación de todas sus columnas antes y después fue idéntica.
- Bodega no recibió cambios de código ni datos durante esta incorporación.

El respaldo anterior a la carga está en `datos/respaldos/catalogo-antes-bloque02-20261104.dump`, fuera del control de versiones. No eliminarlo ni reemplazarlo por un respaldo posterior si se necesita recuperar el estado previo.

## Archivos y reglas

`carga-bloque-02.sql` es una carga manual, fuera de Flyway. No se ejecuta automáticamente al arrancar ni crea registros en planta, huerto o comunidad. Las equivalencias de nombres científicos sin autoría están enumeradas para manzanilla y hierbabuena; no se aplica un algoritmo general de sinonimia.

La carga conserva fichas con fuente documental. Solo completa un registro existente si toda su información botánica adicional está nula y no tiene regiones ni foto. Si encuentra más de una coincidencia o una ficha parcialmente completada sin fuente, se detiene: no sobrescribe información previa. El incremento de versión protege la edición concurrente después de completar un registro vacío.

`fotografias/bloque-02/` contiene once JPEG, su manifiesto de hashes y dimensiones, y un README con autores, licencias y enlaces de origen. Las fotos son referencias de especies, no fotos de ejemplares de las comunidades. Se adjuntaron mediante los formularios autenticados existentes, respetando CSRF, validación de imagen y almacenamiento de evidencias.

`scripts/cargar-fotos-catalogo.ps1` admite `-Bloque 01` y `-Bloque 02`. Sin `-Aplicar` solo verifica archivos, hashes y créditos. Con `-Aplicar` solicita una cuenta de gestor y conserva cualquier foto que ya exista; no reemplaza fotos del usuario. El bloque predeterminado sigue siendo 01.

## Mejoras de la interfaz

- Búsqueda por nombre y provincia que ignora mayúsculas, tildes y espacios repetidos, conservando el texto original de las fichas.
- Recuento visible de fichas encontradas.
- Carga diferida y decodificación asíncrona de fotografías en tarjetas, con dimensiones declaradas.
- Campo de créditos fotográficos en varias líneas, conservando los saltos de línea al editar y consultar.

Los filtros siguen usando intervalos numéricos documentados. Las once fichas del bloque 02 conservan sus datos puntuales y dudas en observaciones, con mínimo y máximo nulos, por lo que no aparecen al activar un filtro de altitud. Esto es coherente con la depuración y no expresa que las especies estén ausentes de una altitud concreta.

## Reproducción en otro entorno local

Requiere V5 aplicada, un respaldo propio y la aplicación local funcionando. Desde la raíz del proyecto, en PowerShell 7:

```powershell
$loteCatalogo = Get-Content docs/catalogo/carga-bloque-02.sql -Raw
# Primero simular; termina sin guardar filas.
('BEGIN;' + $loteCatalogo + 'ROLLBACK;') |
    docker compose exec -T db sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
# Después de revisar el resultado, aplicar.
('BEGIN;' + $loteCatalogo + 'COMMIT;') |
    docker compose exec -T db sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
.\scripts\cargar-fotos-catalogo.ps1 -Bloque 02
.\scripts\cargar-fotos-catalogo.ps1 -Bloque 02 -Aplicar
```

Si la simulación presenta errores, no ejecutar la aplicación del lote. El comportamiento repetible evita duplicados: una segunda ejecución conserva las once fichas documentadas y sus fotos. No ejecutar estos comandos para volver a cargar este entorno: ya están aplicados.

## Verificación realizada

`mvn test`: 83 pruebas aprobadas, cero fallos y cero errores. Incluye seis pruebas del bloque 02: validaciones y campos desconocidos, dos equivalencias sin duplicar, siembras intactas, conservación al repetir, rechazo de ambigüedades y de fichas parciales, y búsqueda sin tildes y por sinónimos. Testcontainers usó PostgreSQL aislado, no la base local.

En la base local se simuló la carga con ROLLBACK antes de aplicarla con COMMIT. Tras subir las once imágenes se recuperó cada una por HTTP y se comprobó su hash SHA-256. Se repitieron la carga de datos y la de fotos: no se crearon duplicados ni se modificaron fichas documentadas o imágenes existentes. Las huellas del catálogo y de todas las columnas de planta permanecieron iguales en esa repetición.

En navegador se comprobaron el catálogo, la búsqueda `llanten` sin tilde con dos resultados y el detalle de Plantago lanceolata con fotografía, créditos, datos documentados y control «Ya está plantada». No se agregó una siembra de prueba a las comunidades.

## Próximo bloque

Continúan pendientes los nueve nombres del bloque 03: orquídeas, tomillo, toronjil, taraxaco, ortiga, violeta, paico, yuca y uña de gato. No se incluyen por el solo hecho de completar una cantidad ni se modifican otras secciones del proyecto.
