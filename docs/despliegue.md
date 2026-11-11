# Despliegue de ALLPANA en Render

Guía para publicar la aplicación en internet por primera vez. No hace falta experiencia previa.

## ¿Por qué todo en Render y no el frontend en Vercel?

ALLPANA es una aplicación **Spring Boot con vistas Thymeleaf**: el servidor Java arma cada página con los datos de la
base, la sesión y la seguridad (inicio de sesión, permisos por rol, formularios protegidos). La carpeta `frontend/`
contiene esas vistas y sus estilos, pero **no es una aplicación independiente**: sin el servidor no puede mostrar nada.

- **Vercel** sirve sitios estáticos o con Node/funciones cortas; no ejecuta un servidor Java permanente.
- **Render** sí ejecuta contenedores Docker y ofrece PostgreSQL, así que despliega la aplicación completa tal como es.

Por eso **no se necesitan ramas separadas** de frontend y backend: se despliega una sola aplicación desde una rama
(normalmente `main`). Separar en Vercel exigiría reescribir el frontend como aplicación JavaScript (React, Vue…)
que consuma una API JSON nueva; es un proyecto aparte, no un cambio de estructura.

## Qué hay preparado en el repositorio

| Archivo | Para qué sirve |
|---|---|
| `render.yaml` | "Blueprint": Render crea con él la aplicación web y la base PostgreSQL, ya conectadas. |
| `Dockerfile` | Compila el proyecto con Maven y lo ejecuta con Java 21 (Render lo usa automáticamente). |
| `src/main/resources/application.yml` | Lee el puerto (`PORT`) y la conexión a la base (`DB_HOST`, `DB_PORT`, `DB_NOMBRE`, `DB_USUARIO`, `DB_CLAVE`) que entrega Render. |

En local nada cambia: `docker compose up -d --build` sigue funcionando igual.

## Pasos (lo hace la persona dueña del repositorio en GitHub)

1. **Tener los cambios en la rama que se va a desplegar.** Render despliega por defecto la rama principal (`main`).
   Fusiona antes en `main` las ramas del equipo que deban salir publicadas (pull requests en GitHub).
2. **Crear cuenta en Render:** entra a <https://render.com>, pulsa *Get Started* y regístrate con **GitHub**.
   Autoriza a Render a ver el repositorio `huertosMedicinalesComunitarios`.
3. **Crear el Blueprint:** en el panel de Render, *New* → **Blueprint**. Elige el repositorio y la rama `main`.
   Render detecta `render.yaml` y muestra lo que va a crear: el servicio web `allpana` y la base `allpana-db`.
4. **Completar las variables que pide** (no se guardan en GitHub):
   - `DEMO_CLAVE`: contraseña que tendrán las cuentas de demostración (`gestor_a`, `revisor_a`, `participante_a1`…).
     Usa una propia; la del README es solo para uso local.
   - `ADMIN_TECNICO_USUARIO`, `ADMIN_TECNICO_NOMBRE`, `ADMIN_TECNICO_CLAVE`: cuenta de administración técnica inicial.
5. Pulsa **Apply / Deploy Blueprint**. La primera vez tarda varios minutos (descarga dependencias y compila).
   Puedes seguir el progreso en *Logs* del servicio `allpana`; termina cuando aparece `Started HuertosApplication`.
6. **Abrir la aplicación:** arriba del servicio aparece la dirección pública, del estilo
   `https://allpana-xxxx.onrender.com`. Compártela con el equipo.

Desde entonces, cada vez que se hace *push* (o se fusiona un pull request) en `main`, Render vuelve a desplegar solo.

## Limitaciones del plan gratuito (importante para la demostración)

- **Se duerme:** tras unos minutos sin visitas el servicio se apaga; la siguiente visita tarda ~1 minuto en cargar.
  Ábrelo un rato antes de presentar.
- **Fotos y videos de evidencias no persisten:** el disco del plan gratuito se borra al reiniciar o redesplegar.
  Los datos de la base (comunidades, actividades, puntos) sí se conservan. Para guardar archivos hay que pasar a un
  plan de pago y añadir un disco al servicio (*Disks* → montar en `/datos/evidencias`).
- **La base gratuita vence** después de un tiempo (Render muestra la fecha). Revisa las condiciones vigentes en
  <https://render.com/pricing>.
- **Memoria de 512 MB:** `render.yaml` ya ajusta Java (`JAVA_TOOL_OPTIONS`) para que quepa.

## Problemas frecuentes

| Síntoma | Qué revisar |
|---|---|
| El despliegue falla en *Build* | Logs del build; normalmente es un error de compilación que también aparece en local con `.\mvnw.cmd package`. |
| La app arranca y se cae | Logs: si menciona la base de datos, comprueba que `allpana-db` esté *Available*. |
| "Application failed to respond" | Espera a que termine de arrancar (Java tarda) o revisa *Logs*. El chequeo de salud es `/actuator/health`. |
| No hay cuentas de demostración | Solo se cargan con `SPRING_PROFILES_ACTIVE=demo` y si la base estaba vacía. |

## Si más adelante quieren usar Vercel

Opciones realistas, de menor a mayor esfuerzo:

1. **Dominio propio apuntando a Render** (sin Vercel): lo más simple.
2. **Vercel solo como "puerta de entrada"** con una reescritura (`rewrites`) hacia la URL de Render: no aporta
   rendimiento y añade una pieza más; solo tiene sentido si ya usan Vercel para otra cosa.
3. **Frontend nuevo en Vercel** (React/Next.js) consumiendo una API REST del backend en Render: requiere crear esa API
   y rehacer todas las pantallas. Es un proyecto de varias semanas.
