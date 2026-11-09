package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.ConflictoException;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión del huerto y sus plantas, siempre acotada a la comunidad del gestor autenticado. */
@Service
public class HuertoServicio {

    private final ComunidadServicio comunidades;
    private final HuertoRepository huertos;
    private final PlantaRepository plantas;
    private final EspacioHuertoServicio espacios;
    private final HuertoCerramientoServicio cerramientos;

    public HuertoServicio(ComunidadServicio comunidades, HuertoRepository huertos, PlantaRepository plantas,
                          EspacioHuertoServicio espacios, HuertoCerramientoServicio cerramientos) {
        this.comunidades = comunidades;
        this.huertos = huertos;
        this.plantas = plantas;
        this.espacios = espacios;
        this.cerramientos = cerramientos;
    }

    @Transactional(readOnly = true)
    public Huerto huertoDeGestor(Long gestorUsuarioId) {
        Long comunidadId = comunidades.gestorDe(gestorUsuarioId).getComunidadId();
        return huertos.findByComunidadId(comunidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tu comunidad aún no tiene un huerto registrado."));
    }

    @Transactional(readOnly = true)
    public ec.edu.epn.huertos.comunidades.Comunidad comunidadDeGestor(Long gestorUsuarioId) {
        return comunidades.comunidad(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
    }

    @Transactional(readOnly = true)
    public List<EspacioHuerto> espaciosDeGestor(Long gestorUsuarioId) {
        return espacios.activos(huertoDeGestor(gestorUsuarioId).getId());
    }

    @Transactional(readOnly = true)
    public List<String> gruposCerramientoDeGestor(Long gestorUsuarioId) {
        return cerramientos.grupos(huertoDeGestor(gestorUsuarioId).getId());
    }

    @Transactional
    public void actualizarHuerto(Long gestorUsuarioId, FormularioHuerto form) {
        Long comunidadId = comunidades.gestorDe(gestorUsuarioId).getComunidadId();
        comunidades.exigirComunidadOperativa(comunidadId);
        ec.edu.epn.huertos.comunidades.Comunidad comunidad = comunidades.comunidad(comunidadId);
        if (!java.util.List.of("TIERRA_SURCOS", "TIERRA_CAMADAS", "TIERRA_CAMELLONES", "TIERRA_TERRAZAS", "VERTICAL_PAREDES", "COLGANTE_TECHOS", "PISO_MACETAS", "PISO_BOTELLAS_PATIO", "PISO_LLANTAS_PATIO", "PISO_TERRAZA", "PISO_GRADAS", "PISO_BALCONES", "PISO_FILOS_VENTANA").contains(form.getDiseno())
                || !java.util.List.of("ARCILLOSO", "LIMOSO", "ARENOSO", "MARGOSO", "GREDOSO", "PANTANOSO").contains(form.getTipoSuelo())
                || !java.util.List.of("BARBECHO", "RESTOS_VEGETALES", "HOJA", "COMPOST").contains(form.getAbonoNatural())
                || !HuertoCerramientoServicio.GRUPOS.containsAll(form.getGruposCerramiento() == null ? java.util.List.of() : form.getGruposCerramiento()))
            throw new ec.edu.epn.huertos.compartido.ReglaNegocioException("Selecciona opciones válidas de diseño, suelo, abono y cerramiento.");
        if (!java.util.List.of("PERSONA", "FAMILIA", "CONDOMINIO", "BARRIO").contains(form.getTipoComunidad()))
            throw new ec.edu.epn.huertos.compartido.ReglaNegocioException("Selecciona un tipo de comunidad válido.");
        if (!comunidad.getNombre().equalsIgnoreCase(form.getNombreComunidad().trim())
                && comunidades.existeNombre(form.getNombreComunidad().trim()))
            throw new ConflictoException("Ya existe una comunidad con ese nombre.");
        comunidad.actualizarDatos(form.getNombreComunidad().trim(), form.getDescripcionComunidad().trim(),
                form.getTipoComunidad(), form.getProvinciaComunidad().trim(), form.getCantonComunidad().trim(),
                form.getDireccionComunidad().trim(), form.getLatitudComunidad(), form.getLongitudComunidad());
        huertoDeGestor(gestorUsuarioId).actualizar(form.getNombre().trim(), form.getDescripcion().trim(),
                form.getProvincia().trim(), form.getParroquia().trim(), form.getDireccion().trim(), form.getAreaM2(),
                form.getDiseno(), form.getTipoSuelo(), form.getFuenteAgua().trim(), form.getAbonoNatural().trim(),
                form.getLatitud(), form.getLongitud());
        espacios.sincronizar(huertoDeGestor(gestorUsuarioId).getId(), form.getAreaM2(), form.getDisenos());
        cerramientos.sincronizar(huertoDeGestor(gestorUsuarioId).getId(), form.getGruposCerramiento());
    }

    /**
     * Huerto del gestor exigiendo que su comunidad esté operativa (no archivada ni con eliminación pendiente).
     * Para módulos que modifican datos del huerto sin acceder a ComunidadServicio (p. ej., bodega).
     */
    @Transactional(readOnly = true)
    public Long huertoOperativoDeGestor(Long gestorUsuarioId) {
        Long huertoId = huertoDeGestor(gestorUsuarioId).getId();
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        return huertoId;
    }

    @Transactional(readOnly = true)
    public List<Planta> plantasDeGestor(Long gestorUsuarioId) {
        return plantas.findByHuertoIdOrderByActivaDescNombreComunAsc(huertoDeGestor(gestorUsuarioId).getId());
    }

    @Transactional(readOnly = true)
    public List<Planta> plantasPlantadasDeGestor(Long gestorUsuarioId) {
        return plantas.findByHuertoIdAndPlantadaTrueOrderByNombreComunAsc(huertoDeGestor(gestorUsuarioId).getId());
    }

    @Transactional(readOnly = true)
    public Planta plantaDeGestor(Long gestorUsuarioId, Long plantaId) {
        Huerto huerto = huertoDeGestor(gestorUsuarioId);
        Planta planta = plantas.findById(plantaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La planta no existe."));
        if (!planta.getHuertoId().equals(huerto.getId())) {
            throw new AccessDeniedException("La planta pertenece a otro huerto");
        }
        return planta;
    }

    @Transactional
    public Planta crearPlanta(Long gestorUsuarioId, FormularioPlanta form) {
        Huerto huerto = huertoDeGestor(gestorUsuarioId);
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        Planta planta = new Planta(huerto.getId(), form.getNombreComun().trim(), form.getNombreCientifico(), form.getDescripcion(), form.getCantidad());
        planta.cambiarSiembra(form.getCantidad(), form.isPlantada());
        return plantas.save(planta);
    }

    @Transactional
    public void actualizarPlanta(Long gestorUsuarioId, Long plantaId, FormularioPlanta form) {
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        Planta planta = plantaDeGestor(gestorUsuarioId, plantaId);
        planta.actualizar(form.getNombreComun().trim(), form.getNombreCientifico(), form.getDescripcion(), form.getCantidad());
        // No se renombra la especie: es del catálogo común y la comparten todas las comunidades.
        planta.cambiarSiembra(form.getCantidad(), form.isPlantada());
    }

    @Transactional
    public void cambiarEstadoPlanta(Long gestorUsuarioId, Long plantaId, boolean activa) {
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        plantaDeGestor(gestorUsuarioId, plantaId).cambiarActiva(activa);
    }

    @Transactional
    public void cambiarPlantada(Long gestorUsuarioId, Long plantaId, boolean plantada) {
        // «plantada» es el estado real y no se limita; el tope de dos es solo del dibujo (HuertoVirtualServicio).
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        plantaDeGestor(gestorUsuarioId, plantaId).cambiarPlantada(plantada);
    }

    @Transactional
    public void eliminarPlanta(Long gestorUsuarioId, Long plantaId) {
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        plantas.delete(plantaDeGestor(gestorUsuarioId, plantaId));
    }

    @Transactional(readOnly = true)
    public List<Planta> plantasActivasPublicas(Long huertoId) {
        return plantas.findByHuertoIdAndActivaTrueOrderByNombreComunAsc(huertoId);
    }
    @Transactional
    public Planta seleccionarEspecie(Long usuarioId, Especie especie, int cantidad, boolean plantada) {
        Long huertoId = huertoDeGestor(usuarioId).getId();
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(usuarioId).getComunidadId());
        // Serializa selecciones de la misma comunidad para no duplicar una especie.
        plantas.bloquearHuerto(huertoId);
        if (plantas.existsByHuertoIdAndEspecieId(huertoId, especie.getId()))
            throw new ec.edu.epn.huertos.compartido.ConflictoException("Esta especie ya está en tu lista. Edita su cantidad o siembra.");
        return plantas.save(new Planta(huertoId, especie, cantidad, plantada));
    }
    @Transactional
    public void actualizarSiembra(Long usuarioId, Long id, SiembraPlanta form) {
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(usuarioId).getComunidadId());
        plantaDeGestor(usuarioId, id).cambiarSiembra(form.getCantidad(), form.isPlantada());
    }
}
