# Huerto virtual 2D cenital

Vista ilustrada del huerto (estilo juego sencillo, vista desde arriba) que **solo lee** datos reales. Integrada hoy
únicamente en el **panel del gestor** (`/gestor`), donde reemplaza el bloque simple «Representación del huerto».

## Datos que usa

| Qué se dibuja | De dónde sale | Regla |
|---|---|---|
| Tipo de suelo (color, textura y etiqueta) | `huerto.tipo_suelo` (V8) | Variante propia para ARCILLOSO, LIMOSO, ARENOSO, MARGOSO, GREDOSO y PANTANOSO. Vacío o desconocido → apariencia neutra gris y aviso «Falta registrar el tipo de suelo» |
| Composición | `huerto.diseno` (13 códigos de V12) | Se agrupa por prefijo: `TIERRA_*` (hierba + marco de troncos), `VERTICAL_*` (panel con bolsillos), `COLGANTE_*` (viga y macetas que se mecen), `PISO_*` (macetas sobre baldosas). Código que no sea uno de los 13 → composición neutra. La etiqueta muestra el código concreto («En piso · gradas») |
| Plantas | `planta` del huerto | Elegible = `plantada = true` **y** `activa = true`. **No** usa `cantidad` ni bodega. Orden: nombre común ascendente (empate por id). Se dibujan hasta **2** después de filtrar; si hay más: «Mostrando 2 de N» |
| Ilustración | `SpritePlanta` por nombre común: manzanilla, hierba luisa, menta; el resto usa un sprite **genérico** | Nunca se muestran fotos reales |
| Progreso | `GamificacionServicio.progreso(comunidadId)` | Solo se lee. La vista no otorga puntos ni recalcula; solo las actividades aprobadas lo hacen crecer (regla existente) |

Accesibilidad: el dibujo es `aria-hidden` y va acompañado de un párrafo `visually-hidden` con la descripción textual
(suelo, diseño y plantas). Las animaciones (balanceo de hojas, flores, macetas colgantes) se detienen con
`prefers-reduced-motion: reduce`. Sin plantas se muestra «Tu parcela está lista y esperando plantas» y, al gestor, un
enlace a `/gestor/plantas` para marcarlas como plantadas.

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
- Sin migraciones nuevas. `PlantaRepository`, `Huerto` y `HuertoServicio` no se modificaron.
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
- `HuertoServicio.cambiarPlantada` limita a 2 las plantas `plantada = true` (contando inactivas), así que «Mostrando 2
  de N» solo aparece con datos cargados por fuera del servicio. La vista limita igualmente.
- Solo hay ilustración propia para manzanilla, hierba luisa y menta; el resto usa la genérica.

## Cómo probarlo a mano

1. Entra como gestor → **Plantas** → «Marcar plantada» en una o dos → vuelve a «Mi comunidad».
2. **Editar datos del huerto** → prueba los 6 suelos y diseños de cada grupo (tierra, vertical, colgante, piso).
3. Desactiva una planta plantada → desaparece del dibujo; «Quitar de vista virtual» → también.
4. Progreso: registra una actividad (no sube), recházala (no sube), apruébala (sube).
5. Celular: reduce el ancho del navegador; con «reducir movimiento» del sistema se detienen las animaciones.
