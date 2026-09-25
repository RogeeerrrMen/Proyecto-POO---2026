package edu.uvg.gestionpedidos.controller;

import edu.uvg.gestionpedidos.model.Producto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class ProductoController {

    // Lista temporal en memoria: todavía no existe Repository ni base de datos.
    private final List<Producto> productos = new ArrayList<>();
    private final AtomicLong contadorId = new AtomicLong(1);

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productos);
        return "productos";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        return "formulario-producto";
    }

    @PostMapping("/productos")
    public String registrarProducto(@ModelAttribute Producto producto) {
        producto.setId(contadorId.getAndIncrement());
        producto.activar();
        productos.add(producto);
        return "redirect:/productos";
    }
}
