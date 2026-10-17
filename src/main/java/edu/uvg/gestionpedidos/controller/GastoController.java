package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.dto.GastoDTO;
import edu.uvg.gestionpedidos.entity.Gasto;
import edu.uvg.gestionpedidos.service.GastoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    //Crea la lista de todos los gastos o filtra por período
    @GetMapping("/gastos")
    public String listarGastos(
            @RequestParam(name = "inicio", defaultValue = "") String inicio,
            @RequestParam(name = "fin", defaultValue = "") String fin,
            Model model
    ) {
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("gastos", List.of());

        try {
            if (inicio.isBlank() && fin.isBlank()) {
                model.addAttribute("gastos", gastoService.listarGastos());
            } else {
                LocalDate fechaInicio =
                        inicio.isBlank() ? null : LocalDate.parse(inicio);

                LocalDate fechaFin =
                        fin.isBlank() ? null : LocalDate.parse(fin);

                model.addAttribute(
                        "gastos",
                        gastoService.consultarGastosPorPeriodo(
                                fechaInicio, fechaFin)
                );

                model.addAttribute(
                        "total",
                        gastoService.calcularTotalPorPeriodo(
                                fechaInicio, fechaFin)
                );
            }
        } catch (DateTimeParseException e) {
            model.addAttribute(
                    "error",
                    "Ingresa fechas válidas con formato AAAA-MM-DD."
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "gastos";
    }

    //Abre el formulario de registro.
    @GetMapping("/gastos/nuevo")
    public String mostrarFormulario(Model model) {
        GastoDTO datos = new GastoDTO();
        datos.setFecha(LocalDate.now());

        model.addAttribute("gasto", datos);
        return "formulario-gasto";
    }

    //Procesa el registro.
    @PostMapping("/gastos")
    public String registrarGasto(
            @ModelAttribute("gasto") GastoDTO datos,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirect
    ) {
        if (resultado.hasErrors()) {
            model.addAttribute(
                    "error",
                    "Revisa el formato del monto y de la fecha."
            );
            return "formulario-gasto";
        }

        try {
            gastoService.registrarGasto(datos);

            redirect.addFlashAttribute(
                    "mensaje",
                    "El gasto se registró correctamente."
            );

            return "redirect:/gastos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "formulario-gasto";
        }
    }

    //Muestra la información de un gasto
    @GetMapping("/gastos/{id}")
    public String consultarGasto(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirect
    ) {
        try {
            model.addAttribute("gasto", obtenerGasto(id));
            return "detalle-gasto";
        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/gastos";
        }
    }

    //Abre el formulario con los datos actuales del gasto para su edición
    @GetMapping("/gastos/{id}/editar")
    public String mostrarFormularioEdicion(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirect
    ) {
        try {
            Gasto gasto = obtenerGasto(id);

            model.addAttribute("gasto", GastoDTO.desde(gasto));
            model.addAttribute("gastoId", gasto.getId());

            return "formulario-gasto";
        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/gastos";
        }
    }

    //Procesa la modificación.
    @PostMapping("/gastos/{id}")
    public String modificarGasto(
            @PathVariable("id") Long id,
            @ModelAttribute("gasto") GastoDTO datos,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirect
    ) {
        //Guarda el identificador para volver al formulario de edición.
        model.addAttribute("gastoId", id);

        if (resultado.hasErrors()) {
            model.addAttribute(
                    "error",
                    "Revisa el formato del monto y de la fecha."
            );
            return "formulario-gasto";
        }

        try {
            gastoService.modificarGasto(id, datos);

            redirect.addFlashAttribute(
                    "mensaje",
                    "El gasto se actualizó correctamente."
            );

            return "redirect:/gastos";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/gastos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "formulario-gasto";
        }
    }

    private Gasto obtenerGasto(Long id) {
        return gastoService.buscarGastoPorId(id)
                .orElseThrow(() ->
                        new NoSuchElementException("El gasto no existe."));
    }
}