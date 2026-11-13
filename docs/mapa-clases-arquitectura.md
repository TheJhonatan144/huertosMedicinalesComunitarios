# Mapa de clases de la reorganización

Base: Develop `9a7bd05bc0c555bfaab7291342ed854660f8094d`. Rutas relativas a `src/main/java/`.

## Clases trasladadas

| Ubicación anterior | Ubicación actual |
|---|---|
| `ec/edu/epn/huertos/actividades/Actividad.java` | `ec/edu/epn/huertos/actividades/dominio/Actividad.java` |
| `ec/edu/epn/huertos/actividades/ActividadParticipante.java` | `ec/edu/epn/huertos/actividades/dominio/ActividadParticipante.java` |
| `ec/edu/epn/huertos/actividades/ActividadParticipanteRepository.java` | `ec/edu/epn/huertos/actividades/persistencia/ActividadParticipanteRepository.java` |
| `ec/edu/epn/huertos/actividades/ActividadRepository.java` | `ec/edu/epn/huertos/actividades/persistencia/ActividadRepository.java` |
| `ec/edu/epn/huertos/actividades/ActividadResumen.java` | `ec/edu/epn/huertos/actividades/aplicacion/ActividadResumen.java` |
| `ec/edu/epn/huertos/actividades/ActividadServicio.java` | `ec/edu/epn/huertos/actividades/aplicacion/ActividadServicio.java` |
| `ec/edu/epn/huertos/actividades/DetalleActividad.java` | `ec/edu/epn/huertos/actividades/aplicacion/DetalleActividad.java` |
| `ec/edu/epn/huertos/actividades/DetalleActividadServicio.java` | `ec/edu/epn/huertos/actividades/aplicacion/DetalleActividadServicio.java` |
| `ec/edu/epn/huertos/actividades/FormularioActividad.java` | `ec/edu/epn/huertos/actividades/aplicacion/FormularioActividad.java` |
| `ec/edu/epn/huertos/actividades/GestorActividadController.java` | `ec/edu/epn/huertos/actividades/presentacion/GestorActividadController.java` |
| `ec/edu/epn/huertos/actividades/ParticipanteController.java` | `ec/edu/epn/huertos/actividades/presentacion/ParticipanteController.java` |
| `ec/edu/epn/huertos/actividades/ParticipanteServicio.java` | `ec/edu/epn/huertos/actividades/aplicacion/ParticipanteServicio.java` |
| `ec/edu/epn/huertos/actividades/ParticipanteVista.java` | `ec/edu/epn/huertos/actividades/aplicacion/ParticipanteVista.java` |
| `ec/edu/epn/huertos/actividades/TipoActividad.java` | `ec/edu/epn/huertos/actividades/dominio/TipoActividad.java` |
| `ec/edu/epn/huertos/autenticacion/AutenticacionController.java` | `ec/edu/epn/huertos/autenticacion/presentacion/AutenticacionController.java` |
| `ec/edu/epn/huertos/autenticacion/AutorizacionServicio.java` | `ec/edu/epn/huertos/autenticacion/aplicacion/AutorizacionServicio.java` |
| `ec/edu/epn/huertos/autenticacion/FormularioRegistroCuenta.java` | `ec/edu/epn/huertos/autenticacion/aplicacion/FormularioRegistroCuenta.java` |
| `ec/edu/epn/huertos/autenticacion/ProvisionAdminTecnicoInicial.java` | `ec/edu/epn/huertos/autenticacion/aplicacion/ProvisionAdminTecnicoInicial.java` |
| `ec/edu/epn/huertos/autenticacion/RegistroCuentaController.java` | `ec/edu/epn/huertos/autenticacion/presentacion/RegistroCuentaController.java` |
| `ec/edu/epn/huertos/autenticacion/RegistroCuentaServicio.java` | `ec/edu/epn/huertos/autenticacion/aplicacion/RegistroCuentaServicio.java` |
| `ec/edu/epn/huertos/autenticacion/Rol.java` | `ec/edu/epn/huertos/autenticacion/dominio/Rol.java` |
| `ec/edu/epn/huertos/autenticacion/Usuario.java` | `ec/edu/epn/huertos/autenticacion/dominio/Usuario.java` |
| `ec/edu/epn/huertos/autenticacion/UsuarioAutenticado.java` | `ec/edu/epn/huertos/autenticacion/aplicacion/UsuarioAutenticado.java` |
| `ec/edu/epn/huertos/autenticacion/UsuarioDetailsService.java` | `ec/edu/epn/huertos/autenticacion/aplicacion/UsuarioDetailsService.java` |
| `ec/edu/epn/huertos/autenticacion/UsuarioRepository.java` | `ec/edu/epn/huertos/autenticacion/persistencia/UsuarioRepository.java` |
| `ec/edu/epn/huertos/bodega/ArticuloBodega.java` | `ec/edu/epn/huertos/bodega/dominio/ArticuloBodega.java` |
| `ec/edu/epn/huertos/bodega/ArticuloBodegaRepository.java` | `ec/edu/epn/huertos/bodega/persistencia/ArticuloBodegaRepository.java` |
| `ec/edu/epn/huertos/bodega/BodegaController.java` | `ec/edu/epn/huertos/bodega/presentacion/BodegaController.java` |
| `ec/edu/epn/huertos/bodega/BodegaServicio.java` | `ec/edu/epn/huertos/bodega/aplicacion/BodegaServicio.java` |
| `ec/edu/epn/huertos/bodega/CategoriaArticulo.java` | `ec/edu/epn/huertos/bodega/dominio/CategoriaArticulo.java` |
| `ec/edu/epn/huertos/bodega/FormularioArticulo.java` | `ec/edu/epn/huertos/bodega/aplicacion/FormularioArticulo.java` |
| `ec/edu/epn/huertos/compartido/ConflictoException.java` | `ec/edu/epn/huertos/compartido/dominio/ConflictoException.java` |
| `ec/edu/epn/huertos/compartido/Fechas.java` | `ec/edu/epn/huertos/compartido/dominio/Fechas.java` |
| `ec/edu/epn/huertos/compartido/ManejadorErrores.java` | `ec/edu/epn/huertos/compartido/presentacion/ManejadorErrores.java` |
| `ec/edu/epn/huertos/compartido/RecursoNoEncontradoException.java` | `ec/edu/epn/huertos/compartido/dominio/RecursoNoEncontradoException.java` |
| `ec/edu/epn/huertos/compartido/ReglaNegocioException.java` | `ec/edu/epn/huertos/compartido/dominio/ReglaNegocioException.java` |
| `ec/edu/epn/huertos/compartido/SeguridadConfig.java` | `ec/edu/epn/huertos/compartido/configuracion/SeguridadConfig.java` |
| `ec/edu/epn/huertos/comunidades/AdminTecnicoController.java` | `ec/edu/epn/huertos/comunidades/presentacion/AdminTecnicoController.java` |
| `ec/edu/epn/huertos/comunidades/AdminTecnicoServicio.java` | `ec/edu/epn/huertos/comunidades/aplicacion/AdminTecnicoServicio.java` |
| `ec/edu/epn/huertos/comunidades/AltaComunidadController.java` | `ec/edu/epn/huertos/comunidades/presentacion/AltaComunidadController.java` |
| `ec/edu/epn/huertos/comunidades/AltaComunidadServicio.java` | `ec/edu/epn/huertos/comunidades/aplicacion/AltaComunidadServicio.java` |
| `ec/edu/epn/huertos/comunidades/AsignacionRevisor.java` | `ec/edu/epn/huertos/comunidades/dominio/AsignacionRevisor.java` |
| `ec/edu/epn/huertos/comunidades/AsignacionRevisorRepository.java` | `ec/edu/epn/huertos/comunidades/persistencia/AsignacionRevisorRepository.java` |
| `ec/edu/epn/huertos/comunidades/AsignacionRevisorServicio.java` | `ec/edu/epn/huertos/comunidades/aplicacion/AsignacionRevisorServicio.java` |
| `ec/edu/epn/huertos/comunidades/AsignacionTecnicaVista.java` | `ec/edu/epn/huertos/comunidades/aplicacion/AsignacionTecnicaVista.java` |
| `ec/edu/epn/huertos/comunidades/Comunidad.java` | `ec/edu/epn/huertos/comunidades/dominio/Comunidad.java` |
| `ec/edu/epn/huertos/comunidades/ComunidadArchivadaVista.java` | `ec/edu/epn/huertos/comunidades/aplicacion/ComunidadArchivadaVista.java` |
| `ec/edu/epn/huertos/comunidades/ComunidadAsignadaVista.java` | `ec/edu/epn/huertos/comunidades/aplicacion/ComunidadAsignadaVista.java` |
| `ec/edu/epn/huertos/comunidades/ComunidadRepository.java` | `ec/edu/epn/huertos/comunidades/persistencia/ComunidadRepository.java` |
| `ec/edu/epn/huertos/comunidades/ComunidadServicio.java` | `ec/edu/epn/huertos/comunidades/aplicacion/ComunidadServicio.java` |
| `ec/edu/epn/huertos/comunidades/CuentaArchivadasController.java` | `ec/edu/epn/huertos/comunidades/presentacion/CuentaArchivadasController.java` |
| `ec/edu/epn/huertos/comunidades/EstadoInvitacion.java` | `ec/edu/epn/huertos/comunidades/dominio/EstadoInvitacion.java` |
| `ec/edu/epn/huertos/comunidades/EstadoSolicitudComunidad.java` | `ec/edu/epn/huertos/comunidades/dominio/EstadoSolicitudComunidad.java` |
| `ec/edu/epn/huertos/comunidades/FormularioAltaComunidad.java` | `ec/edu/epn/huertos/comunidades/aplicacion/FormularioAltaComunidad.java` |
| `ec/edu/epn/huertos/comunidades/FormularioInvitacion.java` | `ec/edu/epn/huertos/comunidades/aplicacion/FormularioInvitacion.java` |
| `ec/edu/epn/huertos/comunidades/FormularioRechazoSolicitud.java` | `ec/edu/epn/huertos/comunidades/aplicacion/FormularioRechazoSolicitud.java` |
| `ec/edu/epn/huertos/comunidades/FormularioSolicitudEliminacion.java` | `ec/edu/epn/huertos/comunidades/aplicacion/FormularioSolicitudEliminacion.java` |
| `ec/edu/epn/huertos/comunidades/GestorPanelController.java` | `ec/edu/epn/huertos/comunidades/presentacion/GestorPanelController.java` |
| `ec/edu/epn/huertos/comunidades/HistorialAsignacionRevisor.java` | `ec/edu/epn/huertos/comunidades/dominio/HistorialAsignacionRevisor.java` |
| `ec/edu/epn/huertos/comunidades/HistorialAsignacionRevisorRepository.java` | `ec/edu/epn/huertos/comunidades/persistencia/HistorialAsignacionRevisorRepository.java` |
| `ec/edu/epn/huertos/comunidades/Integrante.java` | `ec/edu/epn/huertos/comunidades/dominio/Integrante.java` |
| `ec/edu/epn/huertos/comunidades/IntegranteRepository.java` | `ec/edu/epn/huertos/comunidades/persistencia/IntegranteRepository.java` |
| `ec/edu/epn/huertos/comunidades/IntegranteVista.java` | `ec/edu/epn/huertos/comunidades/aplicacion/IntegranteVista.java` |
| `ec/edu/epn/huertos/comunidades/InvitacionComunidad.java` | `ec/edu/epn/huertos/comunidades/dominio/InvitacionComunidad.java` |
| `ec/edu/epn/huertos/comunidades/InvitacionComunidadController.java` | `ec/edu/epn/huertos/comunidades/presentacion/InvitacionComunidadController.java` |
| `ec/edu/epn/huertos/comunidades/InvitacionRepository.java` | `ec/edu/epn/huertos/comunidades/persistencia/InvitacionRepository.java` |
| `ec/edu/epn/huertos/comunidades/InvitacionVista.java` | `ec/edu/epn/huertos/comunidades/aplicacion/InvitacionVista.java` |
| `ec/edu/epn/huertos/comunidades/SolicitudComunidad.java` | `ec/edu/epn/huertos/comunidades/dominio/SolicitudComunidad.java` |
| `ec/edu/epn/huertos/comunidades/SolicitudComunidadRepository.java` | `ec/edu/epn/huertos/comunidades/persistencia/SolicitudComunidadRepository.java` |
| `ec/edu/epn/huertos/comunidades/SolicitudComunidadServicio.java` | `ec/edu/epn/huertos/comunidades/aplicacion/SolicitudComunidadServicio.java` |
| `ec/edu/epn/huertos/comunidades/SolicitudComunidadVista.java` | `ec/edu/epn/huertos/comunidades/aplicacion/SolicitudComunidadVista.java` |
| `ec/edu/epn/huertos/comunidades/SolicitudesComunidadController.java` | `ec/edu/epn/huertos/comunidades/presentacion/SolicitudesComunidadController.java` |
| `ec/edu/epn/huertos/comunidades/TipoInvitacion.java` | `ec/edu/epn/huertos/comunidades/dominio/TipoInvitacion.java` |
| `ec/edu/epn/huertos/comunidades/TipoSolicitudComunidad.java` | `ec/edu/epn/huertos/comunidades/dominio/TipoSolicitudComunidad.java` |
| `ec/edu/epn/huertos/demo/DatosDemo.java` | `ec/edu/epn/huertos/demo/aplicacion/DatosDemo.java` |
| `ec/edu/epn/huertos/evidencias/AlmacenamientoArchivos.java` | `ec/edu/epn/huertos/evidencias/dominio/AlmacenamientoArchivos.java` |
| `ec/edu/epn/huertos/evidencias/AlmacenamientoLocal.java` | `ec/edu/epn/huertos/evidencias/persistencia/AlmacenamientoLocal.java` |
| `ec/edu/epn/huertos/evidencias/ArchivoBorradoPendiente.java` | `ec/edu/epn/huertos/evidencias/dominio/ArchivoBorradoPendiente.java` |
| `ec/edu/epn/huertos/evidencias/ArchivoBorradoPendienteRepository.java` | `ec/edu/epn/huertos/evidencias/persistencia/ArchivoBorradoPendienteRepository.java` |
| `ec/edu/epn/huertos/evidencias/EliminacionComunidadServicio.java` | `ec/edu/epn/huertos/comunidades/aplicacion/EliminacionComunidadServicio.java` |
| `ec/edu/epn/huertos/evidencias/EnvioEvidencia.java` | `ec/edu/epn/huertos/evidencias/dominio/EnvioEvidencia.java` |
| `ec/edu/epn/huertos/evidencias/EnvioEvidenciaRepository.java` | `ec/edu/epn/huertos/evidencias/persistencia/EnvioEvidenciaRepository.java` |
| `ec/edu/epn/huertos/evidencias/EstadoRevision.java` | `ec/edu/epn/huertos/evidencias/dominio/EstadoRevision.java` |
| `ec/edu/epn/huertos/evidencias/EvidenciaController.java` | `ec/edu/epn/huertos/evidencias/presentacion/EvidenciaController.java` |
| `ec/edu/epn/huertos/evidencias/EvidenciaServicio.java` | `ec/edu/epn/huertos/evidencias/aplicacion/EvidenciaServicio.java` |
| `ec/edu/epn/huertos/evidencias/ImagenValidada.java` | `ec/edu/epn/huertos/evidencias/dominio/ImagenValidada.java` |
| `ec/edu/epn/huertos/evidencias/LimpiezaArchivosBorrados.java` | `ec/edu/epn/huertos/evidencias/aplicacion/LimpiezaArchivosBorrados.java` |
| `ec/edu/epn/huertos/evidencias/PropiedadesAlmacenamiento.java` | `ec/edu/epn/huertos/evidencias/configuracion/PropiedadesAlmacenamiento.java` |
| `ec/edu/epn/huertos/evidencias/Revision.java` | `ec/edu/epn/huertos/evidencias/dominio/Revision.java` |
| `ec/edu/epn/huertos/evidencias/RevisionController.java` | `ec/edu/epn/huertos/evidencias/presentacion/RevisionController.java` |
| `ec/edu/epn/huertos/evidencias/RevisionRepository.java` | `ec/edu/epn/huertos/evidencias/persistencia/RevisionRepository.java` |
| `ec/edu/epn/huertos/evidencias/RevisionServicio.java` | `ec/edu/epn/huertos/evidencias/aplicacion/RevisionServicio.java` |
| `ec/edu/epn/huertos/evidencias/ValidadorImagen.java` | `ec/edu/epn/huertos/evidencias/aplicacion/ValidadorImagen.java` |
| `ec/edu/epn/huertos/evidencias/ValidadorVideo.java` | `ec/edu/epn/huertos/evidencias/aplicacion/ValidadorVideo.java` |
| `ec/edu/epn/huertos/evidencias/VideoValidado.java` | `ec/edu/epn/huertos/evidencias/dominio/VideoValidado.java` |
| `ec/edu/epn/huertos/gamificacion/FilaRanking.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/FilaRanking.java` |
| `ec/edu/epn/huertos/gamificacion/GamificacionServicio.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/GamificacionServicio.java` |
| `ec/edu/epn/huertos/gamificacion/Nivel.java` | `ec/edu/epn/huertos/gamificacion/dominio/Nivel.java` |
| `ec/edu/epn/huertos/gamificacion/PosicionRanking.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/PosicionRanking.java` |
| `ec/edu/epn/huertos/gamificacion/ProgresoHuerto.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/ProgresoHuerto.java` |
| `ec/edu/epn/huertos/gamificacion/PropiedadesGamificacion.java` | `ec/edu/epn/huertos/gamificacion/configuracion/PropiedadesGamificacion.java` |
| `ec/edu/epn/huertos/gamificacion/PuntosComunidad.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/PuntosComunidad.java` |
| `ec/edu/epn/huertos/gamificacion/RecompensaComunitaria.java` | `ec/edu/epn/huertos/gamificacion/dominio/RecompensaComunitaria.java` |
| `ec/edu/epn/huertos/gamificacion/RecompensaComunitariaRepository.java` | `ec/edu/epn/huertos/gamificacion/persistencia/RecompensaComunitariaRepository.java` |
| `ec/edu/epn/huertos/gamificacion/RecompensaIndividual.java` | `ec/edu/epn/huertos/gamificacion/dominio/RecompensaIndividual.java` |
| `ec/edu/epn/huertos/gamificacion/RecompensaIndividualRepository.java` | `ec/edu/epn/huertos/gamificacion/persistencia/RecompensaIndividualRepository.java` |
| `ec/edu/epn/huertos/gamificacion/ResumenPuntos.java` | `ec/edu/epn/huertos/gamificacion/aplicacion/ResumenPuntos.java` |
| `ec/edu/epn/huertos/huertos/CatalogoController.java` | `ec/edu/epn/huertos/huertos/presentacion/CatalogoController.java` |
| `ec/edu/epn/huertos/huertos/CatalogoServicio.java` | `ec/edu/epn/huertos/huertos/aplicacion/CatalogoServicio.java` |
| `ec/edu/epn/huertos/huertos/CicloPlanta.java` | `ec/edu/epn/huertos/huertos/dominio/CicloPlanta.java` |
| `ec/edu/epn/huertos/huertos/CualidadPlanta.java` | `ec/edu/epn/huertos/huertos/dominio/CualidadPlanta.java` |
| `ec/edu/epn/huertos/huertos/EspacioHuerto.java` | `ec/edu/epn/huertos/huertos/dominio/EspacioHuerto.java` |
| `ec/edu/epn/huertos/huertos/EspacioHuertoRepository.java` | `ec/edu/epn/huertos/huertos/persistencia/EspacioHuertoRepository.java` |
| `ec/edu/epn/huertos/huertos/EspacioHuertoServicio.java` | `ec/edu/epn/huertos/huertos/aplicacion/EspacioHuertoServicio.java` |
| `ec/edu/epn/huertos/huertos/Especie.java` | `ec/edu/epn/huertos/huertos/dominio/Especie.java` |
| `ec/edu/epn/huertos/huertos/EspecieRepository.java` | `ec/edu/epn/huertos/huertos/persistencia/EspecieRepository.java` |
| `ec/edu/epn/huertos/huertos/FichaEspecie.java` | `ec/edu/epn/huertos/huertos/aplicacion/FichaEspecie.java` |
| `ec/edu/epn/huertos/huertos/FormularioHuerto.java` | `ec/edu/epn/huertos/huertos/aplicacion/FormularioHuerto.java` |
| `ec/edu/epn/huertos/huertos/FormularioPlanta.java` | `ec/edu/epn/huertos/huertos/aplicacion/FormularioPlanta.java` |
| `ec/edu/epn/huertos/huertos/GestorHuertoController.java` | `ec/edu/epn/huertos/huertos/presentacion/GestorHuertoController.java` |
| `ec/edu/epn/huertos/huertos/Huerto.java` | `ec/edu/epn/huertos/huertos/dominio/Huerto.java` |
| `ec/edu/epn/huertos/huertos/HuertoCerramiento.java` | `ec/edu/epn/huertos/huertos/dominio/HuertoCerramiento.java` |
| `ec/edu/epn/huertos/huertos/HuertoCerramientoRepository.java` | `ec/edu/epn/huertos/huertos/persistencia/HuertoCerramientoRepository.java` |
| `ec/edu/epn/huertos/huertos/HuertoCerramientoServicio.java` | `ec/edu/epn/huertos/huertos/aplicacion/HuertoCerramientoServicio.java` |
| `ec/edu/epn/huertos/huertos/HuertoRepository.java` | `ec/edu/epn/huertos/huertos/persistencia/HuertoRepository.java` |
| `ec/edu/epn/huertos/huertos/HuertoServicio.java` | `ec/edu/epn/huertos/huertos/aplicacion/HuertoServicio.java` |
| `ec/edu/epn/huertos/huertos/HuertoVirtual.java` | `ec/edu/epn/huertos/huertos/aplicacion/HuertoVirtual.java` |
| `ec/edu/epn/huertos/huertos/HuertoVirtualServicio.java` | `ec/edu/epn/huertos/huertos/aplicacion/HuertoVirtualServicio.java` |
| `ec/edu/epn/huertos/huertos/Planta.java` | `ec/edu/epn/huertos/huertos/dominio/Planta.java` |
| `ec/edu/epn/huertos/huertos/PlantaPlantadaVista.java` | `ec/edu/epn/huertos/huertos/aplicacion/PlantaPlantadaVista.java` |
| `ec/edu/epn/huertos/huertos/PlantaRepository.java` | `ec/edu/epn/huertos/huertos/persistencia/PlantaRepository.java` |
| `ec/edu/epn/huertos/huertos/PlantaVirtualLectura.java` | `ec/edu/epn/huertos/huertos/persistencia/PlantaVirtualLectura.java` |
| `ec/edu/epn/huertos/huertos/RegionPlanta.java` | `ec/edu/epn/huertos/huertos/dominio/RegionPlanta.java` |
| `ec/edu/epn/huertos/huertos/SiembraPlanta.java` | `ec/edu/epn/huertos/huertos/aplicacion/SiembraPlanta.java` |
| `ec/edu/epn/huertos/huertos/SpritePlanta.java` | `ec/edu/epn/huertos/huertos/aplicacion/SpritePlanta.java` |
| `ec/edu/epn/huertos/misiones/EstadoMision.java` | `ec/edu/epn/huertos/misiones/dominio/EstadoMision.java` |
| `ec/edu/epn/huertos/misiones/MisionHuerto.java` | `ec/edu/epn/huertos/misiones/dominio/MisionHuerto.java` |
| `ec/edu/epn/huertos/misiones/MisionHuertoRepository.java` | `ec/edu/epn/huertos/misiones/persistencia/MisionHuertoRepository.java` |
| `ec/edu/epn/huertos/misiones/MisionPlantilla.java` | `ec/edu/epn/huertos/misiones/dominio/MisionPlantilla.java` |
| `ec/edu/epn/huertos/misiones/MisionPlantillaRepository.java` | `ec/edu/epn/huertos/misiones/persistencia/MisionPlantillaRepository.java` |
| `ec/edu/epn/huertos/misiones/MisionServicio.java` | `ec/edu/epn/huertos/misiones/aplicacion/MisionServicio.java` |
| `ec/edu/epn/huertos/misiones/RecurrenciaMision.java` | `ec/edu/epn/huertos/misiones/dominio/RecurrenciaMision.java` |
| `ec/edu/epn/huertos/publico/PublicoController.java` | `ec/edu/epn/huertos/publico/presentacion/PublicoController.java` |
| `ec/edu/epn/huertos/publico/PublicoServicio.java` | `ec/edu/epn/huertos/publico/aplicacion/PublicoServicio.java` |

## Clases incorporadas

- `src/main/java/ec/edu/epn/huertos/actividades/aplicacion/ActividadVista.java`
- `src/main/java/ec/edu/epn/huertos/actividades/aplicacion/ConsultaActividadServicio.java`
- `src/main/java/ec/edu/epn/huertos/autenticacion/aplicacion/SesionServicio.java`
- `src/main/java/ec/edu/epn/huertos/bodega/aplicacion/ArticuloVista.java`
- `src/main/java/ec/edu/epn/huertos/comunidades/aplicacion/ComunidadVista.java`
- `src/main/java/ec/edu/epn/huertos/comunidades/aplicacion/PertenenciaVista.java`
- `src/main/java/ec/edu/epn/huertos/comunidades/persistencia/AsignacionBloqueo.java`
- `src/main/java/ec/edu/epn/huertos/comunidades/persistencia/EliminacionComunidadRepository.java`
- `src/main/java/ec/edu/epn/huertos/huertos/aplicacion/EspacioVista.java`
- `src/main/java/ec/edu/epn/huertos/huertos/aplicacion/EspecieVista.java`
- `src/main/java/ec/edu/epn/huertos/huertos/aplicacion/HuertosConsultaServicio.java`
- `src/main/java/ec/edu/epn/huertos/huertos/aplicacion/HuertoVista.java`
- `src/main/java/ec/edu/epn/huertos/huertos/aplicacion/PlantaVista.java`
- `src/main/java/ec/edu/epn/huertos/huertos/dominio/DatosEspecie.java`
- `src/main/java/ec/edu/epn/huertos/huertos/persistencia/FotoCatalogoInicialRepository.java`
- `src/main/java/ec/edu/epn/huertos/misiones/persistencia/MisionDependenciaRepository.java`
