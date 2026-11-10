package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.huertos.Huerto;
import ec.edu.epn.huertos.huertos.HuertoRepository;
import ec.edu.epn.huertos.huertos.EspacioHuertoServicio;
import ec.edu.epn.huertos.huertos.HuertoCerramientoServicio;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AltaComunidadServicio {
    private final ComunidadRepository comunidades;
    private final UsuarioRepository usuarios;
    private final IntegranteRepository integrantes;
    private final HuertoRepository huertos;
    private final Validator validador;
    private final AsignacionRevisorServicio asignaciones;
    private final EspacioHuertoServicio espacios;
    private final HuertoCerramientoServicio cerramientos;

    public AltaComunidadServicio(ComunidadRepository comunidades, UsuarioRepository usuarios,
            IntegranteRepository integrantes, HuertoRepository huertos, Validator validador,
            AsignacionRevisorServicio asignaciones, EspacioHuertoServicio espacios,
            HuertoCerramientoServicio cerramientos) {
        this.comunidades = comunidades; this.usuarios = usuarios; this.integrantes = integrantes;
        this.huertos = huertos; this.validador = validador; this.asignaciones = asignaciones;
        this.espacios = espacios; this.cerramientos = cerramientos;
    }

    @Transactional
    public void crearAprobada(Long usuarioId, FormularioAltaComunidad f) {
        validarFormulario(f);
        Usuario usuario = usuarios.findById(usuarioId)
                .filter(u -> u.isActivo() && u.getRol() == Rol.SIN_ASIGNAR)
                .orElseThrow(() -> new ConflictoException("Solo una cuenta sin comunidad puede crear una huerta."));
        if (integrantes.findByUsuarioIdAndActivoTrue(usuarioId).isPresent())
            throw new ConflictoException("Ya perteneces a una comunidad. Primero debes dejarla.");
        if (comunidades.existsByNombreIgnoreCase(f.getNombreComunidad().trim()))
            throw new IllegalArgumentException("Ya existe una comunidad con ese nombre.");
        validarOpciones(f);
        Comunidad comunidad = comunidades.save(new Comunidad(f.getNombreComunidad().trim(), f.getDescripcionComunidad().trim(),
                f.getTipoComunidad(), f.getProvincia().trim(), f.getCanton().trim(), f.getDireccionComunidad().trim(),
                usuario.getTelefono(), usuario.getCorreo(),
                f.getLatitud(), f.getLongitud()));
        integrantes.save(new Integrante(comunidad.getId(), usuarioId, true, usuario.getFechaNacimiento(),
                usuario.getGenero(), usuario.getTalentosCertificados(), usuario.getNivelExperiencia()));
        Huerto huerto = huertos.save(new Huerto(comunidad.getId(), f.getNombre().trim(), f.getDescripcion().trim(), f.getProvincia().trim(),
                f.getParroquia().trim(), f.getDireccion().trim(), f.getAreaM2(), f.getDiseno(), f.getTipoSuelo(),
                f.getFuenteAgua().trim(), f.getAbonoNatural().trim(), f.getCerramiento(), f.getLatitud(), f.getLongitud()));
        espacios.sincronizar(huerto.getId(), f.getAreaM2(), f.getDisenos());
        cerramientos.sincronizar(huerto.getId(), f.getGruposCerramiento());
        asignaciones.asignarAutomaticamente(comunidad.getId());
        usuario.cambiarRol(Rol.GESTOR_HUERTO);
    }

    public void validarFormulario(FormularioAltaComunidad f) {
        var errores = validador.validate(f);
        if (!errores.isEmpty())
            throw new IllegalArgumentException(errores.stream().map(e -> e.getMessage()).distinct().collect(Collectors.joining(" ")));
        validarOpciones(f);
    }

    private void validarOpciones(FormularioAltaComunidad f) {
        List<String> disenos = f.getDisenos() == null || f.getDisenos().isEmpty() ? List.of(f.getDiseno()) : f.getDisenos();
        if (disenos.isEmpty() || disenos.stream().anyMatch(d -> !List.of("TIERRA_SURCOS", "TIERRA_CAMADAS", "TIERRA_CAMELLONES", "TIERRA_TERRAZAS", "VERTICAL_PAREDES", "COLGANTE_TECHOS", "PISO_MACETAS", "PISO_BOTELLAS_PATIO", "PISO_LLANTAS_PATIO", "PISO_TERRAZA", "PISO_GRADAS", "PISO_BALCONES", "PISO_FILOS_VENTANA").contains(d)))
            throw new IllegalArgumentException("Selecciona uno o más diseños válidos.");
        if (!List.of("ARCILLOSO", "LIMOSO", "ARENOSO", "MARGOSO", "GREDOSO", "PANTANOSO").contains(f.getTipoSuelo()))
            throw new IllegalArgumentException("Selecciona un tipo de suelo válido.");
        if (!List.of("BARBECHO", "RESTOS_VEGETALES", "HOJA", "COMPOST").contains(f.getAbonoNatural()))
            throw new IllegalArgumentException("Selecciona un abono natural válido.");
        List<String> gruposCerramiento = f.getGruposCerramiento() == null ? List.of() : f.getGruposCerramiento();
        if (!HuertoCerramientoServicio.GRUPOS.containsAll(gruposCerramiento))
            throw new IllegalArgumentException("Selecciona grupos de cerramiento válidos.");
        if (!List.of("PERSONA", "FAMILIA", "CONDOMINIO", "BARRIO").contains(f.getTipoComunidad()))
            throw new IllegalArgumentException("Selecciona un tipo de comunidad válido.");
    }
}
