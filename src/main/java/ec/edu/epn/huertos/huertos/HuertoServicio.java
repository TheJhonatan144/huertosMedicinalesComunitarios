package ec.edu.epn.huertos.huertos;

import ec.edu.epn.huertos.comunidades.ComunidadServicio;
import ec.edu.epn.huertos.compartido.RecursoNoEncontradoException;
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

    @Transactional
    public void actualizarHuerto(Long gestorUsuarioId, FormularioHuerto form) {
        huertoDeGestor(gestorUsuarioId).actualizar(form.getNombre().trim(), form.getDescripcion().trim());
    }

    @Transactional(readOnly = true)
    public List<Planta> plantasDeGestor(Long gestorUsuarioId) {
        return plantas.findByHuertoIdOrderByActivaDescNombreComunAsc(huertoDeGestor(gestorUsuarioId).getId());
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
        Planta planta = new Planta(huerto.getId(), form.getNombreComun().trim(), form.getNombreCientifico(), form.getDescripcion(), form.getCantidad());
        planta.cambiarSiembra(form.getCantidad(), form.isPlantada());
        return plantas.save(planta);
    }

    @Transactional
    public void actualizarPlanta(Long gestorUsuarioId, Long plantaId, FormularioPlanta form) {
        Planta planta = plantaDeGestor(gestorUsuarioId, plantaId);
        planta.actualizar(form.getNombreComun().trim(), form.getNombreCientifico(), form.getDescripcion(), form.getCantidad());
        planta.getEspecie().cambiarNombre(form.getNombreComun().trim(), form.getNombreCientifico());
        planta.cambiarSiembra(form.getCantidad(), form.isPlantada());
    }

    @Transactional
    public void cambiarEstadoPlanta(Long gestorUsuarioId, Long plantaId, boolean activa) {
        plantaDeGestor(gestorUsuarioId, plantaId).cambiarActiva(activa);
    }

    @Transactional(readOnly = true)
    public List<Planta> plantasActivasPublicas(Long huertoId) {
        return plantas.findByHuertoIdAndActivaTrueOrderByNombreComunAsc(huertoId);
    }
    @Transactional
    public Planta seleccionarEspecie(Long usuarioId, Especie especie, int cantidad, boolean plantada) {
        Long huertoId = huertoDeGestor(usuarioId).getId();
        // Serializa selecciones de la misma comunidad para no duplicar una especie.
        plantas.bloquearHuerto(huertoId);
        if (plantas.existsByHuertoIdAndEspecieId(huertoId, especie.getId()))
            throw new ec.edu.epn.huertos.compartido.ConflictoException("Esta especie ya está en tu lista. Edita su cantidad o siembra.");
        return plantas.save(new Planta(huertoId, especie, cantidad, plantada));
    }
    @Transactional
    public void actualizarSiembra(Long usuarioId, Long id, SiembraPlanta form) {
        plantaDeGestor(usuarioId, id).cambiarSiembra(form.getCantidad(), form.isPlantada());
    }
    @Transactional(readOnly = true)
    public List<Planta> plantasPlantadasDeGestor(Long usuarioId) {
        return plantas.findByHuertoIdAndPlantadaTrueOrderByNombreComunAsc(huertoDeGestor(usuarioId).getId());
    }
}
