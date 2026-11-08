package ec.edu.epn.huertos.demo;

import ec.edu.epn.huertos.autenticacion.Rol;
import ec.edu.epn.huertos.autenticacion.Usuario;
import ec.edu.epn.huertos.autenticacion.UsuarioRepository;
import ec.edu.epn.huertos.comunidades.AsignacionRevisor;
import ec.edu.epn.huertos.comunidades.AsignacionRevisorRepository;
import ec.edu.epn.huertos.comunidades.Comunidad;
import ec.edu.epn.huertos.comunidades.ComunidadRepository;
import ec.edu.epn.huertos.comunidades.Integrante;
import ec.edu.epn.huertos.comunidades.IntegranteRepository;
import ec.edu.epn.huertos.huertos.Huerto;
import ec.edu.epn.huertos.huertos.HuertoRepository;
import ec.edu.epn.huertos.huertos.Planta;
import ec.edu.epn.huertos.huertos.PlantaRepository;
import ec.edu.epn.huertos.huertos.EspacioHuertoServicio;
import ec.edu.epn.huertos.huertos.HuertoCerramientoServicio;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos SINTÉTICOS de demostración (perfil "demo"). Mecanismo provisional de provisión de usuarios:
 * no existe registro público ni recuperación por correo. Solo carga datos si la tabla de usuarios está vacía;
 * nunca borra ni modifica datos existentes. Todos los nombres, comunidades y plantas son ficticios o ilustrativos.
 */
@Component
@Profile("demo")
@Order(1)
public class DatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);

    private final UsuarioRepository usuarios;
    private final ComunidadRepository comunidades;
    private final IntegranteRepository integrantes;
    private final AsignacionRevisorRepository asignaciones;
    private final HuertoRepository huertos;
    private final PlantaRepository plantas;
    private final EspacioHuertoServicio espacios;
    private final HuertoCerramientoServicio cerramientos;
    private final PasswordEncoder encoder;
    private final String clave;

    public DatosDemo(UsuarioRepository usuarios, ComunidadRepository comunidades, IntegranteRepository integrantes,
                     AsignacionRevisorRepository asignaciones, HuertoRepository huertos, PlantaRepository plantas,
                     PasswordEncoder encoder, EspacioHuertoServicio espacios,
                     HuertoCerramientoServicio cerramientos,
                     @Value("${app.demo.clave}") String clave) {
        this.usuarios = usuarios;
        this.comunidades = comunidades;
        this.integrantes = integrantes;
        this.asignaciones = asignaciones;
        this.huertos = huertos;
        this.plantas = plantas;
        this.espacios = espacios;
        this.cerramientos = cerramientos;
        this.encoder = encoder;
        this.clave = clave;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            log.info("Perfil demo: la base ya contiene usuarios; no se cargan datos de demostración.");
            return;
        }
        Comunidad imbabura = comunidades.save(new Comunidad("Comunidad Demo Sumak Allpa",
                "Comunidad ficticia de demostración en la sierra norte. Datos sintéticos.", "Imbabura", "Otavalo",
                0.2342, -78.2610));
        Comunidad chimborazo = comunidades.save(new Comunidad("Comunidad Demo Pachamama Wasi",
                "Comunidad ficticia de demostración en la sierra central. Datos sintéticos.", "Chimborazo", "Riobamba",
                -1.6636, -78.6547));

        Huerto huertoA = huertos.save(new Huerto(imbabura.getId(), "Huerto Demo Sumak Allpa",
                "Huerto comunitario ficticio para la demostración del prototipo.", 0.2350, -78.2600));
        Huerto huertoB = huertos.save(new Huerto(chimborazo.getId(), "Huerto Demo Pachamama Wasi",
                "Huerto comunitario ficticio para la demostración del prototipo.", -1.6640, -78.6540));
        // El constructor corto deja el abono «No especificado», que no es una opción del formulario.
        conAbono(huertoA, "COMPOST");
        conAbono(huertoB, "RESTOS_VEGETALES");
        espacios.sincronizar(huertoA.getId(), huertoA.getAreaM2(), List.of(huertoA.getDiseno()));
        espacios.sincronizar(huertoB.getId(), huertoB.getAreaM2(), List.of(huertoB.getDiseno()));
        cerramientos.sincronizar(huertoA.getId(), List.of("NATURAL"));
        cerramientos.sincronizar(huertoB.getId(), List.of("CONVENCIONAL"));

        // Plantas: nombres comunes ilustrativos; sin propiedades medicinales ni recomendaciones (pendiente de fuentes).
        plantas.save(new Planta(huertoA.getId(), "Manzanilla", "Matricaria chamomilla",
                "Texto ilustrativo de demostración. Contenido pendiente de fuentes validadas.", 12));
        plantas.save(new Planta(huertoA.getId(), "Hierba luisa", "Cymbopogon citratus",
                "Texto ilustrativo de demostración. Contenido pendiente de fuentes validadas.", 8));
        plantas.save(new Planta(huertoB.getId(), "Menta", "Mentha spicata",
                "Texto ilustrativo de demostración. Contenido pendiente de fuentes validadas.", 10));
        plantas.save(new Planta(huertoB.getId(), "Ruda", "Ruta graveolens",
                "Texto ilustrativo de demostración. Contenido pendiente de fuentes validadas.", 5));

        // Comunidad A: gestor + 4 participantes (uno de baja para demostrar la regla de selección).
        Usuario gestorA = crear("gestor_a", "Gestora Demo A", Rol.GESTOR_HUERTO);
        integrantes.save(new Integrante(imbabura.getId(), gestorA.getId(), true));
        alta(imbabura, "participante_a1", "Participante Demo A1", true);
        alta(imbabura, "participante_a2", "Participante Demo A2", true);
        alta(imbabura, "participante_a3", "Participante Demo A3", true);
        alta(imbabura, "participante_a4", "Participante Demo A4 (de baja)", false);

        // Comunidad B.
        Usuario gestorB = crear("gestor_b", "Gestor Demo B", Rol.GESTOR_HUERTO);
        integrantes.save(new Integrante(chimborazo.getId(), gestorB.getId(), true));
        alta(chimborazo, "participante_b1", "Participante Demo B1", true);
        alta(chimborazo, "participante_b2", "Participante Demo B2", true);

        // Revisores: cada uno con una sola comunidad asignada.
        Usuario revisorA = crear("revisor_a", "Administrador Ministerio Demo A", Rol.ADMIN_MINISTERIO_SALUD);
        asignaciones.save(new AsignacionRevisor(revisorA.getId(), imbabura.getId()));
        Usuario revisorB = crear("revisor_b", "Administrador Ministerio Demo B", Rol.ADMIN_MINISTERIO_SALUD);
        asignaciones.save(new AsignacionRevisor(revisorB.getId(), chimborazo.getId()));

        crear("admin_tecnico", "Administrador Técnico Demo", Rol.ADMIN_TECNICO);

        log.info("Perfil demo: datos sintéticos cargados (2 comunidades, 11 cuentas de demostración).");
    }

    private Usuario crear(String nombreUsuario, String nombreCompleto, Rol rol) {
        return usuarios.save(new Usuario(nombreUsuario, nombreCompleto, encoder.encode(clave), rol));
    }

    private void alta(Comunidad comunidad, String nombreUsuario, String nombreCompleto, boolean activo) {
        Usuario usuario = crear(nombreUsuario, nombreCompleto, Rol.PARTICIPANTE);
        Integrante integrante = new Integrante(comunidad.getId(), usuario.getId(), false);
        if (!activo) {
            integrante.darDeBaja();
        }
        integrantes.save(integrante);
    }

    /** Cambia solo el abono, conservando el resto de datos del huerto (entidad gestionada dentro de run). */
    private static void conAbono(Huerto h, String abono) {
        h.actualizar(h.getNombre(), h.getDescripcion(), h.getProvincia(), h.getParroquia(), h.getDireccion(),
                h.getAreaM2(), h.getDiseno(), h.getTipoSuelo(), h.getFuenteAgua(), abono, h.getLatitud(), h.getLongitud());
    }
}
