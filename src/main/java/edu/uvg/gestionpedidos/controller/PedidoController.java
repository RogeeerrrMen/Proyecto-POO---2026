package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.dto.PedidoForm;
import edu.uvg.gestionpedidos.model.Cliente;
import edu.uvg.gestionpedidos.model.DireccionEntrega;
import edu.uvg.gestionpedidos.model.Pedido;
import edu.uvg.gestionpedidos.service.PedidoService;
import edu.uvg.gestionpedidos.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Coordina las solicitudes HTTP de pedidos. No contiene reglas de negocio:
 * delega en PedidoService y en los objetos del dominio.
 */
@Controller
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProductoService productoService;

    public PedidoController(PedidoService pedidoService, ProductoService productoService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
    }

    @GetMapping("/pedidos")
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listar());
        return "pedidos";
    }

    @GetMapping("/pedidos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("form", new PedidoForm());
        model.addAttribute("productos", productoService.listarActivos());
        return "formulario-pedido";
    }

    @PostMapping("/pedidos")
    public String registrarPedido(@ModelAttribute("form") PedidoForm form, Model model) {
        try {
            Cliente cliente = new Cliente(form.getNombreCliente(), form.getTelefonoCliente(), null);
            DireccionEntrega direccion = new DireccionEntrega(
                    form.getNombreReceptor(), form.getTelefonoReceptor(), form.getDireccion(),
                    form.getReferencias(), form.getEnlaceMapa());
            Pedido pedido = pedidoService.crearPedido(cliente, direccion, form.getCantidades(),
                    form.getFechaPreparacion(), form.getFechaEnvio(), form.getFechaEntrega(),
                    form.getObservaciones());
            return "redirect:/pedidos/" + pedido.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productos", productoService.listarActivos());
            return "formulario-pedido";
        }
    }

    @GetMapping("/pedidos/{id}")
    public String consultarPedido(@PathVariable Long id, Model model) {
        return pedidoService.buscarPorId(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    return "detalle-pedido";
                })
                .orElse("redirect:/pedidos");
    }

    @PostMapping("/pedidos/{id}/estado")
    public String actualizarEstado(@PathVariable Long id, @RequestParam String accion,
                                   RedirectAttributes redirect) {
        try {
            pedidoService.cambiarEstado(id, accion);
            redirect.addFlashAttribute("mensaje", "El estado del pedido se actualizó correctamente.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/pedidos/{id}/abono")
    public String registrarAbono(@PathVariable Long id, @RequestParam double monto,
                                 RedirectAttributes redirect) {
        try {
            pedidoService.registrarAbono(id, monto);
            redirect.addFlashAttribute("mensaje", "El abono se registró correctamente.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }
}
