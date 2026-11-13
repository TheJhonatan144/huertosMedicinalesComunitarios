# Inventario técnico de Develop `9a7bd05`

Inventario estructural generado desde la instantánea del commit. La clasificación es heurística; no equivale a una prueba de comportamiento.

## Código de producción

| Archivo | Tipo | Líneas | Dependencias entre módulos |
|---|---|---:|---|
| `src/main/java/db/catalogo/V2026110901__carga_inicial_catalogo.java` | modelo / formulario / utilidad | 72 | — |
| `src/main/java/ec/edu/epn/huertos/actividades/Actividad.java` | entidad | 151 | compartido, evidencias |
| `src/main/java/ec/edu/epn/huertos/actividades/ActividadParticipante.java` | entidad | 48 | — |
| `src/main/java/ec/edu/epn/huertos/actividades/ActividadParticipanteRepository.java` | repositorio | 26 | — |
| `src/main/java/ec/edu/epn/huertos/actividades/ActividadRepository.java` | repositorio | 18 | — |
| `src/main/java/ec/edu/epn/huertos/actividades/ActividadResumen.java` | modelo / formulario / utilidad | 6 | evidencias |
| `src/main/java/ec/edu/epn/huertos/actividades/ActividadServicio.java` | servicio | 237 | compartido, comunidades, evidencias, huertos, misiones |
| `src/main/java/ec/edu/epn/huertos/actividades/DetalleActividad.java` | modelo / formulario / utilidad | 31 | evidencias |
| `src/main/java/ec/edu/epn/huertos/actividades/DetalleActividadServicio.java` | servicio | 88 | autenticacion, compartido, comunidades, evidencias, gamificacion, huertos |
| `src/main/java/ec/edu/epn/huertos/actividades/FormularioActividad.java` | modelo / formulario / utilidad | 58 | — |
| `src/main/java/ec/edu/epn/huertos/actividades/GestorActividadController.java` | controlador | 183 | autenticacion, compartido, huertos, misiones |
| `src/main/java/ec/edu/epn/huertos/actividades/ParticipanteController.java` | controlador | 40 | autenticacion, comunidades |
| `src/main/java/ec/edu/epn/huertos/actividades/ParticipanteServicio.java` | servicio | 60 | compartido, comunidades, evidencias, gamificacion |
| `src/main/java/ec/edu/epn/huertos/actividades/ParticipanteVista.java` | modelo / formulario / utilidad | 4 | — |
| `src/main/java/ec/edu/epn/huertos/actividades/TipoActividad.java` | modelo / formulario / utilidad | 21 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/AutenticacionController.java` | controlador | 26 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/AutorizacionServicio.java` | servicio | 30 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/FormularioRegistroCuenta.java` | modelo / formulario / utilidad | 60 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/ProvisionAdminTecnicoInicial.java` | componente | 40 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/RegistroCuentaController.java` | controlador | 36 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/RegistroCuentaServicio.java` | servicio | 42 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/Rol.java` | modelo / formulario / utilidad | 30 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/Usuario.java` | entidad | 115 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/UsuarioAutenticado.java` | modelo / formulario / utilidad | 66 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/UsuarioDetailsService.java` | servicio | 23 | — |
| `src/main/java/ec/edu/epn/huertos/autenticacion/UsuarioRepository.java` | repositorio | 27 | — |
| `src/main/java/ec/edu/epn/huertos/bodega/ArticuloBodega.java` | entidad | 51 | — |
| `src/main/java/ec/edu/epn/huertos/bodega/ArticuloBodegaRepository.java` | repositorio | 6 | — |
| `src/main/java/ec/edu/epn/huertos/bodega/BodegaController.java` | controlador | 30 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/bodega/BodegaServicio.java` | servicio | 23 | compartido, huertos |
| `src/main/java/ec/edu/epn/huertos/bodega/CategoriaArticulo.java` | modelo / formulario / utilidad | 7 | — |
| `src/main/java/ec/edu/epn/huertos/bodega/FormularioArticulo.java` | modelo / formulario / utilidad | 13 | — |
| `src/main/java/ec/edu/epn/huertos/compartido/ConflictoException.java` | modelo / formulario / utilidad | 9 | — |
| `src/main/java/ec/edu/epn/huertos/compartido/Fechas.java` | modelo / formulario / utilidad | 29 | — |
| `src/main/java/ec/edu/epn/huertos/compartido/ManejadorErrores.java` | controlador | 72 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/compartido/RecursoNoEncontradoException.java` | modelo / formulario / utilidad | 8 | — |
| `src/main/java/ec/edu/epn/huertos/compartido/ReglaNegocioException.java` | modelo / formulario / utilidad | 9 | — |
| `src/main/java/ec/edu/epn/huertos/compartido/SeguridadConfig.java` | configuración | 62 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/AdminTecnicoController.java` | controlador | 50 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/comunidades/AdminTecnicoServicio.java` | servicio | 69 | autenticacion, compartido |
| `src/main/java/ec/edu/epn/huertos/comunidades/AltaComunidadController.java` | controlador | 82 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/comunidades/AltaComunidadServicio.java` | servicio | 87 | autenticacion, compartido, huertos, misiones |
| `src/main/java/ec/edu/epn/huertos/comunidades/AsignacionRevisor.java` | entidad | 47 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/AsignacionRevisorRepository.java` | repositorio | 32 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/AsignacionRevisorServicio.java` | servicio | 43 | autenticacion, compartido |
| `src/main/java/ec/edu/epn/huertos/comunidades/AsignacionTecnicaVista.java` | modelo / formulario / utilidad | 4 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/Comunidad.java` | entidad | 152 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/ComunidadArchivadaVista.java` | modelo / formulario / utilidad | 6 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/ComunidadAsignadaVista.java` | modelo / formulario / utilidad | 4 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/ComunidadRepository.java` | repositorio | 21 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/ComunidadServicio.java` | servicio | 322 | autenticacion, compartido |
| `src/main/java/ec/edu/epn/huertos/comunidades/CuentaArchivadasController.java` | controlador | 46 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/comunidades/EstadoInvitacion.java` | modelo / formulario / utilidad | 8 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/EstadoSolicitudComunidad.java` | modelo / formulario / utilidad | 7 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/FormularioAltaComunidad.java` | modelo / formulario / utilidad | 34 | huertos |
| `src/main/java/ec/edu/epn/huertos/comunidades/FormularioInvitacion.java` | modelo / formulario / utilidad | 7 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/FormularioRechazoSolicitud.java` | modelo / formulario / utilidad | 13 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/FormularioSolicitudEliminacion.java` | modelo / formulario / utilidad | 13 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/GestorPanelController.java` | controlador | 148 | actividades, autenticacion, compartido, evidencias, gamificacion, huertos |
| `src/main/java/ec/edu/epn/huertos/comunidades/HistorialAsignacionRevisor.java` | entidad | 21 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/HistorialAsignacionRevisorRepository.java` | repositorio | 5 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/Integrante.java` | entidad | 96 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/IntegranteRepository.java` | repositorio | 39 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/IntegranteVista.java` | modelo / formulario / utilidad | 4 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/InvitacionComunidad.java` | entidad | 39 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/InvitacionComunidadController.java` | controlador | 49 | autenticacion, compartido |
| `src/main/java/ec/edu/epn/huertos/comunidades/InvitacionRepository.java` | repositorio | 12 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/InvitacionVista.java` | modelo / formulario / utilidad | 5 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/SolicitudComunidad.java` | entidad | 113 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/SolicitudComunidadRepository.java` | repositorio | 22 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/SolicitudComunidadServicio.java` | servicio | 207 | autenticacion, compartido, evidencias |
| `src/main/java/ec/edu/epn/huertos/comunidades/SolicitudComunidadVista.java` | modelo / formulario / utilidad | 7 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/SolicitudesComunidadController.java` | controlador | 68 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/comunidades/TipoInvitacion.java` | modelo / formulario / utilidad | 6 | — |
| `src/main/java/ec/edu/epn/huertos/comunidades/TipoSolicitudComunidad.java` | modelo / formulario / utilidad | 6 | — |
| `src/main/java/ec/edu/epn/huertos/demo/DatosDemo.java` | componente | 164 | autenticacion, comunidades, huertos, misiones |
| `src/main/java/ec/edu/epn/huertos/evidencias/AlmacenamientoArchivos.java` | modelo / formulario / utilidad | 16 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/AlmacenamientoLocal.java` | componente | 103 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/ArchivoBorradoPendiente.java` | entidad | 35 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/ArchivoBorradoPendienteRepository.java` | repositorio | 8 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/EliminacionComunidadServicio.java` | servicio | 62 | autenticacion, comunidades |
| `src/main/java/ec/edu/epn/huertos/evidencias/EnvioEvidencia.java` | entidad | 130 | compartido |
| `src/main/java/ec/edu/epn/huertos/evidencias/EnvioEvidenciaRepository.java` | repositorio | 32 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/EstadoRevision.java` | modelo / formulario / utilidad | 17 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/EvidenciaController.java` | controlador | 76 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/evidencias/EvidenciaServicio.java` | servicio | 68 | actividades, autenticacion, compartido, comunidades |
| `src/main/java/ec/edu/epn/huertos/evidencias/ImagenValidada.java` | modelo / formulario / utilidad | 10 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/LimpiezaArchivosBorrados.java` | servicio | 31 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/PropiedadesAlmacenamiento.java` | configuración | 8 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/Revision.java` | entidad | 71 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/RevisionController.java` | controlador | 66 | autenticacion, compartido, comunidades |
| `src/main/java/ec/edu/epn/huertos/evidencias/RevisionRepository.java` | repositorio | 15 | — |
| `src/main/java/ec/edu/epn/huertos/evidencias/RevisionServicio.java` | servicio | 167 | actividades, autenticacion, compartido, comunidades, gamificacion, misiones |
| `src/main/java/ec/edu/epn/huertos/evidencias/ValidadorImagen.java` | componente | 139 | compartido |
| `src/main/java/ec/edu/epn/huertos/evidencias/ValidadorVideo.java` | componente | 80 | compartido |
| `src/main/java/ec/edu/epn/huertos/evidencias/VideoValidado.java` | modelo / formulario / utilidad | 5 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/FilaRanking.java` | modelo / formulario / utilidad | 4 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/GamificacionServicio.java` | servicio | 170 | actividades, evidencias, misiones |
| `src/main/java/ec/edu/epn/huertos/gamificacion/Nivel.java` | modelo / formulario / utilidad | 18 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/PosicionRanking.java` | modelo / formulario / utilidad | 39 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/ProgresoHuerto.java` | modelo / formulario / utilidad | 6 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/PropiedadesGamificacion.java` | configuración | 22 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/PuntosComunidad.java` | modelo / formulario / utilidad | 4 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaComunitaria.java` | entidad | 56 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaComunitariaRepository.java` | repositorio | 31 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaIndividual.java` | entidad | 56 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaIndividualRepository.java` | repositorio | 16 | — |
| `src/main/java/ec/edu/epn/huertos/gamificacion/ResumenPuntos.java` | modelo / formulario / utilidad | 5 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/CatalogoController.java` | controlador | 104 | autenticacion, compartido |
| `src/main/java/ec/edu/epn/huertos/huertos/CatalogoServicio.java` | servicio | 99 | autenticacion, compartido, evidencias |
| `src/main/java/ec/edu/epn/huertos/huertos/CicloPlanta.java` | modelo / formulario / utilidad | 7 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/CualidadPlanta.java` | modelo / formulario / utilidad | 7 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/EspacioHuerto.java` | entidad | 51 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/EspacioHuertoRepository.java` | repositorio | 10 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/EspacioHuertoServicio.java` | servicio | 49 | compartido |
| `src/main/java/ec/edu/epn/huertos/huertos/Especie.java` | entidad | 78 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/EspecieRepository.java` | repositorio | 6 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/FichaEspecie.java` | modelo / formulario / utilidad | 109 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/FormularioHuerto.java` | modelo / formulario / utilidad | 181 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/FormularioPlanta.java` | modelo / formulario / utilidad | 71 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/GestorHuertoController.java` | controlador | 141 | autenticacion |
| `src/main/java/ec/edu/epn/huertos/huertos/Huerto.java` | entidad | 143 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/HuertoCerramiento.java` | entidad | 34 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/HuertoCerramientoRepository.java` | repositorio | 9 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/HuertoCerramientoServicio.java` | servicio | 32 | compartido |
| `src/main/java/ec/edu/epn/huertos/huertos/HuertoRepository.java` | repositorio | 17 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/HuertoServicio.java` | servicio | 166 | compartido, comunidades |
| `src/main/java/ec/edu/epn/huertos/huertos/HuertoVirtual.java` | modelo / formulario / utilidad | 151 | gamificacion |
| `src/main/java/ec/edu/epn/huertos/huertos/HuertoVirtualServicio.java` | servicio | 62 | gamificacion |
| `src/main/java/ec/edu/epn/huertos/huertos/Planta.java` | entidad | 114 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/PlantaPlantadaVista.java` | modelo / formulario / utilidad | 8 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/PlantaRepository.java` | repositorio | 18 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/PlantaVirtualLectura.java` | repositorio | 24 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/RegionPlanta.java` | modelo / formulario / utilidad | 7 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/SiembraPlanta.java` | modelo / formulario / utilidad | 11 | — |
| `src/main/java/ec/edu/epn/huertos/huertos/SpritePlanta.java` | modelo / formulario / utilidad | 129 | — |
| `src/main/java/ec/edu/epn/huertos/HuertosApplication.java` | configuración | 16 | — |
| `src/main/java/ec/edu/epn/huertos/misiones/EstadoMision.java` | modelo / formulario / utilidad | 5 | — |
| `src/main/java/ec/edu/epn/huertos/misiones/MisionHuerto.java` | entidad | 50 | compartido |
| `src/main/java/ec/edu/epn/huertos/misiones/MisionHuertoRepository.java` | repositorio | 19 | — |
| `src/main/java/ec/edu/epn/huertos/misiones/MisionPlantilla.java` | entidad | 42 | actividades |
| `src/main/java/ec/edu/epn/huertos/misiones/MisionPlantillaRepository.java` | repositorio | 8 | — |
| `src/main/java/ec/edu/epn/huertos/misiones/MisionServicio.java` | servicio | 119 | actividades, compartido |
| `src/main/java/ec/edu/epn/huertos/misiones/RecurrenciaMision.java` | modelo / formulario / utilidad | 5 | — |
| `src/main/java/ec/edu/epn/huertos/publico/PublicoController.java` | controlador | 50 | — |
| `src/main/java/ec/edu/epn/huertos/publico/PublicoServicio.java` | servicio | 87 | compartido, comunidades, gamificacion, huertos |

## Pruebas declaradas

| Archivo | Métodos @Test |
|---|---:|
| `src/test/java/ec/edu/epn/huertos/actividades/ActividadServicioIT.java` | 12 |
| `src/test/java/ec/edu/epn/huertos/comunidades/CupoComunidadIT.java` | 8 |
| `src/test/java/ec/edu/epn/huertos/comunidades/SolicitudesComunidadIT.java` | 6 |
| `src/test/java/ec/edu/epn/huertos/evidencias/AlmacenamientoLocalTest.java` | 4 |
| `src/test/java/ec/edu/epn/huertos/evidencias/RevisionServicioIT.java` | 13 |
| `src/test/java/ec/edu/epn/huertos/evidencias/ValidadorImagenTest.java` | 10 |
| `src/test/java/ec/edu/epn/huertos/FabricaDatos.java` | 0 |
| `src/test/java/ec/edu/epn/huertos/gamificacion/GamificacionReglasTest.java` | 5 |
| `src/test/java/ec/edu/epn/huertos/gamificacion/PosicionRankingTest.java` | 7 |
| `src/test/java/ec/edu/epn/huertos/huertos/CatalogoBloqueDosIT.java` | 6 |
| `src/test/java/ec/edu/epn/huertos/huertos/CatalogoBloqueTresIT.java` | 4 |
| `src/test/java/ec/edu/epn/huertos/huertos/CatalogoBodegaIT.java` | 10 |
| `src/test/java/ec/edu/epn/huertos/huertos/CatalogoCargaInicialIT.java` | 5 |
| `src/test/java/ec/edu/epn/huertos/huertos/CatalogoInicialIT.java` | 2 |
| `src/test/java/ec/edu/epn/huertos/huertos/FormularioHuertoTarjetasIT.java` | 6 |
| `src/test/java/ec/edu/epn/huertos/huertos/HuertoVirtualIT.java` | 14 |
| `src/test/java/ec/edu/epn/huertos/huertos/HuertoVirtualTest.java` | 6 |
| `src/test/java/ec/edu/epn/huertos/huertos/IlustracionesPlantasTest.java` | 3 |
| `src/test/java/ec/edu/epn/huertos/huertos/PlantasIT.java` | 4 |
| `src/test/java/ec/edu/epn/huertos/huertos/PlantaVirtualLecturaIT.java` | 1 |
| `src/test/java/ec/edu/epn/huertos/huertos/SiembraYBodegaComunidadIT.java` | 4 |
| `src/test/java/ec/edu/epn/huertos/huertos/SpritePlantaTest.java` | 5 |
| `src/test/java/ec/edu/epn/huertos/misiones/MisionHuertoTest.java` | 3 |
| `src/test/java/ec/edu/epn/huertos/PruebaIntegracion.java` | 0 |
| `src/test/java/ec/edu/epn/huertos/publico/PublicoPaginasTest.java` | 3 |
| `src/test/java/ec/edu/epn/huertos/publico/PublicoServicioTest.java` | 3 |
| `src/test/java/ec/edu/epn/huertos/RecorridoPrincipalIT.java` | 1 |
| `src/test/java/ec/edu/epn/huertos/SeguridadWebIT.java` | 13 |
| `src/test/java/ec/edu/epn/huertos/ValidacionFormulariosTest.java` | 3 |

## Resto de archivos versionados

Se incluyen recursos, documentación, scripts y el árbol binario heredado; se excluye target generado durante la revisión.

| Ruta | Bytes |
|---|---:|
| `.dockerignore` | 61 |
| `.env.example` | 669 |
| `.gitattributes` | 57 |
| `.gitignore` | 121 |
| `.mvn/wrapper/maven-wrapper.properties` | 171 |
| `bin/.dockerignore` | 43 |
| `bin/.env.example` | 485 |
| `bin/.gitignore` | 112 |
| `bin/.mvn/wrapper/maven-wrapper.properties` | 171 |
| `bin/compose.yaml` | 1333 |
| `bin/Dockerfile` | 687 |
| `bin/docs/alcance-prototipo.md` | 3313 |
| `bin/docs/arquitectura.md` | 7118 |
| `bin/docs/avance.md` | 3324 |
| `bin/docs/backlog.md` | 1442 |
| `bin/docs/decisiones.md` | 6681 |
| `bin/docs/guion-demostracion.md` | 4401 |
| `bin/docs/pendientes.md` | 3907 |
| `bin/docs/pruebas.md` | 4660 |
| `bin/docs/referencias/LEEME.md` | 463 |
| `bin/docs/trazabilidad.md` | 10322 |
| `bin/mvnw` | 11790 |
| `bin/mvnw.cmd` | 8481 |
| `bin/pom.xml` | 4474 |
| `bin/README.md` | 6000 |
| `bin/scripts/iniciar-local.ps1` | 1139 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/Actividad.class` | 8660 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ActividadParticipante.class` | 2923 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ActividadParticipanteRepository.class` | 2190 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ActividadRepository.class` | 2048 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ActividadResumen.class` | 501 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ActividadServicio.class` | 11820 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/DetalleActividad$EnvioVista.class` | 727 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/DetalleActividad.class` | 1244 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/DetalleActividadServicio.class` | 8850 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/FormularioActividad.class` | 4356 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/GestorActividadController.class` | 11093 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ParticipanteController.class` | 2192 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ParticipanteServicio$Panel.class` | 942 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ParticipanteServicio$Participacion.class` | 662 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ParticipanteServicio.class` | 5057 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/ParticipanteVista.class` | 372 |
| `bin/src/main/java/ec/edu/epn/huertos/actividades/TipoActividad.class` | 2181 |
| `bin/src/main/java/ec/edu/epn/huertos/autenticacion/AutenticacionController.class` | 2026 |
| `bin/src/main/java/ec/edu/epn/huertos/autenticacion/AutorizacionServicio.class` | 1884 |
| `bin/src/main/java/ec/edu/epn/huertos/autenticacion/Rol.class` | 1949 |
| `bin/src/main/java/ec/edu/epn/huertos/autenticacion/Usuario.class` | 4167 |
| `bin/src/main/java/ec/edu/epn/huertos/autenticacion/UsuarioAutenticado.class` | 4530 |
| `bin/src/main/java/ec/edu/epn/huertos/autenticacion/UsuarioDetailsService.class` | 2120 |
| `bin/src/main/java/ec/edu/epn/huertos/autenticacion/UsuarioRepository.class` | 1418 |
| `bin/src/main/java/ec/edu/epn/huertos/compartido/ConflictoException.class` | 954 |
| `bin/src/main/java/ec/edu/epn/huertos/compartido/Fechas.class` | 2683 |
| `bin/src/main/java/ec/edu/epn/huertos/compartido/ManejadorErrores.class` | 7199 |
| `bin/src/main/java/ec/edu/epn/huertos/compartido/RecursoNoEncontradoException.class` | 944 |
| `bin/src/main/java/ec/edu/epn/huertos/compartido/ReglaNegocioException.class` | 923 |
| `bin/src/main/java/ec/edu/epn/huertos/compartido/SeguridadConfig.class` | 2314 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/AsignacionRevisor.class` | 2758 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/AsignacionRevisorRepository.class` | 1533 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/Comunidad.class` | 4166 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/ComunidadRepository.class` | 1753 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/ComunidadServicio.class` | 7054 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/GestorPanelController.class` | 7729 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/Integrante.class` | 3777 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/IntegranteRepository.class` | 2189 |
| `bin/src/main/java/ec/edu/epn/huertos/comunidades/IntegranteVista.class` | 432 |
| `bin/src/main/java/ec/edu/epn/huertos/demo/DatosDemo.class` | 8452 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/AlmacenamientoArchivos.class` | 1219 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/AlmacenamientoLocal.class` | 6018 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/EnvioEvidencia.class` | 6065 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/EnvioEvidenciaRepository.class` | 2729 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/EstadoRevision.class` | 1698 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/EvidenciaController.class` | 3461 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/EvidenciaServicio$FotoAutorizada.class` | 530 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/EvidenciaServicio.class` | 5286 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/ImagenValidada.class` | 733 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/PropiedadesAlmacenamiento.class` | 509 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/Revision.class` | 4283 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/RevisionController.class` | 5031 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/RevisionRepository.class` | 1972 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/RevisionServicio$DetalleRevision.class` | 640 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/RevisionServicio$ItemBandeja.class` | 676 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/RevisionServicio$ItemHistorial.class` | 739 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/RevisionServicio$Resultado.class` | 516 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/RevisionServicio.class` | 9379 |
| `bin/src/main/java/ec/edu/epn/huertos/evidencias/ValidadorImagen.class` | 7705 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/FilaRanking.class` | 442 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/GamificacionServicio.class` | 8128 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/Nivel.class` | 1648 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/ProgresoHuerto.class` | 487 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/PropiedadesGamificacion$Etapa.class` | 500 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/PropiedadesGamificacion.class` | 867 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/PuntosComunidad.class` | 421 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaComunitaria.class` | 2995 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaComunitariaRepository.class` | 2226 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaIndividual.class` | 3171 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/RecompensaIndividualRepository.class` | 1860 |
| `bin/src/main/java/ec/edu/epn/huertos/gamificacion/ResumenPuntos.class` | 428 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/FormularioHuerto.class` | 2896 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/FormularioPlanta.class` | 4483 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/GestorHuertoController.class` | 8407 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/Huerto.class` | 4244 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/HuertoRepository.class` | 1330 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/HuertoServicio.class` | 7787 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/Planta.class` | 4930 |
| `bin/src/main/java/ec/edu/epn/huertos/huertos/PlantaRepository.class` | 1487 |
| `bin/src/main/java/ec/edu/epn/huertos/HuertosApplication.class` | 1646 |
| `bin/src/main/java/ec/edu/epn/huertos/publico/PublicoController.class` | 4759 |
| `bin/src/main/java/ec/edu/epn/huertos/publico/PublicoServicio$Ficha.class` | 782 |
| `bin/src/main/java/ec/edu/epn/huertos/publico/PublicoServicio$HuertoMapa.class` | 719 |
| `bin/src/main/java/ec/edu/epn/huertos/publico/PublicoServicio.class` | 6095 |
| `bin/src/main/resources/application-demo.yml` | 217 |
| `bin/src/main/resources/application-test.yml` | 128 |
| `bin/src/main/resources/application.yml` | 1924 |
| `bin/src/main/resources/db/migration/V1__comunidades_usuarios_huertos.sql` | 4481 |
| `bin/src/main/resources/db/migration/V2__actividades_y_evidencias.sql` | 4104 |
| `bin/src/main/resources/db/migration/V3__revisiones_y_recompensas.sql` | 4798 |
| `bin/src/main/resources/static/css/app.css` | 1223 |
| `bin/src/main/resources/static/js/app.js` | 1025 |
| `bin/src/main/resources/static/js/mapa.js` | 2668 |
| `bin/src/main/resources/static/vendor/bootstrap/bootstrap.bundle.min.js` | 80679 |
| `bin/src/main/resources/static/vendor/bootstrap/bootstrap.min.css` | 232763 |
| `bin/src/main/resources/static/vendor/leaflet/images/layers-2x.png` | 1259 |
| `bin/src/main/resources/static/vendor/leaflet/images/layers.png` | 696 |
| `bin/src/main/resources/static/vendor/leaflet/images/marker-icon-2x.png` | 2464 |
| `bin/src/main/resources/static/vendor/leaflet/images/marker-icon.png` | 1466 |
| `bin/src/main/resources/static/vendor/leaflet/images/marker-shadow.png` | 618 |
| `bin/src/main/resources/static/vendor/leaflet/leaflet.css` | 14806 |
| `bin/src/main/resources/static/vendor/leaflet/leaflet.js` | 147557 |
| `bin/src/main/resources/templates/auth/login.html` | 1604 |
| `bin/src/main/resources/templates/error/403.html` | 461 |
| `bin/src/main/resources/templates/error/404.html` | 416 |
| `bin/src/main/resources/templates/error/500.html` | 417 |
| `bin/src/main/resources/templates/error/regla.html` | 419 |
| `bin/src/main/resources/templates/fragmentos/base.html` | 4071 |
| `bin/src/main/resources/templates/fragmentos/componentes.html` | 1108 |
| `bin/src/main/resources/templates/gestor/actividad-detalle.html` | 5883 |
| `bin/src/main/resources/templates/gestor/actividad-form.html` | 4484 |
| `bin/src/main/resources/templates/gestor/actividades.html` | 1634 |
| `bin/src/main/resources/templates/gestor/huerto-form.html` | 1178 |
| `bin/src/main/resources/templates/gestor/integrantes.html` | 2218 |
| `bin/src/main/resources/templates/gestor/panel.html` | 2514 |
| `bin/src/main/resources/templates/gestor/planta-form.html` | 2024 |
| `bin/src/main/resources/templates/gestor/plantas.html` | 2209 |
| `bin/src/main/resources/templates/participante/panel.html` | 3032 |
| `bin/src/main/resources/templates/publico/comunidad.html` | 3189 |
| `bin/src/main/resources/templates/publico/inicio.html` | 2237 |
| `bin/src/main/resources/templates/publico/mapa.html` | 1837 |
| `bin/src/main/resources/templates/publico/ranking.html` | 1387 |
| `bin/src/main/resources/templates/revision/bandeja.html` | 1659 |
| `bin/src/main/resources/templates/revision/detalle.html` | 3391 |
| `bin/src/main/resources/templates/revision/historial.html` | 1283 |
| `bin/src/test/java/ec/edu/epn/huertos/actividades/ActividadServicioIT.class` | 9816 |
| `bin/src/test/java/ec/edu/epn/huertos/comunidades/CupoComunidadIT.class` | 10113 |
| `bin/src/test/java/ec/edu/epn/huertos/evidencias/AlmacenamientoLocalTest.class` | 7890 |
| `bin/src/test/java/ec/edu/epn/huertos/evidencias/RevisionServicioIT.class` | 9597 |
| `bin/src/test/java/ec/edu/epn/huertos/evidencias/ValidadorImagenTest.class` | 7566 |
| `bin/src/test/java/ec/edu/epn/huertos/FabricaDatos$Escenario.class` | 772 |
| `bin/src/test/java/ec/edu/epn/huertos/FabricaDatos.class` | 9040 |
| `bin/src/test/java/ec/edu/epn/huertos/gamificacion/GamificacionReglasTest.class` | 3910 |
| `bin/src/test/java/ec/edu/epn/huertos/huertos/PlantasIT.class` | 8826 |
| `bin/src/test/java/ec/edu/epn/huertos/PruebaIntegracion.class` | 2220 |
| `bin/src/test/java/ec/edu/epn/huertos/RecorridoPrincipalIT.class` | 9849 |
| `bin/src/test/java/ec/edu/epn/huertos/SeguridadWebIT.class` | 10551 |
| `compose.yaml` | 1498 |
| `Distribucion_equipo_7_personas.docx` | 41053 |
| `Dockerfile` | 890 |
| `docs/alcance-prototipo.md` | 3672 |
| `docs/arquitectura.md` | 7830 |
| `docs/avance.md` | 3330 |
| `docs/backlog.md` | 1442 |
| `docs/catalogo/bloque-01.json` | 28900 |
| `docs/catalogo/bloque-02-depurado.json` | 42707 |
| `docs/catalogo/bloque-02-depurado.md` | 32321 |
| `docs/catalogo/bloque-03-depurado.json` | 33996 |
| `docs/catalogo/bloque-03-depurado.md` | 28357 |
| `docs/catalogo/carga-bloque-01.sql` | 31438 |
| `docs/catalogo/carga-bloque-02.sql` | 31634 |
| `docs/catalogo/carga-bloque-03.sql` | 27265 |
| `docs/catalogo/fotografias/achiote.jpg` | 210904 |
| `docs/catalogo/fotografias/aguacate.jpg` | 335046 |
| `docs/catalogo/fotografias/ajo-sacha.jpg` | 640203 |
| `docs/catalogo/fotografias/albahaca.jpg` | 249445 |
| `docs/catalogo/fotografias/algodon.jpg` | 358758 |
| `docs/catalogo/fotografias/bloque-02/fotografias.json` | 14651 |
| `docs/catalogo/fotografias/bloque-02/guayusa.jpg` | 668671 |
| `docs/catalogo/fotografias/bloque-02/hierbabuena.jpg` | 364542 |
| `docs/catalogo/fotografias/bloque-02/jengibre.jpg` | 875342 |
| `docs/catalogo/fotografias/bloque-02/llanten-hoja-estrecha.jpg` | 460350 |
| `docs/catalogo/fotografias/bloque-02/llanten-mayor.jpg` | 472537 |
| `docs/catalogo/fotografias/bloque-02/manzanilla.jpg` | 211603 |
| `docs/catalogo/fotografias/bloque-02/marco.jpg` | 192653 |
| `docs/catalogo/fotografias/bloque-02/matico.jpg` | 190107 |
| `docs/catalogo/fotografias/bloque-02/menta.jpg` | 234185 |
| `docs/catalogo/fotografias/bloque-02/oregano.jpg` | 425443 |
| `docs/catalogo/fotografias/bloque-02/README.md` | 3278 |
| `docs/catalogo/fotografias/bloque-02/romero.jpg` | 1639539 |
| `docs/catalogo/fotografias/bloque-03/fotografias.json` | 11994 |
| `docs/catalogo/fotografias/bloque-03/orquidea-ecuatoriana.jpg` | 244929 |
| `docs/catalogo/fotografias/bloque-03/ortiga-menor.jpg` | 141672 |
| `docs/catalogo/fotografias/bloque-03/paico.jpg` | 640885 |
| `docs/catalogo/fotografias/bloque-03/README.md` | 2756 |
| `docs/catalogo/fotografias/bloque-03/taraxaco.jpg` | 731261 |
| `docs/catalogo/fotografias/bloque-03/tomillo.jpg` | 404672 |
| `docs/catalogo/fotografias/bloque-03/toronjil.jpg` | 123549 |
| `docs/catalogo/fotografias/bloque-03/una-de-gato.jpg` | 99343 |
| `docs/catalogo/fotografias/bloque-03/violeta.jpg` | 345616 |
| `docs/catalogo/fotografias/bloque-03/yuca.jpg` | 497338 |
| `docs/catalogo/fotografias/cafe-arabigo.jpg` | 597540 |
| `docs/catalogo/fotografias/canela-amazonica.jpg` | 164594 |
| `docs/catalogo/fotografias/ceibo-costeno.jpg` | 586978 |
| `docs/catalogo/fotografias/chilca.jpg` | 152319 |
| `docs/catalogo/fotografias/escancel.jpg` | 1026553 |
| `docs/catalogo/fotografias/fotografias.json` | 13858 |
| `docs/catalogo/incorporacion-bloque-02.md` | 6257 |
| `docs/catalogo/incorporacion-bloque-03.md` | 7420 |
| `docs/catalogo/primera-carga.md` | 6916 |
| `docs/catalogo/revisar-bloque-01.sql` | 693 |
| `docs/catalogo/revision-bloque-02.md` | 9490 |
| `docs/catalogo-plantas-bodega.md` | 5563 |
| `docs/decisiones.md` | 7833 |
| `docs/despliegue.md` | 5210 |
| `docs/estilos.md` | 2450 |
| `docs/guion-demostracion.md` | 4477 |
| `docs/huerto-virtual.md` | 13367 |
| `docs/misiones-implementacion.md` | 2103 |
| `docs/pendientes.md` | 3972 |
| `docs/plan-mejoras-reunion.md` | 5721 |
| `docs/pruebas.md` | 5080 |
| `docs/referencias/LEEME.md` | 463 |
| `docs/seccion-1-mapa-publico.md` | 4864 |
| `docs/trazabilidad.md` | 10897 |
| `frontend/README.md` | 2001 |
| `frontend/static/css/app.css` | 18570 |
| `frontend/static/css/bodega.css` | 7479 |
| `frontend/static/css/huerto-form.css` | 3761 |
| `frontend/static/css/huerto-virtual.css` | 19285 |
| `frontend/static/css/interior.css` | 19564 |
| `frontend/static/css/paleta.css` | 5803 |
| `frontend/static/css/plantas.css` | 8134 |
| `frontend/static/css/portada.css` | 26422 |
| `frontend/static/css/publico.css` | 16356 |
| `frontend/static/css/revision.css` | 1892 |
| `frontend/static/favicon.ico` | 180638 |
| `frontend/static/img/huerto/cerramientos/CONVENCIONAL.jpg` | 160627 |
| `frontend/static/img/huerto/cerramientos/METALICO.jpg` | 163843 |
| `frontend/static/img/huerto/cerramientos/NATURAL.jpg` | 178410 |
| `frontend/static/img/huerto/cerramientos/RECICLADO.jpg` | 185100 |
| `frontend/static/img/huerto/cerramientos/VIVA.jpg` | 196988 |
| `frontend/static/img/huerto/disenos/COLGANTE_TECHOS.jpg` | 194092 |
| `frontend/static/img/huerto/disenos/PISO_BALCONES.jpg` | 241188 |
| `frontend/static/img/huerto/disenos/PISO_BOTELLAS_PATIO.jpg` | 337901 |
| `frontend/static/img/huerto/disenos/PISO_FILOS_VENTANA.jpg` | 182836 |
| `frontend/static/img/huerto/disenos/PISO_GRADAS.jpg` | 253422 |
| `frontend/static/img/huerto/disenos/PISO_LLANTAS_PATIO.jpg` | 286164 |
| `frontend/static/img/huerto/disenos/PISO_MACETAS.jpg` | 263886 |
| `frontend/static/img/huerto/disenos/PISO_TERRAZA.jpg` | 279551 |
| `frontend/static/img/huerto/disenos/TIERRA_CAMADAS.jpg` | 474282 |
| `frontend/static/img/huerto/disenos/TIERRA_CAMELLONES.jpg` | 315172 |
| `frontend/static/img/huerto/disenos/TIERRA_SURCOS.jpg` | 264712 |
| `frontend/static/img/huerto/disenos/TIERRA_TERRAZAS.jpg` | 289201 |
| `frontend/static/img/huerto/disenos/VERTICAL_PAREDES.jpg` | 288895 |
| `frontend/static/img/huerto/escena-objetos.svg` | 13260 |
| `frontend/static/img/huerto/escena-tierra.svg` | 10837 |
| `frontend/static/img/huerto/galpon-arriba.svg` | 2828 |
| `frontend/static/img/huerto/plantas/achiote.svg` | 69861 |
| `frontend/static/img/huerto/plantas/aguacate.svg` | 57036 |
| `frontend/static/img/huerto/plantas/albahaca.svg` | 34619 |
| `frontend/static/img/huerto/plantas/algodon.svg` | 53393 |
| `frontend/static/img/huerto/plantas/cafe.svg` | 68540 |
| `frontend/static/img/huerto/plantas/canela.svg` | 106924 |
| `frontend/static/img/huerto/plantas/ceibo.svg` | 230324 |
| `frontend/static/img/huerto/plantas/chilca.svg` | 180401 |
| `frontend/static/img/huerto/plantas/escancel.svg` | 326896 |
| `frontend/static/img/huerto/plantas/guayusa.svg` | 185037 |
| `frontend/static/img/huerto/plantas/hierba-luisa.svg` | 103129 |
| `frontend/static/img/huerto/plantas/hierbabuena.svg` | 240434 |
| `frontend/static/img/huerto/plantas/jengibre.svg` | 281286 |
| `frontend/static/img/huerto/plantas/llanten-hoja-estrecha.svg` | 344500 |
| `frontend/static/img/huerto/plantas/llanten-mayor.svg` | 174670 |
| `frontend/static/img/huerto/plantas/manzanilla.svg` | 239000 |
| `frontend/static/img/huerto/plantas/marco.svg` | 228018 |
| `frontend/static/img/huerto/plantas/matico.svg` | 165352 |
| `frontend/static/img/huerto/plantas/menta.svg` | 259158 |
| `frontend/static/img/huerto/plantas/taraxaco.svg` | 218479 |
| `frontend/static/img/huerto/suelo.svg` | 625 |
| `frontend/static/img/huerto/suelos/ARCILLOSO.jpg` | 303096 |
| `frontend/static/img/huerto/suelos/ARENOSO.jpg` | 330035 |
| `frontend/static/img/huerto/suelos/GREDOSO.jpg` | 344711 |
| `frontend/static/img/huerto/suelos/LIMOSO.jpg` | 285072 |
| `frontend/static/img/huerto/suelos/MARGOSO.jpg` | 341488 |
| `frontend/static/img/huerto/suelos/PANTANOSO.jpg` | 340043 |
| `frontend/static/img/huerto-form/abono-barbecho.svg` | 714 |
| `frontend/static/img/huerto-form/abono-compost.svg` | 505 |
| `frontend/static/img/huerto-form/abono-hoja.svg` | 871 |
| `frontend/static/img/huerto-form/abono-restos_vegetales.svg` | 510 |
| `frontend/static/img/huerto-form/cerramiento-convencional.svg` | 1841 |
| `frontend/static/img/huerto-form/cerramiento-metalico.svg` | 1015 |
| `frontend/static/img/huerto-form/cerramiento-natural.svg` | 1337 |
| `frontend/static/img/huerto-form/cerramiento-reciclado.svg` | 793 |
| `frontend/static/img/huerto-form/cerramiento-vivo.svg` | 708 |
| `frontend/static/img/huerto-form/diseno-colgante.svg` | 1499 |
| `frontend/static/img/huerto-form/diseno-piso.svg` | 1562 |
| `frontend/static/img/huerto-form/diseno-tierra.svg` | 2474 |
| `frontend/static/img/huerto-form/diseno-vertical.svg` | 2793 |
| `frontend/static/img/huerto-form/suelo-arcilloso.svg` | 413 |
| `frontend/static/img/huerto-form/suelo-arenoso.svg` | 3327 |
| `frontend/static/img/huerto-form/suelo-gredoso.svg` | 471 |
| `frontend/static/img/huerto-form/suelo-limoso.svg` | 594 |
| `frontend/static/img/huerto-form/suelo-margoso.svg` | 586 |
| `frontend/static/img/huerto-form/suelo-pantanoso.svg` | 501 |
| `frontend/static/img/marca/allpana-emblema.png` | 160226 |
| `frontend/static/img/marca/allpana-logo.png` | 288128 |
| `frontend/static/img/planta-referencia.svg` | 601 |
| `frontend/static/img/portada/amazonia-1280.webp` | 124422 |
| `frontend/static/img/portada/amazonia-1920.jpg` | 362453 |
| `frontend/static/img/portada/amazonia-1920.webp` | 235010 |
| `frontend/static/img/portada/amazonia-2880.webp` | 377872 |
| `frontend/static/img/portada/amazonia-800.webp` | 52958 |
| `frontend/static/img/publico/sembrar.svg` | 2593 |
| `frontend/static/js/app.js` | 1025 |
| `frontend/static/js/interior.js` | 3363 |
| `frontend/static/js/mapa.js` | 10459 |
| `frontend/static/js/portada.js` | 9806 |
| `frontend/static/vendor/bootstrap/bootstrap.bundle.min.js` | 80679 |
| `frontend/static/vendor/bootstrap/bootstrap.min.css` | 232763 |
| `frontend/static/vendor/leaflet/images/layers-2x.png` | 1259 |
| `frontend/static/vendor/leaflet/images/layers.png` | 696 |
| `frontend/static/vendor/leaflet/images/marker-icon-2x.png` | 2464 |
| `frontend/static/vendor/leaflet/images/marker-icon.png` | 1466 |
| `frontend/static/vendor/leaflet/images/marker-shadow.png` | 618 |
| `frontend/static/vendor/leaflet/leaflet.css` | 14806 |
| `frontend/static/vendor/leaflet/leaflet.js` | 147557 |
| `frontend/templates/admin-tecnico/panel.html` | 3502 |
| `frontend/templates/auth/archivadas.html` | 3535 |
| `frontend/templates/auth/cuenta.html` | 1184 |
| `frontend/templates/auth/login.html` | 2157 |
| `frontend/templates/auth/registro.html` | 4685 |
| `frontend/templates/cuenta/comunidad-form.html` | 7915 |
| `frontend/templates/cuenta/invitaciones.html` | 1515 |
| `frontend/templates/cuenta/solicitudes-comunidad.html` | 1674 |
| `frontend/templates/error/403.html` | 461 |
| `frontend/templates/error/404.html` | 416 |
| `frontend/templates/error/500.html` | 417 |
| `frontend/templates/error/regla.html` | 419 |
| `frontend/templates/fragmentos/arbol-sumate.html` | 94187 |
| `frontend/templates/fragmentos/base.html` | 12740 |
| `frontend/templates/fragmentos/bodega.html` | 1651 |
| `frontend/templates/fragmentos/componentes.html` | 2076 |
| `frontend/templates/fragmentos/huerto-virtual.html` | 12149 |
| `frontend/templates/fragmentos/plantas.html` | 1744 |
| `frontend/templates/gestor/actividad-detalle.html` | 6311 |
| `frontend/templates/gestor/actividad-form.html` | 4928 |
| `frontend/templates/gestor/actividades.html` | 1747 |
| `frontend/templates/gestor/bodega-form.html` | 1482 |
| `frontend/templates/gestor/bodega.html` | 2619 |
| `frontend/templates/gestor/catalogo.html` | 5152 |
| `frontend/templates/gestor/especie-detalle.html` | 8079 |
| `frontend/templates/gestor/especie-form.html` | 6937 |
| `frontend/templates/gestor/huerto-form.html` | 7957 |
| `frontend/templates/gestor/integrantes.html` | 7283 |
| `frontend/templates/gestor/misiones.html` | 2741 |
| `frontend/templates/gestor/panel.html` | 3610 |
| `frontend/templates/gestor/planta-form.html` | 2199 |
| `frontend/templates/gestor/plantas.html` | 5031 |
| `frontend/templates/gestor/siembra-form.html` | 1471 |
| `frontend/templates/participante/panel.html` | 3608 |
| `frontend/templates/publico/comunidad.html` | 6783 |
| `frontend/templates/publico/inicio.html` | 12667 |
| `frontend/templates/publico/mapa.html` | 7367 |
| `frontend/templates/publico/ranking.html` | 1387 |
| `frontend/templates/revision/bandeja.html` | 2656 |
| `frontend/templates/revision/detalle.html` | 3761 |
| `frontend/templates/revision/historial.html` | 1358 |
| `frontend/templates/revision/solicitudes-comunidad.html` | 2875 |
| `mvnw` | 11790 |
| `mvnw.cmd` | 8481 |
| `pom.xml` | 5825 |
| `README.md` | 8317 |
| `render.yaml` | 2501 |
| `scripts/cargar-fotos-catalogo.ps1` | 6579 |
| `scripts/iniciar-local.ps1` | 1343 |
| `src/main/resources/application-demo.yml` | 217 |
| `src/main/resources/application-test.yml` | 178 |
| `src/main/resources/application.yml` | 2657 |
| `src/main/resources/db/migration/V10__archivo_comunidad.sql` | 72 |
| `src/main/resources/db/migration/V11__estado_planta_plantada.sql` | 157 |
| `src/main/resources/db/migration/V12__detalle_registro_comunidad_y_huerto.sql` | 2120 |
| `src/main/resources/db/migration/V13__solicitudes_ministeriales_y_estado_comunidad.sql` | 5483 |
| `src/main/resources/db/migration/V14__capacidad_por_tipo_y_responsable_unico.sql` | 2591 |
| `src/main/resources/db/migration/V15__cupo_comunidad_por_tipo.sql` | 1089 |
| `src/main/resources/db/migration/V18__espacios_huerto.sql` | 1138 |
| `src/main/resources/db/migration/V19__evidencia_video.sql` | 564 |
| `src/main/resources/db/migration/V1__comunidades_usuarios_huertos.sql` | 4481 |
| `src/main/resources/db/migration/V2026110801__catalogo_comun_plantas_y_bodega.sql` | 2422 |
| `src/main/resources/db/migration/V2026110802__contexto_de_fichas_del_catalogo.sql` | 773 |
| `src/main/resources/db/migration/V2026111101__misiones_por_huerto.sql` | 4464 |
| `src/main/resources/db/migration/V20__administrador_tecnico.sql` | 767 |
| `src/main/resources/db/migration/V21__cerramientos_mixtos_por_grupo.sql` | 998 |
| `src/main/resources/db/migration/V22__cerramientos_en_cinco_tipos.sql` | 1312 |
| `src/main/resources/db/migration/V2__actividades_y_evidencias.sql` | 4104 |
| `src/main/resources/db/migration/V3__revisiones_y_recompensas.sql` | 4798 |
| `src/main/resources/db/migration/V4__alta_comunidades_perfiles.sql` | 718 |
| `src/main/resources/db/migration/V5__rol_comunidades_es_gestor.sql` | 295 |
| `src/main/resources/db/migration/V6__historial_pertenencias_usuario.sql` | 385 |
| `src/main/resources/db/migration/V7__registro_cuenta_sin_comunidad.sql` | 539 |
| `src/main/resources/db/migration/V8__datos_completos_huerto.sql` | 1056 |
| `src/main/resources/db/migration/V9__invitaciones_comunidad.sql` | 967 |
