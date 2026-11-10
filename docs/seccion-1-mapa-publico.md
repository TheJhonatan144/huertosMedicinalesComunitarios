# Sección 1 · Inicio, mapa y ficha pública

Responsable: Ariel Baldeón. Rama: `Ariel_Baldeon`. Base: `Develop` (`592f235`).

## Alcance implementado

La sección 1 del documento de distribución del 7 de noviembre abarca las tres pantallas públicas: inicio, mapa y ficha de comunidad. Esta implementación mantiene Bootstrap, la paleta y las variables del proyecto; incorpora una ilustración SVG original y reglas aisladas en `publico.css`.

- Inicio con presentación del proyecto, acceso al mapa, explicación del recorrido y resumen del ranking existente.
- Explorador con búsqueda por comunidad, huerto, cantón o provincia, sin distinguir mayúsculas ni acentos. Filtros combinables por provincia y etapa; contador, reinicio y estado sin coincidencias.
- Leaflet 1.9.4 local con mosaicos de OpenStreetMap, atribución visible, controles de zoom, escala, marcadores propios, ajuste de vista y ventanas con enlaces a la comunidad correcta.
- Los filtros actualizan tarjetas y marcadores a la vez y se conservan en la URL. La lista renderizada por Thymeleaf es la fuente de datos del mapa: no se descargan datos de otra comunidad ni se duplica una llamada a la API.
- Ficha con información pública, resumen de puntos, ubicación, espacios y plantas. Inserta el fragmento `huerto-virtual :: parcela` del módulo de huertos, consumiendo `HuertoVirtualServicio.paraComunidad` sin modificar su implementación.
- Las fichas históricas existentes se conservan y se identifican como tales. El fragmento virtual solo se solicita para comunidades activas y sin eliminación pendiente.
- Con Leaflet no disponible, la lista y los filtros continúan funcionando. Si fallan los mosaicos, se informa al usuario y se conservan los marcadores y las fichas. Sin JavaScript, las tarjetas y sus enlaces se renderizan desde el servidor.
- En móvil, los controles de búsqueda aparecen antes del mapa y las tarjetas después. No se solicita geolocalización ni se requieren claves API.

## Límites y archivos compartidos

No se modifican tablas, migraciones, altas de comunidades, formularios de huerto, catálogo, evidencias ni reglas de recompensas. No se muestran contactos, direcciones personales, identidades de participantes ni fotografías de evidencias.

Único ajuste compartido: una línea en `SeguridadConfig` añade `https://tile.openstreetmap.org` a `img-src`. La política anterior solo permitía subdominios `*.tile.openstreetmap.org`, aunque el mapa ya usaba el dominio raíz. La revisión en navegador confirmó la carga de mosaicos después del cambio. No se amplía la política a otros proveedores ni a todos los orígenes HTTPS. Este ajuste debe señalarse al integrador al preparar el PR; no se ha enviado ningún mensaje al grupo ni creado un PR.

`base.html`, `app.css` y la navegación compartida permanecen sin cambios. Los enlaces existentes a registro, login y ranking conservan sus destinos.

## Validación

- Compilación Java 21 y ejecución de las clases `*Test`: 31 pruebas, sin fallos ni errores.
- Seis pruebas nuevas: renderizado real de las tres plantillas con Spring MVC/Thymeleaf y seguridad HTTP; enlaces a distintas fichas; estados vacíos; nombres escapados; ausencia de datos privados; uso del servicio del huerto virtual solo para comunidades elegibles.
- Revisión de navegador con las plantillas renderizadas por las pruebas y datos sintéticos, usando la misma CSP que la aplicación. Comprobación de búsqueda, filtros combinados, cero resultados, limpieza, marcador, enlace a ficha, retorno con búsqueda y mosaicos reales.
- Revisión visual de inicio, mapa y ficha en escritorio y viewport móvil de 390 px; comprobación de ausencia de desbordamiento horizontal.

Las pruebas de integración con PostgreSQL y el recorrido completo autenticado no se ejecutaron: Docker no está disponible en este equipo. La vista previa es de las plantillas reales con datos de prueba; no sustituye una aplicación conectada a la base. Antes del PR, ejecutar `mvnw test` con Docker y revisar las pantallas con datos de la base del equipo.

## Ejecución

La aplicación conserva el procedimiento de arranque del README. Una vez iniciada, las rutas son `/`, `/mapa` y `/comunidades/{id}`.

Para exportar localmente las páginas de ejemplo a `target/publico-preview` durante las pruebas:

```powershell
.\mvnw.cmd '-Dtest=*Test' '-Dpublico.preview=target/publico-preview' test
```

La exportación solo se activa con esa propiedad; no agrega datos de demostración a una base ni expone rutas nuevas en producción.

Referencias de integración: [Leaflet 1.9.4](https://leafletjs.com/reference-1.9.4.html) y [política de mosaicos de OpenStreetMap](https://operations.osmfoundation.org/policies/tiles/). No se implementan descargas masivas, precarga offline ni seguimiento de ubicación.
