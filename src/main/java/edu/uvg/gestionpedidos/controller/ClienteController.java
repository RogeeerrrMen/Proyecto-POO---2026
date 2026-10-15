package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String busqueda,
            Model model) {

        model.addAttribute("clientes", clienteService.buscar(busqueda));
        model.addAttribute("busqueda", busqueda);

        return "clientes";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("titulo", "Registrar cliente");
        model.addAttribute("accion", "/clientes");

        return "formulario-cliente";
    }

    @PostMapping
    public String registrar(
            @RequestParam String nombre,
            @RequestParam String telefono,
            Model model) {

        try {
            clienteService.registrar(nombre, telefono);
            return "redirect:/clientes";

        } catch (IllegalArgumentException e) {
            model.addAttribute("cliente", new Cliente(nombre, telefono));
            model.addAttribute("titulo", "Registrar cliente");
            model.addAttribute("accion", "/clientes");
            model.addAttribute("error", e.getMessage());

            return "formulario-cliente";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("cliente", clienteService.obtenerPorId(id));
        model.addAttribute("titulo", "Editar cliente");
        model.addAttribute("accion", "/clientes/" + id);

        return "formulario-cliente";
    }

    @PostMapping("/{id}")
    public String modificar(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String telefono,
            Model model) {

        try {
            clienteService.modificar(id, nombre, telefono);
            return "redirect:/clientes";

        } catch (IllegalArgumentException e) {
            model.addAttribute("cliente", new Cliente(nombre, telefono));
            model.addAttribute("titulo", "Editar cliente");
            model.addAttribute("accion", "/clientes/" + id);
            model.addAttribute("error", e.getMessage());

            return "formulario-cliente";
        }
    }
}