package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.service.ClienteService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        prepararFormulario(model, new Cliente(), "Registrar cliente", "/clientes");

        return "formulario-cliente";
    }

    @PostMapping
    public String registrar(@RequestParam String nombre, @RequestParam String telefono, @RequestParam(required = false) String nit,
            @RequestParam(required = false) String razonSocial, Model model) {

        try {
            clienteService.registrar(nombre, telefono, nit, razonSocial);

            return "redirect:/clientes";

        } catch (IllegalArgumentException e) {
            Cliente cliente = crearClienteFormulario(nombre, telefono, nit, razonSocial);

            prepararFormulario(model, cliente, "Registrar cliente", "/clientes");

            model.addAttribute("error", e.getMessage());

            return "formulario-cliente";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {

        prepararFormulario(model, clienteService.obtenerPorId(id), "Editar cliente", "/clientes/" + id);

        return "formulario-cliente";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable Long id, @RequestParam String nombre, @RequestParam String telefono,
            @RequestParam(required = false) String nit, @RequestParam(required = false) String razonSocial, Model model) {

        try {
            clienteService.modificar(id, nombre, telefono, nit, razonSocial);

            return "redirect:/clientes";

        } catch (IllegalArgumentException e) {
            Cliente cliente = crearClienteFormulario(nombre, telefono, nit, razonSocial);

            prepararFormulario(model, cliente, "Editar cliente", "/clientes/" + id);

            model.addAttribute("error", e.getMessage());

            return "formulario-cliente";
        }
    }

    private Cliente crearClienteFormulario(String nombre, String telefono, String nit, String razonSocial) {

        Cliente cliente = new Cliente(nombre, telefono);
        cliente.setNit(nit);
        cliente.setRazonSocial(razonSocial);

        return cliente;
    }

    private void prepararFormulario(Model model, Cliente cliente, String titulo, String accion) {

        model.addAttribute("cliente", cliente);
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
    }
}