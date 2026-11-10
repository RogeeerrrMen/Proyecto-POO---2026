package edu.uvg.gestionpedidos.controller;

import java.security.Principal;
import java.util.NoSuchElementException;

import edu.uvg.gestionpedidos.dto.ConfiguracionEstadosDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.MaquinaEstadosPedido;
import edu.uvg.gestionpedidos.service.EmprendimientoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/emprendimientos/estados")
public class ConfiguracionEstadosController {

    private final EmprendimientoService emprendimientoService;

    public ConfiguracionEstadosController(
            EmprendimientoService emprendimientoService) {

        this.emprendimientoService = emprendimientoService;
    }

    @GetMapping
    public String mostrarFormulario(
            Principal principal,
            Model model,
            RedirectAttributes mensajes) {

        try {
            Emprendimiento emprendimiento =
                    obtenerMiEmprendimiento(principal.getName());

            ConfiguracionEstadosDTO datos =
                    new ConfiguracionEstadosDTO();

            datos.setEstados(
                    emprendimientoService.consultarFlujoEstados(
                            principal.getName()));

            model.addAttribute("configuracionEstadosDTO", datos);

            cargarOpciones(model, emprendimiento);

            return "configuracion-estados";

        } catch (NoSuchElementException e) {
            mensajes.addFlashAttribute("error", e.getMessage());

            return "redirect:/emprendimientos";
        }
    }

    @PostMapping
    public String guardarConfiguracion(
            @ModelAttribute("configuracionEstadosDTO")
            ConfiguracionEstadosDTO datos,
            BindingResult resultado,
            Principal principal,
            Model model,
            RedirectAttributes mensajes) {

        Emprendimiento emprendimiento;

        try {
            emprendimiento = obtenerMiEmprendimiento(principal.getName());

        } catch (NoSuchElementException e) {
            mensajes.addFlashAttribute("error", e.getMessage());

            return "redirect:/emprendimientos";
        }

        if (resultado.hasErrors()) {
            cargarOpciones(model, emprendimiento);

            model.addAttribute("error", "La selección tiene valores inválidos. " + "Revisa los estados del formulario.");

            return "configuracion-estados";
        }

        try {
            emprendimientoService.modificarFlujoEstados(emprendimiento.getId(), datos.getEstados(), principal.getName());

            mensajes.addFlashAttribute("mensaje", "La configuración de estados se guardó correctamente.");

            return "redirect:/emprendimientos/estados";

        } catch (IllegalArgumentException e) {
            cargarOpciones(model, emprendimiento);

            model.addAttribute("error", e.getMessage());

            return "configuracion-estados";

        } catch (NoSuchElementException e) {
            mensajes.addFlashAttribute("error", e.getMessage());

            return "redirect:/emprendimientos";
        }
    }

    private Emprendimiento obtenerMiEmprendimiento(String correo) {
        return emprendimientoService
                .buscarMiEmprendimiento(correo)
                .orElseThrow(() -> new NoSuchElementException("Debes registrar tu emprendimiento " + "antes de configurar los estados."));
    }

    private void cargarOpciones(Model model, Emprendimiento emprendimiento) {

        model.addAttribute("emprendimiento", emprendimiento);

        model.addAttribute("estadosDisponibles", MaquinaEstadosPedido.flujoPredeterminado());
    }
}