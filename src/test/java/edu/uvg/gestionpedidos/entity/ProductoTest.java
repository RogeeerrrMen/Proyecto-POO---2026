package edu.uvg.gestionpedidos.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setNombre("Pastel de chocolate");
        producto.actualizarPrecio(50.0);
    }

    @Test
    void activarYDesactivarCambianElEstado() {
        producto.activar();
        assertTrue(producto.isActivo());

        producto.desactivar();
        assertFalse(producto.isActivo());
    }

    @Test
    void actualizarPrecioGuardaElNuevoPrecio() {
        producto.actualizarPrecio(65.5);
        assertEquals(65.5, producto.getPrecio());
    }

    @Test
    void actualizarPrecioAceptaCero() {
        producto.actualizarPrecio(0);
        assertEquals(0.0, producto.getPrecio());
    }

    @Test
    void actualizarPrecioRechazaNegativosYConservaElPrecioAnterior() {
        assertThrows(IllegalArgumentException.class, () -> producto.actualizarPrecio(-1));
        assertEquals(50.0, producto.getPrecio());
    }

    @Test
    void actualizarPrecioRechazaValoresQueNoSonNumeros() {
        assertThrows(IllegalArgumentException.class, () -> producto.actualizarPrecio(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> producto.actualizarPrecio(Double.POSITIVE_INFINITY));
        assertEquals(50.0, producto.getPrecio());
    }
}
