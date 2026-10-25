# Guion de demostración

Requisitos: aplicación en ejecución con perfil `demo` (ver [README](../README.md)). Contraseña de todas las cuentas: `Demo-2026!`.
Usa ventanas de incógnito o sesiones separadas para cada rol. Sustituye `8080` por tu `APP_PUERTO`.

## 0. Preparación (una vez)

```powershell
Copy-Item .env.example .env
docker compose up -d --build        # http://localhost:8080
```

## 1. Visitante (sin cuenta)

1. Abre `/mapa`: mapa centrado en Ecuador con atribución de OpenStreetMap, dos marcadores y, debajo, la **lista alternativa** de huertos.
2. Entra a «Comunidad Demo Sumak Allpa»: ficha pública, huerto, progreso (Semilla, 0), puntos comunitarios (0) y plantas (texto ilustrativo).
3. `/ranking`: dos comunidades con 0 puntos.
4. Intenta abrir `/gestor` → te lleva al inicio de sesión (el visitante no modifica nada).

## 2. Gestor: registrar actividad con participantes y fotografía

1. Inicia sesión con **`gestor_a`**. Panel: integrantes 4/25 (gestor + 3 activos; `participante_a4` está de baja), progreso 0.
2. **Registrar actividad** → tipo «Riego manual», descripción, fecha de hoy.
3. Observa que solo aparecen los **integrantes activos** de su comunidad (no `participante_a4`, ni nadie de la comunidad B). Marca 2 o 3.
4. Sube una fotografía JPEG o PNG. *Prueba negativa:* renombra un `.txt` a `.jpg` y súbelo → «El archivo no es una fotografía JPEG o PNG válida».
5. Resultado: la actividad queda **Pendiente**; en el detalle: «todavía no genera puntos ni progreso». El panel sigue en 0.
6. *(Opcional)* **Editar actividad** mientras está pendiente: cambia la descripción; los participantes no se pueden modificar.

## 3. Revisor: rechazo con observaciones

1. Cierra sesión e inicia con **`revisor_a`** → Bandeja de revisión: aparece el envío.
2. Abre la evidencia (foto visible solo para este revisor y el gestor).
3. Pulsa **Rechazar con observaciones** con el campo vacío → mensaje: las observaciones son obligatorias.
4. Escribe «La foto no muestra el huerto» y rechaza. Puntos y progreso siguen en 0.

## 4. Gestor: corregir y reenviar (historial conservado)

1. Con `gestor_a`, abre la actividad (**Rechazada**): se ve el envío 1 con las observaciones.
2. En «Corregir y reenviar» sube otra foto, agrega un comentario y reenvía. La actividad vuelve a **Pendiente** y el historial muestra **Envío 1 (rechazado)** y **Envío 2 (pendiente)**.

## 5. Revisor: aprobación (una sola vez)

1. Con `revisor_a` aprueba el envío 2 → mensaje «Se registraron los puntos y el progreso».
2. *(Prueba de duplicación)* Pulsa «Atrás» y vuelve a enviar la decisión, o abre la misma URL en dos pestañas: la segunda recibe «Este envío ya fue revisado»; nada se duplica.
3. «Mis revisiones» muestra el rechazo y la aprobación.

## 6. Resultados

- `participante_a1` (si fue seleccionado): **10 puntos individuales, nivel Aprendiz**; puntos comunitarios 20, progreso del huerto 5/100.
- Un participante **no seleccionado**: 0 puntos individuales (los comunitarios sí se ven).
- `/ranking`: Sumak Allpa **20** puntos, Pachamama Wasi 0. El ranking usa solo puntos comunitarios.
- Ficha pública de la comunidad: 20 puntos, progreso 5/100 (etapa «Semilla»; «Brote» desde 10).
- Valores **de demostración** (RB06 no aprobada): 10 / 20 / 5 por actividad aprobada.

## 7. Permisos y aislamiento (denegaciones esperadas)

| Acción | Cuenta | Resultado |
|---|---|---|
| Abrir la actividad de A | `gestor_b` | 403 |
| Ver la foto de A (`/evidencias/envios/1/foto`) | `revisor_b`, `gestor_b`, participantes | 403 |
| Abrir la bandeja | `gestor_a`, `participante_a1` | 403 |
| Abrir `/gestor` | `participante_a1`, `revisor_a` | 403 |
| Enviar un formulario sin token CSRF | cualquiera | 403 |

## 8. Cupo de 25 integrantes y bajas

`gestor_a` → Integrantes: da de baja a `participante_a1` (libera cupo) y reactívalo. La regla de 25 se demuestra con las pruebas
automáticas (`CupoComunidadIT`), porque la demo solo trae 4 integrantes.

## 9. Persistencia

`docker compose restart app` (o `docker compose down` **sin** `-v` y luego `up`): inicia sesión de nuevo; los puntos, el ranking, el historial
y las fotografías siguen ahí. Para empezar desde cero, ver «Reiniciar los datos» en el [README](../README.md) (acción manual y destructiva).
