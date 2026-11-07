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

    public HuertoServicio(ComunidadServicio comunidades, HuertoRepository huertos, PlantaRepository plantas) {
        this.comunidades = comunidades;
        this.huertos = huertos;
        this.plantas = plantas;
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

    @Transactional
    public void actualizarHuerto(Long gestorUsuarioId, FormularioHuerto form) {
        Long comunidadId = comunidades.gestorDe(gestorUsuarioId).getComunidadId();
        comunidades.exigirComunidadOperativa(comunidadId);
        ec.edu.epn.huertos.comunidades.Comunidad comunidad = comunidades.comunidad(comunidadId);
        if (!java.util.List.of("TIERRA_SURCOS", "TIERRA_CAMADAS", "TIERRA_CAMELLONES", "TIERRA_TERRAZAS", "VERTICAL_PAREDES", "COLGANTE_TECHOS", "PISO_MACETAS", "PISO_BOTELLAS_PATIO", "PISO_LLANTAS_PATIO", "PISO_TERRAZA", "PISO_GRADAS", "PISO_BALCONES", "PISO_FILOS_VENTANA").contains(form.getDiseno())
                || !java.util.List.of("ARCILLOSO", "LIMOSO", "ARENOSO", "MARGOSO", "GREDOSO", "PANTANOSO").contains(form.getTipoSuelo())
                || !java.util.List.of("BARBECHO", "RESTOS_VEGETALES", "HOJA", "COMPOST").contains(form.getAbonoNatural())
                || !java.util.List.of("VIVO_HIGUERILLA", "VIVO_FLORIPONDIO", "VIVO_AGAVE", "VIVO_LAVANDA", "VIVO_NARANJOS", "VIVO_CAPULIES", "VIVO_TOCTE", "VIVO_GUABO", "NATURAL_CANA_GUADUA", "NATURAL_BAHAREQUE", "NATURAL_TAPIAL", "RECICLADO_TABLAS", "RECICLADO_BOTELLAS", "RECICLADO_NEUMATICOS", "METALICO_ALAMBRE_PUAS", "CONVENCIONAL_PIEDRA", "CONVENCIONAL_LADRILLO", "CONVENCIONAL_BLOQUE").contains(form.getCerramiento()))
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
                form.getCerramiento(), form.getLatitud(), form.getLongitud());
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
        return plantas.save(new Planta(huerto.getId(), form.getNombreComun().trim(), form.getNombreCientifico(),
                form.getDescripcion(), form.getCantidad()));
    }

    @Transactional
    public void actualizarPlanta(Long gestorUsuarioId, Long plantaId, FormularioPlanta form) {
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        plantaDeGestor(gestorUsuarioId, plantaId)
                .actualizar(form.getNombreComun().trim(), form.getNombreCientifico(), form.getDescripcion(),
                        form.getCantidad());
    }

    @Transactional
    public void cambiarEstadoPlanta(Long gestorUsuarioId, Long plantaId, boolean activa) {
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        plantaDeGestor(gestorUsuarioId, plantaId).cambiarActiva(activa);
    }

    @Transactional
    public void cambiarPlantada(Long gestorUsuarioId, Long plantaId, boolean plantada) {
        Huerto huerto = huertoDeGestor(gestorUsuarioId);
        comunidades.exigirComunidadOperativa(comunidades.gestorDe(gestorUsuarioId).getComunidadId());
        huertos.bloquearPorId(huerto.getId()).orElseThrow();
        Planta planta = plantaDeGestor(gestorUsuarioId, plantaId);
        if (planta.isPlantada() == plantada) return;
        if (plantada && plantas.countByHuertoIdAndPlantadaTrue(huerto.getId()) >= 2)
            throw new ConflictoException("La vista virtual admite como máximo dos plantas marcadas como plantadas.");
        planta.cambiarPlantada(plantada);
    }

    @Transactional(readOnly = true)
    public List<Planta> plantasActivasPublicas(Long huertoId) {
        return plantas.findByHuertoIdAndActivaTrueOrderByNombreComunAsc(huertoId);
    }
}
