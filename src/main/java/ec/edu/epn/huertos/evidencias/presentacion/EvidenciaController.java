package ec.edu.epn.huertos.evidencias.presentacion;
import ec.edu.epn.huertos.autenticacion.aplicacion.UsuarioAutenticado;
import ec.edu.epn.huertos.evidencias.aplicacion.EvidenciaServicio;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class EvidenciaController {
    private static final Pattern RANGO = Pattern.compile("bytes=(\\d*)-(\\d*)");

    private final EvidenciaServicio evidencias;

    public EvidenciaController(EvidenciaServicio evidencias) {
        this.evidencias = evidencias;
    }

    @GetMapping("/evidencias/envios/{envioId}/foto")
    ResponseEntity<Resource> foto(@PathVariable Long envioId, @AuthenticationPrincipal UsuarioAutenticado usuario,
                                  @RequestHeader(value = HttpHeaders.RANGE, required = false) String rango) {
        EvidenciaServicio.FotoAutorizada foto = evidencias.cargarFoto(usuario.getId(), envioId);
        long longitud;
        try { longitud = foto.recurso().contentLength(); }
        catch (IOException e) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND); }
        String nombre = "evidencia-" + envioId + ("video/mp4".equals(foto.tipoContenido()) ? ".mp4" : "");
        var respuesta = ResponseEntity.ok().contentType(MediaType.parseMediaType(foto.tipoContenido()))
                .cacheControl(CacheControl.noStore().cachePrivate()).header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(nombre).build().toString());
        if (!"video/mp4".equals(foto.tipoContenido())) return respuesta.body(foto.recurso());
        respuesta.header(HttpHeaders.ACCEPT_RANGES, "bytes");
        if (rango == null) return respuesta.body(foto.recurso());
        Matcher matcher = RANGO.matcher(rango);
        if (!matcher.matches() || longitud == 0) return ResponseEntity.status(416).header(HttpHeaders.CONTENT_RANGE, "bytes */" + longitud).build();
        try {
            long inicio = matcher.group(1).isEmpty() ? 0 : Long.parseLong(matcher.group(1));
            long fin = matcher.group(2).isEmpty() || matcher.group(1).isEmpty()
                    ? longitud - 1 : Math.min(Long.parseLong(matcher.group(2)), longitud - 1);
            if (matcher.group(1).isEmpty() && !matcher.group(2).isEmpty()) inicio = Math.max(0, longitud - Long.parseLong(matcher.group(2)));
            if (inicio >= longitud || fin < inicio) return ResponseEntity.status(416).header(HttpHeaders.CONTENT_RANGE, "bytes */" + longitud).build();
            long cantidad = fin - inicio + 1;
            var flujo = foto.recurso().getInputStream();
            flujo.skipNBytes(inicio);
            InputStreamResource parcial = new InputStreamResource(new LimitarFlujo(flujo, cantidad));
            return ResponseEntity.status(206).contentType(MediaType.parseMediaType(foto.tipoContenido()))
                    .contentLength(cantidad).header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + inicio + "-" + fin + "/" + longitud)
                    .header("X-Content-Type-Options", "nosniff")
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(nombre).build().toString())
                    .cacheControl(CacheControl.noStore().cachePrivate()).body(parcial);
        } catch (IOException | NumberFormatException e) {
            return ResponseEntity.status(416).header(HttpHeaders.CONTENT_RANGE, "bytes */" + longitud).build();
        }
    }

    private static final class LimitarFlujo extends java.io.FilterInputStream {
        private long restantes;
        LimitarFlujo(java.io.InputStream entrada, long cantidad) { super(entrada); restantes = cantidad; }
        @Override public int read() throws IOException { if (restantes <= 0) return -1; int b = super.read(); if (b >= 0) restantes--; return b; }
        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (restantes <= 0) return -1;
            int n = super.read(b, off, (int) Math.min(len, restantes)); if (n > 0) restantes -= n; return n;
        }
    }
}
