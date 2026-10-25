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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
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
public class DatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);

    private final UsuarioRepository usuarios;
    private final ComunidadRepository comunidades;
    private final IntegranteRepository integrantes;
    private final AsignacionRevisorRepository asignaciones;
    private final HuertoRepository huertos;
    private final PlantaRepository plantas;
    private final PasswordEncoder encoder;
    private final String clave;

    public DatosDemo(UsuarioRepository usuarios, ComunidadRepository comunidades, IntegranteRepository integrantes,
                     AsignacionRevisorRepository asignaciones, HuertoRepository huertos, PlantaRepository plantas,
                     PasswordEncoder encoder, @Value("${app.demo.clave}") String clave) {
        this.usuarios = usuarios;
        this.comunidades = comunidades;
        this.integrantes = integrantes;
        this.asignaciones = asignaciones;
        this.huertos = huertos;
        this.plantas = plantas;
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
        Usuario revisorA = crear("revisor_a", "Revisor Demo A", Rol.ADMIN_EVIDENCIAS);
        asignaciones.save(new AsignacionRevisor(revisorA.getId(), imbabura.getId()));
        Usuario revisorB = crear("revisor_b", "Revisor Demo B", Rol.ADMIN_EVIDENCIAS);
        asignaciones.save(new AsignacionRevisor(revisorB.getId(), chimborazo.getId()));

        log.info("Perfil demo: datos sintéticos cargados (2 comunidades, 9 cuentas de demostración).");
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
}
