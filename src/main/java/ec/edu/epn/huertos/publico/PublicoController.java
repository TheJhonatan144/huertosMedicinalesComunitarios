package ec.edu.epn.huertos.publico;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PublicoController {

    private final PublicoServicio publico;

    public PublicoController(PublicoServicio publico) {
        this.publico = publico;
    }

    @GetMapping("/")
    String inicio(Model model) {
        model.addAttribute("ranking", publico.ranking().stream().limit(5).toList());
        model.addAttribute("huertos", publico.huertosParaMapa().size());
        return "publico/inicio";
    }

    @GetMapping("/mapa")
    String mapa(Model model) {
        model.addAttribute("huertos", publico.huertosParaMapa());
        return "publico/mapa";
    }

    @GetMapping("/comunidades/{id}")
    String comunidad(@PathVariable Long id, Model model) {
        model.addAttribute("ficha", publico.ficha(id));
        return "publico/comunidad";
    }

    @GetMapping("/ranking")
    String ranking(Model model) {
        model.addAttribute("ranking", publico.ranking());
        return "publico/ranking";
    }

    /** Datos del mapa para Leaflet; solo información pública. */
    @GetMapping("/api/publico/huertos")
    @ResponseBody
    List<PublicoServicio.HuertoMapa> huertosJson() {
        return publico.huertosParaMapa();
    }
}
