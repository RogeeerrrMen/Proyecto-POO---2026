package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.dto.EmprendimientoDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.service.EmprendimientoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.NoSuchElementException;

@Controller
public class EmprendimientoController {

    private final EmprendimientoService emprendimientoService;

    public EmprendimientoController(
            EmprendimientoService emprendimientoService
    ) {
        this.emprendimientoService = emprendimientoService;
    }

    //Muestra el emprendimiento del usuario autenticado
    @GetMapping("/emprendimientos")
    public String mostrarConfiguracion(
            Principal principal,
            Model model
    ) {
        Emprendimiento emprendimiento = emprendimientoService
                .buscarMiEmprendimiento(principal.getName())
                .orElse(null);

        model.addAttribute("emprendimiento", emprendimiento);

        return "configuracion";
    }

    //Abre el formulario para usuarios que todavía no tienen un negocio
    @GetMapping("/emprendimientos/nuevo")
    public String mostrarFormulario(
            Principal principal,
            Model model,
            RedirectAttributes redirect
    ) {
        if (emprendimientoService
                .buscarMiEmprendimiento(principal.getName())
                .isPresent()) {

            redirect.addFlashAttribute(
                    "mensaje",
                    "Ya tienes un emprendimiento registrado. "
                            + "Puedes modificar su nombre."
            );

            return "redirect:/emprendimientos";
        }

        model.addAttribute(
                "emprendimiento",
                new EmprendimientoDTO()
        );

        return "formulario-emprendimiento";
    }

    //Registra el negocio con el usuario autenticado como propietario
    @PostMapping("/emprendimientos")
    public String registrarEmprendimiento(
            @ModelAttribute("emprendimiento") EmprendimientoDTO datos,
            Principal principal,
            Model model,
            RedirectAttributes redirect
    ) {
        try {
            emprendimientoService.registrarEmprendimiento(
                    datos,
                    principal.getName()
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "El emprendimiento se registró correctamente."
            );

            return "redirect:/emprendimientos";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "formulario-emprendimiento";
        }
    }

    //Consulta un identificador comprobando que sea el del usuario
    @GetMapping("/emprendimientos/{id}")
    public String consultarEmprendimiento(
            @PathVariable("id") Long id,
            Principal principal,
            Model model,
            RedirectAttributes redirect
    ) {
        try {
            Emprendimiento emprendimiento =
                    emprendimientoService.consultarEmprendimiento(
                            id,
                            principal.getName()
                    );

            model.addAttribute("emprendimiento", emprendimiento);

            return "configuracion";

        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprendimientos";
        }
    }

    //Abre el formulario con el nombre actual
    @GetMapping("/emprendimientos/{id}/editar")
    public String mostrarFormularioEdicion(
            @PathVariable("id") Long id,
            Principal principal,
            Model model,
            RedirectAttributes redirect
    ) {
        try {
            Emprendimiento emprendimiento =
                    emprendimientoService.consultarEmprendimiento(
                            id,
                            principal.getName()
                    );

            model.addAttribute(
                    "emprendimiento",
                    EmprendimientoDTO.desde(emprendimiento)
            );

            model.addAttribute(
                    "emprendimientoId",
                    emprendimiento.getId()
            );

            return "formulario-emprendimiento";

        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprendimientos";
        }
    }

    //Actualiza únicamente el nombre del emprendimiento propio
    @PostMapping("/emprendimientos/{id}")
    public String modificarEmprendimiento(
            @PathVariable("id") Long id,
            @ModelAttribute("emprendimiento") EmprendimientoDTO datos,
            Principal principal,
            Model model,
            RedirectAttributes redirect
    ) {
        model.addAttribute("emprendimientoId", id);

        try {
            emprendimientoService.modificarEmprendimiento(
                    id,
                    datos,
                    principal.getName()
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "El emprendimiento se actualizó correctamente."
            );

            return "redirect:/emprendimientos";

        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprendimientos";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "formulario-emprendimiento";
        }
    }
}