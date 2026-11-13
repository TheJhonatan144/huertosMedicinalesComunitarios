# Trazabilidad

Cadena: **requisito o referencia provisional → historia de usuario → criterio de aceptación → componente → prueba → resultado observado**.

> **Vinculación con RF/RNF/RB: pendiente.** La matriz de requisitos no estaba disponible ([pendientes](pendientes.md)). Los identificadores `HU-P` y `CA-P`
> son **temporales**. Solo se anotan las referencias que el encargo menciona explícitamente (RB05, RB06, RB09, RF10, RF24, RF25).
> «Resultado observado» refleja lo ejecutado el 2026-10-23 (ver [pruebas](pruebas.md)); lo que no se ejecutó se indica como tal.

## Historias de usuario provisionales

| ID | Historia |
|---|---|
| HU-P01 | Como usuario con cuenta quiero iniciar y cerrar sesión para acceder a mis funciones |
| HU-P02 | Como equipo quiero permisos por rol y aislamiento entre comunidades para proteger la información |
| HU-P03 | Como gestor quiero administrar integrantes activos (máx. 25) y los datos de mi huerto |
| HU-P04 | Como visitante quiero consultar el mapa, la comunidad y el huerto sin cuenta |
| HU-P05 | Como gestor quiero registrar las plantas de mi huerto |
| HU-P06 | Como gestor quiero registrar una actividad con participantes activos de mi comunidad |
| HU-P07 | Como gestor quiero adjuntar una fotografía como evidencia, guardada de forma persistente y privada |
| HU-P08 | Como revisor asignado quiero una bandeja con las evidencias pendientes de mis comunidades |
| HU-P09 | Como revisor quiero aprobar o rechazar con observaciones, y como gestor corregir y reenviar |
| HU-P10 | Como participante quiero recibir puntos individuales y que la comunidad reciba puntos comunitarios solo si se aprueba |
| HU-P11 | Como visitante quiero ver el progreso visual del huerto virtual |
| HU-P12 | Como visitante quiero ver el ranking comunitario |
| HU-P13 | Como usuario autorizado quiero consultar el historial de envíos, revisiones y recompensas |

## Matriz

| Referencia | HU | Criterio de aceptación | Componente | Prueba (clase › método) | Resultado observado |
|---|---|---|---|---|---|
| (pendiente) | HU-P01 | CA-P01 Inicia sesión con credenciales válidas y rechaza las incorrectas; el logout y el login exigen CSRF; las contraseñas se guardan con hash | `SeguridadConfig`, `UsuarioDetailsService` | `SeguridadWebIT › elInicioDeSesionFuncionaYRechazaCredencialesIncorrectas`, `› lasContrasenasSeGuardanConHashNuncaEnClaro`, `› lasPeticionesQueModificanDatosExigenTokenCsrf` | Aprobada; además login real con curl y CSRF real |
| (pendiente) | HU-P02 | CA-P02 Cada rol solo accede a su área; el visitante es redirigido al login | `SeguridadConfig`, `AutorizacionServicio` | `SeguridadWebIT › cadaRolSoloAccedeASuPropiaArea`, `› elVisitanteNoPuedeEntrarAAreasPrivadasNiVerEvidencias` | Aprobada |
| (pendiente) | HU-P02 | CA-P03 Un gestor no ve ni modifica recursos de otra comunidad | `ActividadServicio`, `ComunidadServicio`, `HuertoServicio` | `SeguridadWebIT › ungestorNoVeNiModificaRecursosDeOtraComunidad`, `CupoComunidadIT › elGestorNoPuedeDarseDeBajaNiAdministrarOtraComunidad`, `PlantasIT › ungestorNoTocaPlantasDeOtroHuerto…` | Aprobada |
| Regla obligatoria (máx. 25) | HU-P03 | CA-P04 Nunca hay más de 25 activos (incl. gestor); una baja libera cupo; altas y reactivaciones concurrentes respetan el límite | `ComunidadServicio`, trigger `validar_cupo_comunidad` | `CupoComunidadIT` (7 pruebas: `labaseDeDatosRechazaElIntegranteNumero26`, `unaBajaLiberaCupo…`, `reactivacionesConcurrentes…`, `altasDirectasConcurrentes…`, …) | Aprobada, con PostgreSQL real y hilos concurrentes |
| (pendiente) | HU-P04 | CA-P05 El visitante consulta mapa, ficha, API y ranking; las fichas no exponen datos personales; el mapa tiene lista alternativa | `PublicoController`, `PublicoServicio`, `mapa.js` | `SeguridadWebIT › elVisitanteConsultaMapaComunidadRankingYApiPublica`, `› lasFichasPublicasNoExponenDatosPersonales`; recorrido curl | Aprobada (HTML/API). **Mapa Leaflet no verificado en navegador** |
| (pendiente) | HU-P05 | CA-P06 El gestor crea, edita y (des)activa plantas de su huerto; la ficha pública solo muestra activas; valida datos | `HuertoServicio`, `GestorHuertoController` | `PlantasIT` (3 pruebas) | Aprobada |
| Regla obligatoria (participantes) | HU-P06 | CA-P07 Solo participantes activos de la propia comunidad; al menos uno; fecha no futura; solo el gestor registra | `ActividadServicio.validarParticipantes` | `ActividadServicioIT › noPermiteSeleccionarIntegrantesDeOtraComunidad`, `› …DeBaja`, `› exigeAlMenosUnParticipante…`, `› rechazaFechasFuturas`, `› soloElGestorPuedeRegistrar` | Aprobada |
| Regla obligatoria (lista fija) | HU-P06 | CA-P08 La lista queda fijada: no se agrega, quita ni sustituye (ni por la BD); editar no la altera | trigger `validar_alta_participante`, `bloquear_modificacion` | `ActividadServicioIT › laListaDeParticipantesEsInmutableTambienEnLaBaseDeDatos`, `› labaseDeDatosImpideParticipantesDeOtraComunidad`, `› puedeEditarseMientrasEstaPendienteSinAlterarSusParticipantes` | Aprobada |
| Regla obligatoria (edición) | HU-P06 | CA-P09 Solo se edita mientras está pendiente | `ActividadServicio.editar` | `ActividadServicioIT › noPuedeEditarActividadesAjenasNiYaRevisadas` | Aprobada |
| (pendiente) | HU-P07 | CA-P10 Valida tamaño y contenido real (firma + decodificación), ignora extensión/tipo declarado, sanea el nombre | `ValidadorImagen` | `ValidadorImagenTest` (10 pruebas) | Aprobada |
| (pendiente) | HU-P07 | CA-P11 Nombre interno seguro, sin recorrido de rutas, fuera de recursos estáticos, persistente tras reinicio | `AlmacenamientoLocal` | `AlmacenamientoLocalTest` (4 pruebas); `ActividadServicioIT › registraUnaActividadPendiente…`; reinicio de contenedor manual | Aprobada; persistencia verificada con Docker (foto creada en contenedor sirve tras `restart`) |
| (pendiente) | HU-P07 | CA-P12 Las fotografías solo las ven el gestor de la comunidad y el revisor asignado; no hay acceso estático | `EvidenciaServicio`, `EvidenciaController` | `SeguridadWebIT › lasFotografiasSoloLasVenElGestorYElRevisorAsignado`, `› lasFotografiasNoSeSirvenComoRecursoEstatico` | Aprobada |
| (pendiente) | HU-P08 | CA-P13 La bandeja muestra solo pendientes de comunidades asignadas; otro revisor no las ve ni accede | `RevisionServicio.bandeja/detalle` | `RevisionServicioIT › soloUnRevisorAsignadoConRolPuedeDecidir`; `RecorridoPrincipalIT` | Aprobada |
| Regla obligatoria (autorrevisión) | HU-P08 | CA-P14 Nadie revisa lo que registró; también lo impide la BD | `RevisionServicio.actividadRevisable`, trigger `validar_revisor` | `RevisionServicioIT › nadiePuedeRevisarUnaActividadRegistradaPorSiMismo` | Aprobada |
| Regla obligatoria (rechazo) | HU-P09 | CA-P15 Rechazar exige observaciones (servicio y BD) | `RevisionServicio.decidir`, `CHECK` en `revision` | `RevisionServicioIT › rechazarExigeObservaciones…`, `› elHistorialDeRevisionesEsInmutable…` | Aprobada |
| Regla obligatoria (reenvío) | HU-P09 | CA-P16 Tras un rechazo se corrige y reenvía; se conservan envíos, revisiones, observaciones y archivos; el reenvío solo aplica a rechazadas | `ActividadServicio.reenviar` | `ActividadServicioIT › elReenvioSoloAplicaATrasUnRechazoYConservaElHistorial`, `RevisionServicioIT › rechazoYReenvioConservanElHistorial…`, `RecorridoPrincipalIT` | Aprobada |
| RB05, RB09 (aprobadas) · RF10 en contradicción | HU-P10 | CA-P17 Pendiente/rechazada: 0 puntos y 0 progreso; solo la aprobación los genera | `GamificacionServicio.otorgar`, triggers de recompensa | `ActividadServicioIT › unaActividadPendienteNoGeneraPuntosNiProgreso`, `RevisionServicioIT › rechazarExige…`, `› laBaseDeDatosRechazaRecompensas…` | Aprobada |
| Regla obligatoria (misma recompensa) | HU-P10 | CA-P18 Todos los seleccionados reciben lo mismo; los no seleccionados, nada | `GamificacionServicio.otorgar` | `RevisionServicioIT › aprobarOtorgaLaMismaRecompensa…` | Aprobada |
| Regla obligatoria (independencia) · RB06 pendiente | HU-P10 | CA-P19 Puntos individuales y comunitarios independientes; valores configurables de demostración | `PropiedadesGamificacion` | `RevisionServicioIT › puntosIndividualesYComunitariosSonIndependientes` | Aprobada (valores 10/20/5 son de **demostración**) |
| Regla obligatoria (sin duplicación) | HU-P10 | CA-P20 Dobles clics, reintentos, reenvíos y revisiones concurrentes no duplican recompensas ni decisiones | `RevisionServicio.decidir` (bloqueo + estado), `UNIQUE` en BD | `RevisionServicioIT › unaDecisionRepetida…`, `› aprobacionesConcurrentes…` (8 hilos, 2 revisores), `› aprobarYRechazarAlMismoTiempo…` (5 rondas), `› reenviosConcurrentes…`; recorrido curl (2.º intento rechazado) | Aprobada |
| Regla obligatoria (niveles) · RB06 pendiente | HU-P10 | CA-P21 Niveles Aprendiz/Oficial/Maestro por umbrales; Maestro sin tope | `GamificacionServicio.nivelPara` | `GamificacionReglasTest` | Aprobada (umbrales de demostración) |
| RB05 | HU-P11 | CA-P22 El progreso del huerto avanza por etapas solo con aprobaciones | `GamificacionServicio.progreso(Para)`, fragmento `progreso` | `GamificacionReglasTest › elProgresoDelHuertoAvanzaPorEtapas`, `RevisionServicioIT › puntosIndividualesYComunitarios…` | Aprobada (lógica); **apariencia visual no revisada en navegador** |
| (pendiente) | HU-P12 | CA-P23 El ranking ordena solo por puntos comunitarios; empates comparten posición | `GamificacionServicio.ranking` | `RevisionServicioIT › elRankingUsaSoloPuntosComunitariosYLosEmpatesComparten` | Aprobada |
| (pendiente) | HU-P13 | CA-P24 El gestor, el revisor y el participante consultan su historial (envíos, observaciones, puntos) | `DetalleActividadServicio`, `RevisionServicio.historial`, `ParticipanteServicio` | `RecorridoPrincipalIT` (detalle con observaciones y «Aprobada»; panel con 10 puntos) | Aprobada |
| Criterio de finalización 7 | — | CA-P25 Los datos y las fotos sobreviven a reiniciar la aplicación | Volúmenes Docker | Verificación manual con Docker (`restart app`) | Verificado manualmente; no automatizado |
| RF24 | — | Sin funcionalidad atribuida (en revisión) | — | — | No aplica |
| RF25 | — | Reservado | — | — | No aplica |
