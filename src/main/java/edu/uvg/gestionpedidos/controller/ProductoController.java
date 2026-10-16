package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.dto.ProductoDTO;
import edu.uvg.gestionpedidos.entity.Producto;
import edu.uvg.gestionpedidos.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

/**
 * Coordina las solicitudes HTTP de productos. No contiene reglas de negocio:
 * delega en ProductoService y muestra el resultado en las vistas.
 */
@Controller
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarProductos());
        return "productos";
    }

    @GetMapping("/productos/buscar")
    public String buscarProducto(@RequestParam(name = "nombre", defaultValue = "") String nombre,
                                 Model model) {
        model.addAttribute("productos", productoService.buscarProductosPorNombre(nombre));
        model.addAttribute("busqueda", nombre.trim());
        return "productos";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        return "formulario-producto";
    }

    @PostMapping("/productos")
    public String registrarProducto(@ModelAttribute("producto") ProductoDTO datos,
                                    Model model,
                                    RedirectAttributes redirect) {
        try {
            Producto creado = productoService.registrarProducto(datos);
            redirect.addFlashAttribute("mensaje",
                    "El producto «" + creado.getNombre() + "» se registró correctamente.");
            return "redirect:/productos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "formulario-producto";
        }
    }

    @GetMapping("/productos/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id,
                                           Model model,
                                           RedirectAttributes redirect) {
        return productoService.buscarProductoPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", ProductoDTO.desde(producto));
                    model.addAttribute("productoId", producto.getId());
                    return "formulario-producto";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "El producto no existe.");
                    return "redirect:/productos";
                });
    }

    @PostMapping("/productos/{id}")
    public String modificarProducto(@PathVariable Long id,
                                    @ModelAttribute("producto") ProductoDTO datos,
                                    Model model,
                                    RedirectAttributes redirect) {
        try {
            Producto actualizado = productoService.modificarProducto(id, datos);
            redirect.addFlashAttribute("mensaje",
                    "El producto «" + actualizado.getNombre() + "» se actualizó correctamente.");
            return "redirect:/productos";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productoId", id);
            return "formulario-producto";
        }
    }

    @PostMapping("/productos/{id}/activar")
    public String activarProducto(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Producto producto = productoService.activarProducto(id);
            redirect.addFlashAttribute("mensaje",
                    "El producto «" + producto.getNombre() + "» ahora está activo.");
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @PostMapping("/productos/{id}/desactivar")
    public String desactivarProducto(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Producto producto = productoService.desactivarProducto(id);
            redirect.addFlashAttribute("mensaje",
                    "El producto «" + producto.getNombre() + "» fue desactivado.");
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }
}
