# Mapa de ubicaciones de pruebas

Rutas Java relativas a `src/test/java`. Se conservan los casos existentes; el formulario conjunto se divide por módulo.

| Antes | Ahora |
|---|---|
| `ec/edu/epn/huertos/ArquitecturaTest.java` | `ec/edu/epn/huertos/arquitectura/ArquitecturaTest.java` |
| `ec/edu/epn/huertos/FabricaDatos.java` | `ec/edu/epn/huertos/soporte/datos/FabricaDatos.java` |
| `ec/edu/epn/huertos/PruebaCatalogoAislado.java` | `ec/edu/epn/huertos/soporte/integracion/PruebaCatalogoAislado.java` |
| `ec/edu/epn/huertos/PruebaIntegracion.java` | `ec/edu/epn/huertos/soporte/integracion/PruebaIntegracion.java` |
| `ec/edu/epn/huertos/RecorridoPrincipalIT.java` | `ec/edu/epn/huertos/recorridos/RecorridoPrincipalIT.java` |
| `ec/edu/epn/huertos/SeguridadWebIT.java` | `ec/edu/epn/huertos/compartido/presentacion/SeguridadWebIT.java` |
| `ec/edu/epn/huertos/actividades/ActividadServicioIT.java` | `ec/edu/epn/huertos/actividades/aplicacion/ActividadServicioIT.java` |
| `ec/edu/epn/huertos/arquitectura/OrganizacionPruebasTest.java` | `ec/edu/epn/huertos/arquitectura/OrganizacionPruebasTest.java` |
| `ec/edu/epn/huertos/comunidades/CupoComunidadIT.java` | `ec/edu/epn/huertos/comunidades/aplicacion/CupoComunidadIT.java` |
| `ec/edu/epn/huertos/comunidades/SolicitudesComunidadIT.java` | `ec/edu/epn/huertos/comunidades/aplicacion/SolicitudesComunidadIT.java` |
| `ec/edu/epn/huertos/evidencias/AlmacenamientoLocalTest.java` | `ec/edu/epn/huertos/evidencias/persistencia/AlmacenamientoLocalTest.java` |
| `ec/edu/epn/huertos/evidencias/RevisionServicioIT.java` | `ec/edu/epn/huertos/evidencias/aplicacion/RevisionServicioIT.java` |
| `ec/edu/epn/huertos/evidencias/aplicacion/ValidadorImagenTest.java` | `ec/edu/epn/huertos/evidencias/aplicacion/ValidadorImagenTest.java` |
| `ec/edu/epn/huertos/gamificacion/GamificacionReglasTest.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/GamificacionReglasTest.java` |
| `ec/edu/epn/huertos/gamificacion/PosicionRankingTest.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/PosicionRankingTest.java` |
| `ec/edu/epn/huertos/huertos/CatalogoBloqueDosIT.java` | `ec/edu/epn/huertos/huertos/persistencia/CatalogoBloqueDosIT.java` |
| `ec/edu/epn/huertos/huertos/CatalogoBloqueTresIT.java` | `ec/edu/epn/huertos/huertos/persistencia/CatalogoBloqueTresIT.java` |
| `ec/edu/epn/huertos/huertos/CatalogoBodegaIT.java` | `ec/edu/epn/huertos/huertos/aplicacion/CatalogoBodegaIT.java` |
| `ec/edu/epn/huertos/huertos/CatalogoCargaInicialIT.java` | `ec/edu/epn/huertos/huertos/persistencia/CatalogoCargaInicialIT.java` |
| `ec/edu/epn/huertos/huertos/CatalogoInicialIT.java` | `ec/edu/epn/huertos/huertos/persistencia/CatalogoInicialIT.java` |
| `ec/edu/epn/huertos/huertos/FormularioHuertoTarjetasIT.java` | `ec/edu/epn/huertos/huertos/presentacion/FormularioHuertoTarjetasIT.java` |
| `ec/edu/epn/huertos/huertos/IlustracionesPlantasTest.java` | `ec/edu/epn/huertos/huertos/presentacion/IlustracionesPlantasTest.java` |
| `ec/edu/epn/huertos/huertos/PlantaVirtualLecturaIT.java` | `ec/edu/epn/huertos/huertos/persistencia/PlantaVirtualLecturaIT.java` |
| `ec/edu/epn/huertos/huertos/PlantasIT.java` | `ec/edu/epn/huertos/huertos/aplicacion/PlantasIT.java` |
| `ec/edu/epn/huertos/huertos/SiembraYBodegaComunidadIT.java` | `ec/edu/epn/huertos/huertos/aplicacion/SiembraYBodegaComunidadIT.java` |
| `ec/edu/epn/huertos/huertos/aplicacion/HuertoVirtualIT.java` | `ec/edu/epn/huertos/huertos/aplicacion/HuertoVirtualIT.java` |
| `ec/edu/epn/huertos/huertos/aplicacion/HuertoVirtualTest.java` | `ec/edu/epn/huertos/huertos/aplicacion/HuertoVirtualTest.java` |
| `ec/edu/epn/huertos/huertos/aplicacion/SpritePlantaTest.java` | `ec/edu/epn/huertos/huertos/aplicacion/SpritePlantaTest.java` |
| `ec/edu/epn/huertos/misiones/MisionHuertoTest.java` | `ec/edu/epn/huertos/misiones/dominio/MisionHuertoTest.java` |
| `ec/edu/epn/huertos/publico/PublicoPaginasTest.java` | `ec/edu/epn/huertos/publico/presentacion/PublicoPaginasTest.java` |
| `ec/edu/epn/huertos/publico/PublicoServicioTest.java` | `ec/edu/epn/huertos/publico/aplicacion/PublicoServicioTest.java` |
| `ec/edu/epn/huertos/ValidacionFormulariosTest.java` | `autenticacion/aplicacion/FormularioRegistroCuentaTest.java` y `huertos/aplicacion/FormularioHuertoTest.java`, bajo el mismo paquete raíz |

## Soporte nuevo o extraído

- `src/test/java/ec/edu/epn/huertos/soporte/concurrencia/EjecucionConcurrente.java`
- `src/test/java/ec/edu/epn/huertos/soporte/concurrencia/EjecucionConcurrenteTest.java`
- `src/test/java/ec/edu/epn/huertos/soporte/datos/FabricaDatos.java`
- `src/test/java/ec/edu/epn/huertos/soporte/datos/FormulariosPrueba.java`
- `src/test/java/ec/edu/epn/huertos/soporte/datos/ImagenesPrueba.java`
- `src/test/java/ec/edu/epn/huertos/soporte/integracion/AislamientoIntegracion.java`
- `src/test/java/ec/edu/epn/huertos/soporte/integracion/AislamientoIntegracionIT.java`
- `src/test/java/ec/edu/epn/huertos/soporte/integracion/EntornoIntegracion.java`
- `src/test/java/ec/edu/epn/huertos/soporte/integracion/PruebaCatalogoAislado.java`
- `src/test/java/ec/edu/epn/huertos/soporte/integracion/PruebaIntegracion.java`
- `src/test/java/ec/edu/epn/huertos/soporte/validacion/PruebaValidacion.java`
