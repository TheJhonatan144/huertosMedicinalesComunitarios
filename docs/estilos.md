# Guía de estilos de la aplicación

La hoja común está en `src/main/resources/static/css/app.css` y se carga desde `templates/fragmentos/base.html` después de Bootstrap. Las pantallas deben reutilizar sus variables y componentes antes de crear reglas propias.

## Identidad visual

| Uso | Variable | Color |
|---|---|---|
| Verde principal | `--huerto` | `#285B45` |
| Verde oscuro | `--huerto-oscuro` | `#1D4435` |
| Verde profundo | `--huerto-profundo` | `#15372B` |
| Fondo suave | `--fondo` | `#F5F7F3` |
| Superficie | `--superficie` | `#FFFFFF` |
| Tierra/acento | `--tierra` | `#A75D36` |
| Texto | `--texto` | `#25332B` |
| Texto secundario | `--texto-suave` | `#647169` |
| Bordes | `--borde` | `#DCE4DC` |

La tipografía es la pila del sistema (`system-ui` y fallbacks locales); no descarga fuentes externas. Los encabezados usan peso fuerte y el texto mantiene contraste sobre fondos claros.

## Componentes comunes

- Navegación, contenido y pie: `.app-navbar`, `.app-main`, `.app-footer`.
- Acción principal: `.btn.btn-huerto`. Acciones secundarias: `.btn-outline-success` o `.btn-outline-huerto`.
- Paneles: `.card`, `.card-header`, `.tarjeta-huerto`; portada destacada: `.heroe`.
- Formularios: `.form-label`, `.form-control`, `.form-select` y controles Bootstrap con foco verde.
- Tablas, alertas, insignias y progreso: usar las clases Bootstrap existentes; `app.css` unifica sus colores y bordes.
- Evidencias y mapa: `.foto-evidencia`, `.miniatura`, `#mapa`.

## Opciones visuales con imagen

Para opciones seleccionables de huerto, suelo, cerramiento o planta, usar una tarjeta asociada al control del formulario. La clase de selección aplica un borde verde y un anillo de foco:

```html
<label class="opcion-visual">
    <input type="radio" name="tipoSuelo" value="ARCILLOSO">
    <span class="opcion-visual__contenido">
        <img class="opcion-visual__imagen" src="/images/suelo-arcilloso.jpg" alt="">
        <span class="opcion-visual__texto">
            <strong>Arcilloso</strong>
            <span class="d-block text-muted">Descripción breve</span>
        </span>
    </span>
</label>
```

Cada integrante debe aplicar estos estilos a las pantallas de su módulo. Si necesita un componente nuevo, debe agregarlo a `app.css` para que se reutilice, en lugar de duplicar reglas en una plantilla. Mantener el diseño responsive y respetar `prefers-reduced-motion`.
