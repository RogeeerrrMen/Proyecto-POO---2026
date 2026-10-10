package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.model.Producto;
import edu.uvg.gestionpedidos.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listar());
        return "productos";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        return "formulario-producto";
    }

    @PostMapping("/productos")
    public String registrarProducto(@ModelAttribute Producto producto) {
        productoService.registrar(producto);
        return "redirect:/productos";
    }
}
