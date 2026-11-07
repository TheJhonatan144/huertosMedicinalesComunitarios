# Huerto virtual 2D cenital

Vista ilustrada del huerto (estilo juego sencillo, vista desde arriba) integrada con datos reales. Aparece en: panel del gestor (`/gestor`),
panel del participante (`/participante`) y ficha pública de la comunidad (`/comunidades/{id}`).

## Datos que usa

| Qué se dibuja | De dónde sale | Regla |
|---|---|---|
| Tipo de suelo (color/textura y etiqueta) | `huerto.tipo_suelo` (**nuevo**, migración V4, opcional) | Nulo → apariencia neutra gris y aviso «Falta registrar: tipo de suelo» |
| Diseño (tierra, vertical, colgante, piso) | `huerto.diseno` (**nuevo**, V4, opcional) | Nulo → composición predeterminada y aviso «diseño del huerto» |
| Plantas | `planta` del huerto | «Plantada» = `activa = true` **y** `cantidad > 0`. Quitar/desactivar la planta o ponerla en 0 la saca del dibujo. No hay bodega en el sistema: las existencias nunca cuentan |
| Ilustración | `SpritePlanta` por nombre común: manzanilla, hierba luisa, menta; el resto usa un sprite **genérico** | Nunca se muestran fotos reales |
| Progreso | `GamificacionServicio.progreso` (suma de recompensas) | Solo actividades aprobadas lo hacen crecer (regla existente, sin cambios) |

Primera versión: se dibujan **hasta 2 plantas** (orden alfabético); si hay más se indica «+N planta(s) más».

## Archivos

- Migración: `db/migration/V4__huerto_suelo_y_diseno.sql`
- Dominio: `TipoSuelo`, `DisenoHuerto`, `Huerto` (+2 campos), `FormularioHuerto`, `HuertoServicio.actualizarHuerto`
- Vista: `HuertoVirtual`, `HuertoVirtualServicio`, `SpritePlanta`
- UI: `templates/fragmentos/huerto-virtual.html` (parcela + sprites SVG), `static/css/huerto-virtual.css`
- Integración: `GestorPanelController`, `ParticipanteServicio`, `PublicoServicio`, plantillas de panel/ficha, formulario `gestor/huerto-form.html`
- Pruebas: `HuertoVirtualIT` (9), `SpritePlantaTest` (2)

## Cómo probarlo

1. Entra como `gestor_a` (demo) → **Editar datos del huerto** → elige tipo de suelo y diseño → Guardar → vuelve a «Mi comunidad».
2. Prueba los 5 suelos (franco, arcilloso, arenoso, limoso, humus) y los 4 diseños; también «Sin registrar» (neutro y aviso).
3. **Plantas** → desactiva o pon cantidad 0 a una → desaparece de la parcela; reactívala → vuelve. Sin plantas → parcela vacía con enlace a registrar.
4. Progreso: registra una actividad (queda en 0), recházala (0) y apruébala (sube 5 con los valores de demostración).
5. Celular: reduce el ancho del navegador o abre desde el móvil; con «reducir movimiento» del sistema operativo se detienen las animaciones.

## Pendientes

Las listas de suelos y diseños y los sprites son una **propuesta técnica** (ver [pendientes](pendientes.md), P-13). Solo hay ilustración propia
para manzanilla, hierba luisa y menta.
