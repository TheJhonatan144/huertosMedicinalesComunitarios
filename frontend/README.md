# Frontend de ALLPANA - Proyecto Calidad de Software GR05 2026-B

Todo lo que se ve en el navegador vive aquí: las vistas Thymeleaf y los archivos estáticos.
El backend (Java, configuración y migraciones) sigue en `src/main/`.

```
frontend/
  templates/          vistas Thymeleaf (HTML que el servidor completa con datos)
    fragmentos/       piezas compartidas: base.html (barra y pie), componentes, huerto virtual…
    publico/          portada, mapa, ranking y fichas públicas
    auth/ cuenta/ gestor/ participante/ revision/ admin-tecnico/ error/
  static/             archivos que se sirven tal cual
    css/              estilos (app.css global; portada.css, publico.css… por pantalla)
    js/               comportamiento en el navegador (app.js global; portada.js, mapa.js…)
    img/              imágenes (marca/ con el logo ALLPANA, portada/ con la foto de la portada…)
    vendor/           librerías locales (Bootstrap, Leaflet)
    favicon.ico
```

## Cómo se conecta con Spring Boot

No es una aplicación aparte ni necesita Node. El `pom.xml` empaqueta `frontend/templates` como `templates/`
y `frontend/static` como `static/` dentro del JAR, que son las rutas que Spring Boot ya usa. Por eso:

- Los controladores siguen devolviendo nombres de vista como `"publico/inicio"`.
- En las plantillas, las rutas siguen siendo `@{/css/app.css}`, `@{/img/marca/allpana-emblema.png}`, etc.
- Por compatibilidad, una plantilla que alguien agregue todavía en `src/main/resources/templates` también funciona,
  pero lo nuevo debe ir aquí.

## Ver los cambios

```powershell
docker compose up -d --build     # reconstruye la imagen con el frontend actual
```

Reglas que impone la política de seguridad (CSP) de la aplicación:

- Solo scripts propios en archivos `.js` (no se permiten `<script>` en línea ni librerías desde internet).
- Imágenes y fuentes locales: copia aquí cualquier recurso en vez de enlazarlo desde otro sitio.
