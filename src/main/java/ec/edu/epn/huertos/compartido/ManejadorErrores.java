package ec.edu.epn.huertos.compartido;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Atributos comunes de las vistas y traducción de excepciones de dominio a páginas en español. */
@ControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ModelAttribute("usuarioActual")
    UsuarioAutenticado usuarioActual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario;
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String noEncontrado(RecursoNoEncontradoException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ConflictoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String conflicto(ConflictoException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "error/regla";
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String regla(ReglaNegocioException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "error/regla";
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String versionObsoleta(Model model) {
        model.addAttribute("mensaje",
                "El registro cambió mientras lo editabas (por ejemplo, fue revisado). Vuelve a abrirlo e inténtalo de nuevo.");
        return "error/regla";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    String archivoGrande(Model model) {
        model.addAttribute("mensaje", "La fotografía supera el tamaño permitido.");
        return "error/regla";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String integridad(DataIntegrityViolationException e, Model model) {
        // Las restricciones de la base son la última defensa; el detalle técnico solo se registra.
        log.warn("Restricción de base de datos violada: {}", e.getMostSpecificCause().getMessage());
        model.addAttribute("mensaje", "La operación fue rechazada por una regla de integridad de los datos.");
        return "error/regla";
    }
}
