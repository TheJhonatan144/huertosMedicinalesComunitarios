# Implementación inicial del sistema de misiones

Esta primera entrega integra la **ruta única de inicio** con el flujo existente de actividades y evidencias.

## Qué incluye

- Plantillas de misión y dependencias en Flyway (`V2026111101__misiones_por_huerto.sql`).
- Instancias de misiones por huerto, incluyendo los huertos que ya existían al aplicar la migración.
- Estados `BLOQUEADA`, `DISPONIBLE`, `EN_REVISION` y `COMPLETADA`.
- Desbloqueo por dependencias; las misiones de abono y cerramiento se pueden completar en paralelo.
- Registro de una actividad normal vinculada a una misión y evidencia por el flujo existente.
- Rechazo que permite corregir y reenviar la misma actividad, conservando su historial.
- Puntos individuales, comunitarios y progreso definidos por plantilla; las actividades libres mantienen la configuración actual.
- Vista inicial `/gestor/misiones`.

## Pendiente para la siguiente fase

- Misiones recurrentes diarias, semanales y mensuales con ventanas de tiempo y enfriamiento.
- Racha de riego, bonos de ruta y recompensas por cambio de etapa.
- Participación compartida en una misión diaria por huerto sin duplicar recompensas.
- Pruebas de integración con PostgreSQL para migración, desbloqueo, aprobación, rechazo y reenvío.
- Validación visual de las plantillas Thymeleaf en ejecución.

## Verificación local

1. Haz una copia de seguridad de tu base de datos.
2. Ejecuta el proyecto con PostgreSQL.
3. Flyway aplicará la migración nueva automáticamente.
4. Registra un huerto nuevo o entra con un huerto existente.
5. Abre **Actividades → Ver misiones**.
6. Completa la primera misión, envía la evidencia y apruébala desde el usuario revisor.
7. Comprueba que se desbloquee la siguiente misión y que los puntos correspondan a la plantilla.

La ejecución de Maven no se pudo completar en el entorno de preparación porque el wrapper necesita descargar Maven desde Maven Central y no había conectividad disponible. Ejecuta `mvnw.cmd test` en Windows para verificar compilación y pruebas en tu equipo.
