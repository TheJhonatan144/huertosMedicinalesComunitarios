# Pendientes y decisiones abiertas

## Documentación de referencia no disponible

El encargo pidió consultar tres archivos en `docs/referencias/`. **No existen** (la carpeta se creó vacía) y no se encontraron con esos nombres en el equipo:

- `Documento_de_cambios_arquitectura_Huertos_Medicinales.docx`
- `Propuesta_Alcance_V1_Huertos_Medicinales_Comunitarios_actualizada.docx`
- `Requerimientos_Huertos_Medicinales_Comunitarios.xlsx`

En `Descargas` hay otros documentos de nombre parecido (`Propuesta_Arquitectura_y_Herramientas_Huertos_Medicinales.docx`, `Stack y metodología Huertos Medicinales Comunitarios.docx`, `Actividad Análisis de requerimientos e interfaces.docx`) que **no se leyeron ni se usaron** por no ser los indicados.

**Consecuencias:** no se verificó la matriz RF01–RF26 / RNF01–RNF12 / RB01–RB15 ni sus prioridades. La [trazabilidad](trazabilidad.md) usa identificadores temporales
**HU-P01…** y **CA-P01…**; la vinculación con RF/RNF/RB queda **pendiente**. Solo se citan RB05, RB09 (aprobadas), RB06 (no aprobada), RF10 (contradicción), RF24 (en revisión) y RF25 (reservado), tal como los describe el encargo.
**Acción:** copiar los tres archivos a `docs/referencias/` para completar la matriz.

## Decisiones que cambian alcance o reglas (requieren al equipo)

| # | Pendiente | Impacto |
|---|---|---|
| P-01 | **RB06**: valores definitivos de recompensas, incrementos de progreso y umbrales de niveles | Hoy son de demostración (configurables) |
| P-02 | **RF10**: corregir la fuente según la propuesta de [decisiones](decisiones.md) | Contradice RB05/RB09 |
| P-03 | Límites de fotografías (tamaño, formatos, dimensiones) | Provisionales: 5 MiB, JPEG/PNG, 8000 px, 25 MP |
| P-04 | Provisión de usuarios (alta de gestores, participantes y revisores) sin registro público | Hoy: cargador de demostración |
| P-05 | Quién puede ver las fotografías (¿participantes de la actividad? ¿público tras aprobar?) | Hoy: solo gestor y revisor asignado |
| P-06 | ¿Un huerto por comunidad? ¿Un usuario en varias comunidades? | Hoy: 1 y 1 |
| P-07 | Política para bajas: ¿una baja retira al integrante de actividades pendientes? | Hoy: la lista queda fija |
| P-08 | Contenido de plantas (propiedades, usos) y **fuentes validadas** de saberes ancestrales | Hoy solo texto ilustrativo, sin afirmaciones medicinales |
| P-09 | RF24 (en revisión) y RF25 (reservado) | No se les atribuye funcionalidad |
| P-10 | Fecha de entrega (13/11/2026) y cronograma | No confirmada |
| P-11 | Designación real/integración con el MSP para el rol de revisor | Hoy es solo funcional |
| P-12 | Requisitos de rendimiento (p. ej. consultas en 3 s): definir operación, carga y entorno | No se afirma ni se mide |

## Verificaciones que faltan

- Revisión visual en navegador (escritorio y móvil) de todas las pantallas y del mapa Leaflet con mosaicos de OSM: **no realizada** (el entorno de trabajo no abrió un navegador).
- Accesibilidad (WCAG) y rendimiento: **no medidos**.
- Versiones más recientes de PostgreSQL, Bootstrap y Leaflet: **no consultadas** en fuentes oficiales.
- Respaldo y restauración de la base y de las fotografías: no definidos.
- Cobertura de pruebas: **no medida** (no se afirma ningún porcentaje).
- Repositorio Git: la carpeta no es un repositorio; no se inicializó por no haberse pedido.

## Deuda técnica conocida

- Las consultas públicas hacen varias consultas por comunidad (aceptable con pocas comunidades; optimizar si crecen).
- El ranking calcula posiciones en memoria tras una consulta agregada.
- Sin bloqueo de cuentas ni límite de intentos de inicio de sesión (pendiente de requisitos de seguridad).
- Sin rotación/limpieza de archivos huérfanos si el servidor falla entre guardar el archivo y confirmar la transacción más allá de la compensación implementada.
