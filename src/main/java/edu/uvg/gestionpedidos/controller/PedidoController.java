package edu.uvg.gestionpedidos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.uvg.gestionpedidos.dto.DetallePedidoDTO;
import edu.uvg.gestionpedidos.dto.PedidoDTO;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.service.ClienteService;
import edu.uvg.gestionpedidos.service.PedidoService;
import edu.uvg.gestionpedidos.service.ProductoService;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    public PedidoController(PedidoService pedidoService, ClienteService clienteService, ProductoService productoService) {

        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listar());

        return "pedidos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        PedidoDTO datos = new PedidoDTO();

        // El formulario comienza con una fila para seleccionar un producto.
        datos.getDetalles().add(new DetallePedidoDTO());

        model.addAttribute("pedidoDTO", datos);
        cargarOpcionesFormulario(model);

        return "formulario-pedido";
    }

    @PostMapping
    public String registrar(@ModelAttribute("pedidoDTO") PedidoDTO datos, BindingResult resultado, Model model, RedirectAttributes mensajes) {

        // Detecta valores que Spring no pudo convertir:
        // por ejemplo, una cantidad escrita como texto.
        if (resultado.hasErrors()) {
            model.addAttribute("error", "Revisa los valores ingresados en el formulario.");

            cargarOpcionesFormulario(model);

            return "formulario-pedido";
        }

        try {
            Pedido pedido = pedidoService.registrar(datos);

            mensajes.addFlashAttribute("exito", "El pedido se registró correctamente.");

            return "redirect:/pedidos/" + pedido.getId();

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            cargarOpcionesFormulario(model);

            return "formulario-pedido";
        }
    }

    @GetMapping("/{id}")
    public String consultar(@PathVariable Long id, Model model) {

        try {
            model.addAttribute("pedido", pedidoService.obtenerPorId(id));

            return "detalle-pedido";

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    private void cargarOpcionesFormulario(Model model) {
        model.addAttribute("clientes", clienteService.listar());

        model.addAttribute("productos", productoService.listarProductosActivos());
    }
}