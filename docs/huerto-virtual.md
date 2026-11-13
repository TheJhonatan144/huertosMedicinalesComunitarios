# Huerto virtual 2D cenital

Vista ilustrada del huerto (estilo juego sencillo, vista desde arriba) que **solo lee** datos reales. Integrada hoy
únicamente en el **panel del gestor** (`/gestor`), donde reemplaza el bloque simple «Representación del huerto».

## Datos que usa

| Qué se dibuja | De dónde sale | Regla |
|---|---|---|
| Tipo de suelo (color, textura y etiqueta) | `huerto.tipo_suelo` (V8) | Variante propia para ARCILLOSO, LIMOSO, ARENOSO, MARGOSO, GREDOSO y PANTANOSO. Vacío o desconocido → apariencia neutra gris y aviso «Falta registrar el tipo de suelo» |
| Composición | `huerto.diseno` (13 códigos de V12) | Se agrupa por prefijo: `TIERRA_*` (hierba + marco de troncos), `VERTICAL_*` (panel con bolsillos), `COLGANTE_*` (viga y macetas que se mecen), `PISO_*` (macetas sobre baldosas). Código que no sea uno de los 13 → composición neutra. La etiqueta muestra el código concreto («En piso · gradas») |
| Plantas | `planta` del huerto + su `especie` (`PlantaVirtualLectura`, join explícito por `especieId`) | Elegible = `plantada = true`, `activa = true` y revisión distinta de `RECHAZADA`. Las pendientes también se muestran. **No** usa `cantidad` ni bodega. Orden: nombre común de la especie ascendente (empate por id de planta). Se dibujan hasta **9** (cuadrícula 3×3) después de filtrar; si hay más: «Mostrando 9 de N» |
| Ilustración | `SpritePlanta`: nombre científico de la especie y, como respaldo, nombre común (ver «Ilustraciones de plantas») | Especie con archivo → `<img>` de `/img/huerto/plantas/<clave>.svg`; sin archivo → sprite **genérico** incrustado. Nunca se muestran fotos reales |
| Progreso | `GamificacionServicio.progreso(comunidadId)` | Solo se lee. La vista no otorga puntos ni recalcula; solo las actividades aprobadas lo hacen crecer (regla existente) |

Accesibilidad: el dibujo es `aria-hidden` y va acompañado de un párrafo `visually-hidden` con la descripción textual
(suelo, diseño y plantas). Las animaciones (balanceo de hojas, flores, macetas colgantes) se detienen con
`prefers-reduced-motion: reduce`. Sin plantas se muestra «Tu parcela está lista y esperando plantas» y, al gestor, un
enlace a `/gestor/plantas` para marcarlas como plantadas.

## Ilustraciones de plantas

Las plantas se dibujan con ilustraciones SVG por especie en `frontend/static/img/huerto/plantas/<clave>.svg`
(servidas en `/img/huerto/plantas/<clave>.svg`). Son **ilustraciones referenciales**, no una representación botánica
exacta: sirven para reconocer la planta en el huerto virtual, no para identificarla.

Origen: Ilustraciones del equipo — autor/herramienta: [COMPLETAR]

Formato: `viewBox="0 0 1024 1024"`, fondo transparente, sin `<script>`, sin atributos `on*` y sin enlaces externos
(lo comprueba `IlustracionesPlantasTest`). Se muestran con `<img>` (nunca SVG incrustado, por peso y seguridad), con
`alt="Ilustración de <nombre común>"`, `loading="lazy"` y `decoding="async"`. Toda la planta se balancea unos grados
desde la base (`hv-planta` en `css/huerto-virtual.css`); con `prefers-reduced-motion: reduce` queda quieta.

### Cómo se elige la ilustración (`SpritePlanta`)

1. **Nombre científico** de la especie del catálogo, normalizado: sin autor, sin tildes, sin «×» (o «x» híbrida) ni
   «sect.»; se comparan **género y epíteto** («Mentha × piperita L.» → `mentha piperita`). Una fila con solo el género
   (Taraxacum) vale para todo el género.
2. Si no hay nombre científico o no coincide: **nombre común** normalizado (minúsculas, sin tildes ni espacios;
   coincidencia exacta, no parcial).
3. Si nada coincide: sprite **genérico** incrustado.

### Tabla especie → archivo

| Especie (catálogo) | Nombre científico | Archivo | Tamaño final |
|---|---|---|---|
| Achiote | *Bixa orellana* | `achiote.svg` | 68 KB |
| Aguacate | *Persea americana* | `aguacate.svg` | 56 KB |
| Albahaca | *Ocimum basilicum* | `albahaca.svg` | 34 KB |
| Algodón | *Gossypium barbadense* | `algodon.svg` | 52 KB |
| Café arábigo | *Coffea arabica* | `cafe.svg` | 67 KB |
| Canela amazónica | *Mespilodaphne quixos* | `canela.svg` | 104 KB |
| Ceibo costeño | *Ceiba trischistandra* | `ceibo.svg` | 225 KB |
| Chilca | *Baccharis latifolia* | `chilca.svg` | 176 KB |
| Escancel | *Iresine diffusa* | `escancel.svg` | 319 KB |
| Guayusa | *Ilex guayusa* | `guayusa.svg` | 181 KB |
| Jengibre | *Zingiber officinale* | `jengibre.svg` | 275 KB |
| Manzanilla | *Matricaria chamomilla* | `manzanilla.svg` | 233 KB |
| Matico | *Piper aduncum* | `matico.svg` | 161 KB |
| Menta | *Mentha × piperita* | `menta.svg` | 253 KB |
| Hierbabuena | *Mentha spicata* | `hierbabuena.svg` | 235 KB |
| Llantén mayor | *Plantago major* | `llanten-mayor.svg` | 171 KB |
| Llantén de hoja estrecha | *Plantago lanceolata* | `llanten-hoja-estrecha.svg` | 336 KB |
| Marco | *Ambrosia arborescens* | `marco.svg` | 223 KB |
| Taraxaco | *Taraxacum* (sect. Taraxacum) | `taraxaco.svg` | 213 KB |
| Orégano | *Origanum vulgare* | `oregano.svg` | 210 KB |
| Orquídea ecuatoriana | *Epidendrum quitensium* | `orquidea.svg` | 231 KB |
| Ortiga menor | *Urtica urens* | `ortiga.svg` | 218 KB |
| Paico | *Dysphania ambrosioides* | `paico.svg` | 279 KB |
| Romero | *Salvia rosmarinus* (también *Rosmarinus officinalis*) | `romero.svg` | 381 KB |
| Tomillo | *Thymus vulgaris* | `tomillo.svg` | 573 KB |
| Toronjil | *Melissa officinalis* | `toronjil.svg` | 230 KB |
| Uña de gato | *Uncaria tomentosa* | `una-de-gato.svg` | 157 KB |
| Violeta | *Viola odorata* | `violeta.svg` | 230 KB |
| Yuca | *Manihot esculenta* | `yuca.svg` | 189 KB |
| Hierba luisa (no está en el catálogo; solo por nombre común) | — | `hierba-luisa.svg` | 101 KB |
| Ruda (no está en el catálogo; solo por nombre común) | — | `ruda.svg` | 259 KB |

Un sinónimo científico (como *Rosmarinus officinalis* para romero) se agrega como otra línea con la misma clave y sin
nombres comunes.

Tamaños tras optimizar con SVGO 4.1.0 (`--multipass --precision 1`, ejecutado con `npx`, sin agregarlo al proyecto):
31 archivos, de 15,6 MB a 6,3 MB en total (−60 %). Se compararon en Edge original y optimizado píxel a píxel (1024 y 300 px): solo
cambian bordes de 1 px por antialiasing, ninguna zona de relleno, así que se conservaron los optimizados. Comprimidos
con gzip pesarían alrededor de un tercio (p. ej., `achiote.svg` 68 KB → 25 KB), pero la compresión HTTP no está
activada en la aplicación.

### Cómo agregar una ilustración

1. Copiar el SVG (viewBox 1024×1024, fondo transparente, sin scripts ni enlaces) a
   `frontend/static/img/huerto/plantas/<clave>.svg`. Clave en minúsculas, sin tildes, palabras separadas por guion.
   Opcional: `npx svgo --multipass --precision 1 <archivo>` y comparar la apariencia antes de reemplazar.
2. Agregar **una línea** a `SpritePlanta.ILUSTRACIONES`:
   `new Ilustracion("<clave>", "<Género epíteto>", "<nombre común>", ...)` (científico `null` si la especie no está en
   el catálogo).
3. Correr `SpritePlantaTest` e `IlustracionesPlantasTest`. Exigen que el archivo exista y sea seguro, y que la carpeta
   tenga exactamente un SVG por clave: un archivo sin registrar en la tabla hace fallar la prueba.

Especie del catálogo que aún usa el sprite genérico: Ajo sacha (*Mansoa alliacea*). El archivo
`sin_identificar_hojas_corazon.svg` no tiene especie asignada y se sacó del repositorio.

## Autorización y aislamiento

- `HuertoVirtualServicio.paraGestor(usuarioId)` deduce la comunidad con `HuertoServicio.huertoDeGestor` (exige rol de
  gestor activo). No recibe un id de comunidad, así que un gestor nunca ve el huerto de otra comunidad.
- `HuertoVirtualServicio.paraComunidad(comunidadId)` es de solo lectura y **no autoriza**: quien la llame debe obtener
  `comunidadId` de una fuente ya autorizada. Devuelve `Optional.empty()` si la comunidad no tiene huerto y nunca muestra
  enlaces de gestión (`puedeRegistrar = false`).

## Archivos

- Nuevos: `HuertoVirtual` (record + mapeo de códigos), `HuertoVirtualServicio`, `SpritePlanta`,
  `PlantaVirtualLectura` (repositorio de solo lectura con `@Query`, extiende `Repository<Planta, Long>`),
  `PlantaPlantadaVista` (record de proyección), `templates/fragmentos/huerto-virtual.html`, `static/css/huerto-virtual.css`.
- Modificados: `GestorPanelController` (atributo `huertoVirtual`), `gestor/panel.html` (bloque reemplazado),
  `fragmentos/base.html` (enlace al CSS).
- Pruebas: `HuertoVirtualIT`, `HuertoVirtualTest`, `PlantaVirtualLecturaIT`, `SpritePlantaTest`.
- La incorporación posterior de revisión de siembras agrega `V2026111102__revision_siembra_plantas.sql`, consultas en `PlantaRepository` y metadatos en `Planta`. `RevisionSiembraServicio` está en aplicación y su controlador en presentación; las lecturas web utilizan DTO.
- Las clases CSS usan el prefijo `hv-` para no chocar con `.huerto-virtual` y `.suelo-*` que ya existen en `app.css`.

## Cómo mostrarlo en otras pantallas (para sus dueños)

Esta rama **no** edita esas pantallas. El fragmento recibe dos parámetros: la vista y si incluye la barra de progreso.

> **`paraComunidad(...)` no autoriza.** Solo lee el huerto de la comunidad que se le pasa. Quien inserte el fragmento
> (ficha pública, panel del participante) es responsable de verificar los permisos antes de llamarlo: obtener el
> `comunidadId` de una fuente ya autorizada (la comunidad pública que se está mostrando, o la del integrante activo
> autenticado; nunca un parámetro libre de la URL sin validar) y mostrar solo datos públicos. El fragmento no muestra
> fotos de evidencias ni enlaces de gestión con `paraComunidad`, pero sí el nombre del huerto, el suelo, el diseño, los
> nombres de las plantas plantadas y el progreso; si alguna comunidad no debe exponerlos (archivada, eliminación
> pendiente, no pública), la pantalla que llama debe filtrarla antes.

**Ficha pública de comunidad** (`publico/comunidad.html`, `/comunidades/{id}`):

```java
// En el controlador/servicio que arma la ficha, con el id de la comunidad ya resuelto y visible públicamente:
model.addAttribute("huertoVirtual", huertoVirtualServicio.paraComunidad(comunidad.getId()).orElse(null));
```
```html
<th:block th:if="${huertoVirtual != null}">
    <section th:replace="~{fragmentos/huerto-virtual :: parcela(${huertoVirtual}, true)}"></section>
</th:block>
```

**Panel del participante** (`participante/panel.html`, `/participante`):

```java
// comunidadId = comunidad del integrante activo autenticado (la que ya usa el panel), nunca un parámetro de la URL.
model.addAttribute("huertoVirtual", huertoVirtualServicio.paraComunidad(comunidadId).orElse(null));
```
```html
<th:block th:if="${huertoVirtual != null}">
    <section th:replace="~{fragmentos/huerto-virtual :: parcela(${huertoVirtual}, true)}"></section>
</th:block>
```

Si esa pantalla ya muestra la barra de progreso por su cuenta, usar `false` como segundo parámetro para no duplicarla.
El CSS ya está enlazado en `fragmentos/base.html`.

## Pendiente al integrar Camila_Paredes

En `origin/Camila_Paredes`, `Planta` pasa a tener `especie` (`@ManyToOne`, `especie_id`) y `getNombreComun()` devuelve
el nombre de la especie. `PlantaVirtualLectura` hoy proyecta y ordena por la columna `planta.nombre_comun`, que puede
quedar desincronizada si se renombra la especie. Al integrar esa rama:

- Cambiar la `@Query` para proyectar y ordenar por el nombre de la especie, con `join p.especie e`
  (`select new ...PlantaPlantadaVista(p.id, e.nombreComun) ... order by e.nombreComun asc, p.id asc`).
- Mantener el filtro por huerto y `p.plantada = true and p.activa = true`, el desempate por `p.id` y el límite de 2
  aplicado **después** de filtrar (en `HuertoVirtualServicio`).
- Agregar una prueba que renombre una especie y compruebe que cambian la etiqueta, el orden, la selección de las dos
  visibles y el sprite (por ejemplo, renombrar «Ruda» a «Manzanilla» y ver que pasa al sprite de manzanilla y al
  primer lugar).
- Revisar además que la columna `plantada` no quede duplicada: Camila la agrega en su V4 y aquí ya existe por V11.

## Limitaciones conocidas

- **No se distingue «no registrado» de «elegido».** `huerto.tipo_suelo` y `huerto.diseno` son `NOT NULL` con
  `DEFAULT` (`'LIMOSO'` y `'TIERRA_SURCOS'`, V8/V12), y el constructor corto de `Huerto` usa esos mismos valores. Un
  huerto cuyo gestor nunca eligió suelo ni diseño se dibuja como «Suelo limoso · En tierra · surcos», sin aviso. La
  apariencia neutra y el aviso «Falta registrar el tipo de suelo» solo aparecen con valores vacíos o desconocidos, que hoy
  los `CHECK` impiden guardar (se cubren con la prueba unitaria `HuertoVirtualTest`). Distinguirlo exigiría permitir
  `NULL` sin `DEFAULT` (migración y cambios en el formulario), fuera del alcance de esta vista.
- Las etiquetas de suelos y diseños de `HuertoVirtual` replican las de `GestorHuertoController` (allí son privadas); si
  cambian los códigos o etiquetas, hay que actualizar ambos lugares.
- «Plantada» no tiene límite al guardar; el tope de 9 es solo del dibujo («Mostrando 9 de N»).
- Hay ilustración propia para 31 especies (29 del catálogo, más hierba luisa y ruda); el resto usa la genérica.

## Cómo probarlo a mano

1. Entra como gestor → **Plantas** → «Marcar plantada» en una o dos → vuelve a «Mi comunidad».
2. **Editar datos del huerto** → prueba los 6 suelos y diseños de cada grupo (tierra, vertical, colgante, piso).
3. Desactiva una planta plantada → desaparece del dibujo; «Quitar de vista virtual» → también.
4. Progreso: registra una actividad (no sube), recházala (no sube), apruébala (sube).
5. Celular: reduce el ancho del navegador; con «reducir movimiento» del sistema se detienen las animaciones.
