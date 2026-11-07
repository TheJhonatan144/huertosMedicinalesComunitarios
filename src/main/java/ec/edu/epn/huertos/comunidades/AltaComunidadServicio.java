package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.compartido.ConflictoException;
import ec.edu.epn.huertos.huertos.Huerto;
import ec.edu.epn.huertos.huertos.HuertoRepository;
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

    public AltaComunidadServicio(ComunidadRepository comunidades, UsuarioRepository usuarios,
            IntegranteRepository integrantes, HuertoRepository huertos, Validator validador,
            AsignacionRevisorServicio asignaciones) {
        this.comunidades = comunidades; this.usuarios = usuarios; this.integrantes = integrantes;
        this.huertos = huertos; this.validador = validador; this.asignaciones = asignaciones;
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
        huertos.save(new Huerto(comunidad.getId(), f.getNombre().trim(), f.getDescripcion().trim(), f.getProvincia().trim(),
                f.getParroquia().trim(), f.getDireccion().trim(), f.getAreaM2(), f.getDiseno(), f.getTipoSuelo(),
                f.getFuenteAgua().trim(), f.getAbonoNatural().trim(), f.getCerramiento(), f.getLatitud(), f.getLongitud()));
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
        if (!List.of("TIERRA_SURCOS", "TIERRA_CAMADAS", "TIERRA_CAMELLONES", "TIERRA_TERRAZAS", "VERTICAL_PAREDES", "COLGANTE_TECHOS", "PISO_MACETAS", "PISO_BOTELLAS_PATIO", "PISO_LLANTAS_PATIO", "PISO_TERRAZA", "PISO_GRADAS", "PISO_BALCONES", "PISO_FILOS_VENTANA").contains(f.getDiseno()))
            throw new IllegalArgumentException("Selecciona un diseño válido.");
        if (!List.of("ARCILLOSO", "LIMOSO", "ARENOSO", "MARGOSO", "GREDOSO", "PANTANOSO").contains(f.getTipoSuelo()))
            throw new IllegalArgumentException("Selecciona un tipo de suelo válido.");
        if (!List.of("BARBECHO", "RESTOS_VEGETALES", "HOJA", "COMPOST").contains(f.getAbonoNatural()))
            throw new IllegalArgumentException("Selecciona un abono natural válido.");
        if (!List.of("VIVO_HIGUERILLA", "VIVO_FLORIPONDIO", "VIVO_AGAVE", "VIVO_LAVANDA", "VIVO_NARANJOS", "VIVO_CAPULIES", "VIVO_TOCTE", "VIVO_GUABO", "NATURAL_CANA_GUADUA", "NATURAL_BAHAREQUE", "NATURAL_TAPIAL", "RECICLADO_TABLAS", "RECICLADO_BOTELLAS", "RECICLADO_NEUMATICOS", "METALICO_ALAMBRE_PUAS", "CONVENCIONAL_PIEDRA", "CONVENCIONAL_LADRILLO", "CONVENCIONAL_BLOQUE").contains(f.getCerramiento()))
            throw new IllegalArgumentException("Selecciona un tipo de cerramiento válido.");
        if (!List.of("PERSONA", "FAMILIA", "CONDOMINIO", "BARRIO").contains(f.getTipoComunidad()))
            throw new IllegalArgumentException("Selecciona un tipo de comunidad válido.");
    }
}
