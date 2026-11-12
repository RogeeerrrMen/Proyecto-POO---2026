package edu.uvg.gestionpedidos.controller;

import java.security.Principal;
import java.util.NoSuchElementException;
import edu.uvg.gestionpedidos.service.CalendarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CalendarioController {
    private final CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @GetMapping("/calendario")
    public String mostrar(@RequestParam(required = false) String mes,
            Principal principal, Model model, RedirectAttributes mensajes) {
        try {
            model.addAttribute("calendario",
                    calendarioService.consultar(principal.getName(), mes));
            return "calendario";
        } catch (NoSuchElementException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprendimientos";
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/calendario";
        }
    }
}
