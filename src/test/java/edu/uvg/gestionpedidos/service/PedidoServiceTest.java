package edu.uvg.gestionpedidos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.uvg.gestionpedidos.dto.DetallePedidoDTO;
import edu.uvg.gestionpedidos.dto.PedidoDTO;
import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.entity.Producto;
import edu.uvg.gestionpedidos.repository.ClienteRepository;
import edu.uvg.gestionpedidos.repository.PedidoRepository;
import edu.uvg.gestionpedidos.repository.ProductoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private ClienteRepository clienteRepository;
    private ProductoRepository productoRepository;
    private PedidoService pedidoService;

    @BeforeEach
    void preparar() {
        pedidoRepository = mock(PedidoRepository.class);
        clienteRepository = mock(ClienteRepository.class);
        productoRepository = mock(ProductoRepository.class);

        pedidoService = new PedidoService(pedidoRepository, clienteRepository, productoRepository);
    }

    private DetallePedidoDTO crearDetalle(Long productoId, int cantidad) {
        DetallePedidoDTO detalle = new DetallePedidoDTO();
        detalle.setProductoId(productoId);
        detalle.setCantidad(cantidad);
        return detalle;
    }

    private PedidoDTO crearFormulario() {
        PedidoDTO datos = new PedidoDTO();

        datos.setClienteId(1L);
        datos.setNombreReceptor("Ana");
        datos.setTelefonoReceptor("12345678");
        datos.setDireccion("Zona 1, Ciudad de Guatemala");

        datos.getDetalles().add(crearDetalle(10L, 2));

        return datos;
    }

    private Producto crearProducto(double precio) {
        Producto producto = new Producto();
        producto.setNombre("Producto de prueba");
        producto.setPrecio(precio);
        producto.activar();
        return producto;
    }

    private void prepararClienteExistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(new Cliente("Ana", "12345678")));
    }

    private void prepararGuardado() {
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void comprobarMonto(String esperado, BigDecimal obtenido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtenido));
    }

    @Test
    void registrarPedidoConUnProducto() {
        prepararClienteExistente();
        prepararGuardado();

        Producto producto = crearProducto(25.50);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        Pedido pedido = pedidoService.registrar(crearFormulario());

        comprobarMonto("51.00", pedido.getTotal());
        comprobarMonto("25.50", pedido.getDetalles().get(0).getPrecioUnitario());

        assertNotNull(pedido.getFechaPedido());
        assertTrue(pedido.getCodigo().startsWith("PED-"));

        verify(pedidoRepository).save(pedido);

        // Cambiar el catálogo no debe cambiar el precio del pedido.
        producto.actualizarPrecio(40.00);

        comprobarMonto("51.00", pedido.getTotal());
    }

    @Test
    void registrarPedidoConVariosProductos() {
        prepararClienteExistente();
        prepararGuardado();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(crearProducto(25.50)));

        when(productoRepository.findById(20L)).thenReturn(Optional.of(crearProducto(7.25)));

        PedidoDTO datos = crearFormulario();
        datos.getDetalles().add(crearDetalle(20L, 3));

        Pedido pedido = pedidoService.registrar(datos);

        assertEquals(2, pedido.getDetalles().size());

        comprobarMonto("21.75", pedido.getDetalles().get(1).getSubtotal());

        comprobarMonto("72.75", pedido.getTotal());

        verify(pedidoRepository).save(pedido);
    }

    @Test
    void rechazarPedidoSinCliente() {
        PedidoDTO datos = crearFormulario();
        datos.setClienteId(null);

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(datos));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarClienteInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(crearFormulario()));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarPedidoSinProductos() {
        PedidoDTO datos = crearFormulario();
        datos.getDetalles().clear();

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(datos));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarCantidadesInvalidas() {
        for (int cantidad : new int[] {0, -2}) {
            PedidoDTO datos = crearFormulario();
            datos.getDetalles().get(0).setCantidad(cantidad);

            assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(datos));
        }

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarProductoInexistente() {
        prepararClienteExistente();

        when(productoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(crearFormulario()));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarProductoInactivo() {
        prepararClienteExistente();

        Producto producto = crearProducto(25.50);
        producto.desactivar();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(
                IllegalArgumentException.class,() -> pedidoService.registrar(crearFormulario()));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void consultarPedidoPorId() {
        prepararClienteExistente();
        prepararGuardado();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(crearProducto(25.50)));

        Pedido registrado = pedidoService.registrar(crearFormulario());

        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(registrado));

        Pedido consultado = pedidoService.obtenerPorId(5L);

        assertSame(registrado, consultado);
        comprobarMonto("51.00", consultado.getTotal());
    }

    @Test
    void listarPedidos() {
        when(pedidoRepository.findAllByOrderByFechaPedidoDescIdDesc()).thenReturn(List.of());

        assertTrue(pedidoService.listar().isEmpty());

        verify(pedidoRepository).findAllByOrderByFechaPedidoDescIdDesc();
    }
}