package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Pedido crearPedido() {
        Cliente cliente = new Cliente("Ana", "12345678");

        DireccionEntrega direccion = new DireccionEntrega();
        direccion.setNombreReceptor("Ana");
        direccion.setTelefonoReceptor("12345678");
        direccion.setDireccion("Zona 1, Ciudad de Guatemala");

        return new Pedido("PED-PRUEBA", cliente, direccion);
    }

    private Producto crearProducto(String nombre) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.activar();

        return producto;
    }

    private void comprobarMonto(String esperado, BigDecimal obtenido) {
        assertEquals(
                0,
                new BigDecimal(esperado).compareTo(obtenido),
                "El monto calculado no coincide con el esperado.");
    }

    @Test
    void calcularPedidoConUnProducto() {
        Pedido pedido = crearPedido();

        pedido.agregarProducto(
                crearProducto("Pastel"),
                2,
                new BigDecimal("25.50"));

        pedido.validarParaRegistro();

        assertEquals(1, pedido.getDetalles().size());
        assertEquals(EstadoPedido.REGISTRADO, pedido.getEstado());

        comprobarMonto(
                "51.00",
                pedido.getDetalles().get(0).getSubtotal());

        comprobarMonto("51.00", pedido.getTotal());
    }

    @Test
    void calcularPedidoConVariosProductos() {
        Pedido pedido = crearPedido();

        pedido.agregarProducto(
                crearProducto("Pastel"),
                2,
                new BigDecimal("25.50"));

        pedido.agregarProducto(
                crearProducto("Galleta"),
                3,
                new BigDecimal("7.25"));

        pedido.agregarProducto(
                crearProducto("Chocolate"),
                1,
                new BigDecimal("10.00"));

        pedido.validarParaRegistro();

        assertEquals(3, pedido.getDetalles().size());

        comprobarMonto(
                "21.75",
                pedido.getDetalles().get(1).getSubtotal());

        comprobarMonto("82.75", pedido.getTotal());
    }

    @Test
    void rechazarPedidoSinProductos() {
        Pedido pedido = crearPedido();

        assertThrows(
                IllegalArgumentException.class,
                () -> pedido.validarParaRegistro());
    }

    @Test
    void rechazarCantidadesNoPositivas() {
        Pedido pedido = crearPedido();
        Producto producto = crearProducto("Pastel");

        assertThrows(
                IllegalArgumentException.class,
                () -> pedido.agregarProducto(
                        producto, -2, new BigDecimal("25.50")));

        assertThrows(
                IllegalArgumentException.class,
                () -> pedido.agregarProducto(
                        producto, 0, new BigDecimal("25.50")));

        assertTrue(pedido.getDetalles().isEmpty());
    }

    @Test
    void rechazarProductoInactivo() {
        Pedido pedido = crearPedido();
        Producto producto = crearProducto("Pastel");
        producto.desactivar();

        assertThrows(
                IllegalArgumentException.class,
                () -> pedido.agregarProducto(
                        producto, 1, new BigDecimal("25.50")));
    }

    @Test
    void rechazarPedidoSinCliente() {
        DireccionEntrega direccion = crearPedido().getDireccionEntrega();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Pedido("PED-PRUEBA", null, direccion));
    }

    @Test
    void recalcularTotalAlCambiarCantidad() {
        Pedido pedido = crearPedido();

        pedido.agregarProducto(
                crearProducto("Pastel"),
                2,
                new BigDecimal("25.50"));

        pedido.getDetalles().get(0).cambiarCantidad(3);

        comprobarMonto("76.50", pedido.getTotal());
    }
}