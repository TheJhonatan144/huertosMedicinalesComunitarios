package ec.edu.epn.huertos.autenticacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class RegistroCuentaController {
    private final RegistroCuentaServicio registro;
    public RegistroCuentaController(RegistroCuentaServicio registro) { this.registro = registro; }

    @GetMapping("/registro")
    String formulario(Model model) {
        if (!model.containsAttribute("formulario")) model.addAttribute("formulario", new FormularioRegistroCuenta());
        return "auth/registro";
    }

    @PostMapping("/registro")
    String registrar(@Valid @ModelAttribute("formulario") FormularioRegistroCuenta f, BindingResult resultado, Model model) {
        if (resultado.hasErrors()) return "auth/registro";
        try {
            registro.registrar(f);
            return "redirect:/login?registro";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }

    @GetMapping("/cuenta")
    String cuenta() { return "auth/cuenta"; }
}
