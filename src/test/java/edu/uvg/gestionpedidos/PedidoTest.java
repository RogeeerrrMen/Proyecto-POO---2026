package edu.uvg.gestionpedidos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Producto producto;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Pastel de chocolate");
        producto.setPrecio(50.0);

        Cliente cliente = new Cliente("Ana López", "55551234", null);
        pedido = new Pedido("PED-001", cliente, null);
    }

    @Test
    void seCreaRegistradoPorDefecto() {
        assertEquals(EstadoPedido.REGISTRADO, pedido.getEstado());
    }

    @Test
    void agregarProductoCalculaElTotalCorrectamente() {
        pedido.agregarProducto(producto, 3);
        assertEquals(150.0, pedido.calcularTotal());
    }

    @Test
    void eliminarProductoQuitaEseDetalle() {
        pedido.agregarProducto(producto, 2);
        pedido.eliminarProducto(producto);
        assertEquals(0, pedido.getDetalles().size());
    }

    @Test
    void elFlujoCompletoDeEstadosFunciona() {
        pedido.confirmar();
        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());

        pedido.iniciarPreparacion();
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());

        pedido.marcarListo();
        assertEquals(EstadoPedido.LISTO, pedido.getEstado());

        pedido.marcarEnviado();
        assertEquals(EstadoPedido.ENVIADO, pedido.getEstado());

        pedido.marcarEntregado();
        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
    }

    @Test
    void noPermiteSaltarDeRegistradoAEntregado() {
        assertThrows(IllegalStateException.class, () -> pedido.marcarEntregado());
    }

    @Test
    void noPermiteConfirmarDosVeces() {
        pedido.confirmar();
        assertThrows(IllegalStateException.class, () -> pedido.confirmar());
    }

    @Test
    void cancelarFuncionaAntesDeEnviar() {
        pedido.confirmar();
        pedido.cancelar();
        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
    }

    @Test
    void noPermiteCancelarUnPedidoYaEntregado() {
        pedido.confirmar();
        pedido.iniciarPreparacion();
        pedido.marcarListo();
        pedido.marcarEnviado();
        pedido.marcarEntregado();
        assertThrows(IllegalStateException.class, () -> pedido.cancelar());
    }
}
