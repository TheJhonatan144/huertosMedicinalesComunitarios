# Alcance del primer prototipo

> **Estado del alcance:** selección **operativa provisional** definida en el encargo de implementación.
> **No** es una aprobación documental del equipo. Los documentos de referencia no estaban en el repositorio (ver [pendientes](pendientes.md)).

## Propósito

Aplicación web educativa y gamificada para apoyar la gestión de huertos medicinales comunitarios del Ecuador y la difusión de saberes
ancestrales documentados. Recorrido: mapa público → comunidad y huerto → actividad y participantes → fotografía → revisión humana →
aprobación/rechazo → puntos y progreso → ranking e historial. El riego es manual.

## Incluido (13 elementos del alcance operativo)

| # | Elemento | Estado |
|---|---|---|
| 1 | Inicio y cierre de sesión | Implementado y verificado |
| 2 | Permisos por rol y aislamiento entre comunidades | Implementado y verificado |
| 3 | Comunidades, integrantes activos (cupo por tipo: persona 1, familia 10, condominio 20, barrio 25) y huertos | Implementado; alta autenticada por administrador/gestor |
| 4 | Consulta pública del mapa, comunidad y huerto | Implementado; mapa con Leaflet **no probado en navegador** |
| 5 | Gestión básica de plantas | Implementado y verificado por HTTP |
| 6 | Actividades y participantes | Implementado y verificado |
| 7 | Evidencia fotográfica persistente | Implementado y verificado |
| 8 | Bandeja de revisión | Implementado y verificado |
| 9 | Aprobación, rechazo con observaciones y reenvío | Implementado y verificado |
| 10 | Puntos individuales y comunitarios separados | Implementado y verificado |
| 11 | Progreso visual sencillo del huerto virtual | Implementado (barra y etapas); apariencia no revisada en navegador |
| 12 | Ranking comunitario | Implementado y verificado |
| 13 | Historial de envíos, revisiones y recompensas | Implementado y verificado |

## Fuera del prototipo (se conserva en el [backlog](backlog.md); no está descartado)

Foro y moderación, bodega, calendario agrícola precargado, insignias, misiones, ranking individual, rachas y notificaciones internas.
RF24 está **pendiente de revisión** y RF25 **reservado**: no se les atribuye funcionalidad.

## Explícitamente no implementado

Registro público, recuperación de contraseña por correo, integraciones institucionales (MSP u otras), sensores/bombas/válvulas/Arduino/ESP32,
servicios de IA, microservicios. El rol «Administrador del Ministerio de Salud» representa una cuenta institucional provisionada
por un proceso autorizado; el prototipo no afirma integración real con el MSP.

## Actores

Visitante (sin cuenta) · Gestor de huerto · Participante · Administrador del Ministerio de Salud. Cada cuenta comunitaria tiene un solo rol activo;
la cuenta del Ministerio es institucional y no pertenece a comunidades ni participa en el ranking.
Correspondencia con constantes técnicas en la [arquitectura actual](../arquitectura.md#roles).

## Limitaciones conocidas del prototipo

- Un huerto por comunidad (restricción del modelo del prototipo).
- Una cuenta puede tener una sola pertenencia comunitaria activa; el historial de pertenencias inactivas se conserva.
- La cuenta personal puede registrarse sin rol comunitario. La comunidad se crea desde una cuenta sin pertenencia o se acepta por invitación; las cuentas del Ministerio no se registran públicamente.
- Tras una baja, las actividades ya registradas conservan su lista fija de participantes (la baja no los retira ni les quita la recompensa).
- Valores de recompensa, umbrales y límites de fotografía son **de demostración/provisionales**.
