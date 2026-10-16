package edu.uvg.gestionpedidos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uvg.gestionpedidos.dto.DetallePedidoDTO;
import edu.uvg.gestionpedidos.dto.PedidoDTO;
import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.entity.DireccionEntrega;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.entity.Producto;
import edu.uvg.gestionpedidos.repository.ClienteRepository;
import edu.uvg.gestionpedidos.repository.PedidoRepository;
import edu.uvg.gestionpedidos.repository.ProductoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository, ProductoRepository productoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Pedido registrar(PedidoDTO datos) {
        validarFormulario(datos);

        Cliente cliente = clienteRepository.findById(datos.getClienteId()).orElseThrow(() -> new IllegalArgumentException("El cliente seleccionado no existe."));

        DireccionEntrega direccion = crearDireccion(datos);

        String codigo = "PED-" + UUID.randomUUID();

        Pedido pedido = new Pedido(codigo, cliente, direccion);

        pedido.setFechaPreparacion(datos.getFechaPreparacion());
        pedido.setFechaEnvio(datos.getFechaEnvio());
        pedido.setFechaEntrega(datos.getFechaEntrega());
        pedido.setObservaciones(limpiarTexto(datos.getObservaciones()));

        for (DetallePedidoDTO detalle : datos.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProductoId()).orElseThrow(() -> new IllegalArgumentException("Uno de los productos seleccionados no existe."));

            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " está inactivo.");
            }

            BigDecimal precioUnitario = obtenerPrecioParaPedido(producto);

            pedido.agregarProducto(producto, detalle.getCantidad(), precioUnitario);
        }

        pedido.validarParaRegistro();

        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return pedidoRepository.findAllByOrderByFechaPedidoDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El identificador del pedido es inválido.");
        }

        return pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El pedido no existe."));
    }

    private BigDecimal obtenerPrecioParaPedido(Producto producto) {
        double precio = producto.getPrecio();

        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("El producto " + producto.getNombre() + " tiene un precio inválido.");
        }

        // Adaptación temporal del catálogo actual.
        // Pendiente: coordinar con Roger Producto.precio como BigDecimal.
        return BigDecimal.valueOf(precio);
    }

    private void validarFormulario(PedidoDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del pedido son obligatorios.");
        }

        if (datos.getClienteId() == null || datos.getClienteId() <= 0) {
            throw new IllegalArgumentException("Debes seleccionar un cliente.");
        }

        if (datos.getDetalles() == null || datos.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debes agregar al menos un producto.");
        }

        for (DetallePedidoDTO detalle : datos.getDetalles()) {
            if (detalle == null) {
                throw new IllegalArgumentException("Uno de los detalles del pedido es inválido.");
            }

            if (detalle.getProductoId() == null || detalle.getProductoId() <= 0) {
                throw new IllegalArgumentException("Debes seleccionar un producto en cada fila.");
            }

            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("Todas las cantidades deben ser mayores que cero.");
            }
        }
    }

    private DireccionEntrega crearDireccion(PedidoDTO datos) {
        DireccionEntrega direccion = new DireccionEntrega();

        direccion.setNombreReceptor(
                limpiarTexto(datos.getNombreReceptor()));

        direccion.setTelefonoReceptor(
                limpiarTexto(datos.getTelefonoReceptor()));

        direccion.setDireccion(limpiarTexto(datos.getDireccion()));
        direccion.setReferencias(limpiarTexto(datos.getReferencias()));
        direccion.setEnlaceMapa(limpiarTexto(datos.getEnlaceMapa()));

        if (!direccion.validarDatos()) {
            throw new IllegalArgumentException("Completa el receptor, un teléfono válido y la dirección.");
        }

        validarLongitud(
                direccion.getNombreReceptor(), "El nombre del receptor");

        validarLongitud(
                direccion.getTelefonoReceptor(), "El teléfono");

        validarLongitud(direccion.getDireccion(), "La dirección");
        validarLongitud(direccion.getReferencias(), "Las referencias");
        validarLongitud(direccion.getEnlaceMapa(), "El enlace del mapa");

        return direccion;
    }

    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }

    private void validarLongitud(String texto, String campo) {
        if (texto != null && texto.length() > 255) {
            throw new IllegalArgumentException(campo + " no puede superar los 255 caracteres.");
        }
    }
}