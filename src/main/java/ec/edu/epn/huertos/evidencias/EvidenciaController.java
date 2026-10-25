package ec.edu.epn.huertos.evidencias;

import ec.edu.epn.huertos.autenticacion.UsuarioAutenticado;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EvidenciaController {

    private final EvidenciaServicio evidencias;

    public EvidenciaController(EvidenciaServicio evidencias) {
        this.evidencias = evidencias;
    }

    @GetMapping("/evidencias/envios/{envioId}/foto")
    ResponseEntity<Resource> foto(@PathVariable Long envioId, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        EvidenciaServicio.FotoAutorizada foto = evidencias.cargarFoto(usuario.getId(), envioId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.tipoContenido()))
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("evidencia-" + envioId).build().toString())
                .body(foto.recurso());
    }
}
