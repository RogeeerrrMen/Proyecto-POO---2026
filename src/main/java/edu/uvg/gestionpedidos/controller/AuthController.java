package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.dto.RegistroUsuarioDTO;
import edu.uvg.gestionpedidos.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;


@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {

        this.usuarioService = usuarioService;

    }

    @GetMapping("/login")
    public String mostrarLogin() {

        return "login";

    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {

        model.addAttribute("registroUsuarioDTO", new RegistroUsuarioDTO());
        return "registro";

    }

    @PostMapping("/registro")
    public String registrarCuenta(@ModelAttribute("registroUsuarioDTO") RegistroUsuarioDTO datos,Model model) {

        try {

            usuarioService.registrarUsuario(datos);
            return "redirect:/dashboard";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            datos.setPassword(null);
            datos.setConfirmarPassword(null);
            
            return "registro";
        }
    }

    

}