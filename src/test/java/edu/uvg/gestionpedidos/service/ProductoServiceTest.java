package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.ProductoDTO;
import edu.uvg.gestionpedidos.entity.Producto;
import edu.uvg.gestionpedidos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del ProductoService con el repositorio simulado: no necesitan PostgreSQL.
 * La prueba con la base de datos real se hace corriendo la aplicación (ver guía de la entrega).
 */
class ProductoServiceTest {

    private ProductoRepository repositorio;
    private ProductoService servicio;

    @BeforeEach
    void setUp() {
        repositorio = mock(ProductoRepository.class);
        servicio = new ProductoService(repositorio);

        // Simula la base de datos: save() devuelve el mismo objeto que recibe.
        when(repositorio.save(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private ProductoDTO datos(String nombre, String descripcion, Double precio) {
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setPrecio(precio);
        return dto;
    }

    /** Deja registrado en el repositorio simulado un producto con id 1. */
    private Producto productoExistente() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Pastel de chocolate");
        producto.setDescripcion("Porción grande");
        producto.actualizarPrecio(50.0);
        producto.activar();
        when(repositorio.findById(1L)).thenReturn(Optional.of(producto));
        return producto;
    }

    private void assertRegistroRechazado(ProductoDTO datosInvalidos) {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarProducto(datosInvalidos));
        verify(repositorio, never()).save(any(Producto.class));
    }

    // --- Registro ---

    @Test
    void registrarProductoGuardaUnProductoActivoConLosDatosLimpios() {
        Producto devuelto = servicio.registrarProducto(datos("  Pastel  ", "  Con cobertura  ", 50.0));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repositorio).save(captor.capture());
        Producto guardado = captor.getValue();

        assertEquals("Pastel", guardado.getNombre());
        assertEquals("Con cobertura", guardado.getDescripcion());
        assertEquals(50.0, guardado.getPrecio());
        assertTrue(guardado.isActivo());
        assertSame(guardado, devuelto);
    }

    @Test
    void registrarProductoSinDescripcionLaDejaVacia() {
        servicio.registrarProducto(datos("Galletas", "   ", 12.0));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repositorio).save(captor.capture());
        assertNull(captor.getValue().getDescripcion());
    }

    @Test
    void registrarProductoAceptaPrecioCero() {
        Producto devuelto = servicio.registrarProducto(datos("Muestra gratis", null, 0.0));
        assertEquals(0.0, devuelto.getPrecio());
    }

    @Test
    void registrarProductoRechazaNombreVacioONulo() {
        assertRegistroRechazado(datos("   ", null, 10.0));
        assertRegistroRechazado(datos(null, null, 10.0));
    }

    @Test
    void registrarProductoRechazaTextosDemasiadoLargos() {
        assertRegistroRechazado(datos("a".repeat(121), null, 10.0));
        assertRegistroRechazado(datos("Pastel", "d".repeat(501), 10.0));
    }

    @Test
    void registrarProductoRechazaPreciosInvalidos() {
        assertRegistroRechazado(datos("Pastel", null, null));
        assertRegistroRechazado(datos("Pastel", null, -5.0));
        assertRegistroRechazado(datos("Pastel", null, Double.NaN));
    }

    @Test
    void registrarProductoRechazaDatosNulos() {
        assertRegistroRechazado(null);
    }

    // --- Modificación ---

    @Test
    void modificarProductoActualizaLosDatosYConservaSuEstado() {
        Producto existente = productoExistente();
        existente.desactivar();

        Producto devuelto = servicio.modificarProducto(1L, datos(" Pastel de vainilla ", "Nueva receta", 65.5));

        assertEquals("Pastel de vainilla", devuelto.getNombre());
        assertEquals("Nueva receta", devuelto.getDescripcion());
        assertEquals(65.5, devuelto.getPrecio());
        assertFalse(devuelto.isActivo());
        verify(repositorio).save(existente);
    }

    @Test
    void modificarProductoInexistenteLanzaError() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> servicio.modificarProducto(99L, datos("Pastel", null, 10.0)));
        verify(repositorio, never()).save(any(Producto.class));
    }

    @Test
    void modificarProductoConPrecioInvalidoNoCambiaNingunDato() {
        Producto existente = productoExistente();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificarProducto(1L, datos("Otro nombre", "Otra descripción", -1.0)));

        assertEquals("Pastel de chocolate", existente.getNombre());
        assertEquals("Porción grande", existente.getDescripcion());
        assertEquals(50.0, existente.getPrecio());
        verify(repositorio, never()).save(any(Producto.class));
    }

    @Test
    void modificarProductoConNombreVacioNoCambiaNingunDato() {
        Producto existente = productoExistente();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificarProducto(1L, datos("  ", "Otra descripción", 20.0)));

        assertEquals("Pastel de chocolate", existente.getNombre());
        assertEquals(50.0, existente.getPrecio());
        verify(repositorio, never()).save(any(Producto.class));
    }

    // --- Activar y desactivar ---

    @Test
    void desactivarProductoLoDeshabilitaSinEliminarlo() {
        Producto existente = productoExistente();

        Producto devuelto = servicio.desactivarProducto(1L);

        assertFalse(devuelto.isActivo());
        verify(repositorio).save(existente);
        verify(repositorio, never()).delete(any(Producto.class));
    }

    @Test
    void activarProductoLoHabilitaDeNuevo() {
        Producto existente = productoExistente();
        existente.desactivar();

        Producto devuelto = servicio.activarProducto(1L);

        assertTrue(devuelto.isActivo());
        verify(repositorio).save(existente);
    }

    @Test
    void activarOdesactivarUnProductoInexistenteLanzaError() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> servicio.activarProducto(99L));
        assertThrows(NoSuchElementException.class, () -> servicio.desactivarProducto(99L));
    }

    // --- Consulta y búsqueda ---

    @Test
    void listarProductosDevuelveLosDelRepositorio() {
        Producto existente = productoExistente();
        when(repositorio.findAllByOrderByIdAsc()).thenReturn(List.of(existente));

        assertEquals(List.of(existente), servicio.listarProductos());
    }

    @Test
    void listarProductosActivosUsaLaConsultaDeActivos() {
        Producto existente = productoExistente();
        when(repositorio.findByActivoTrueOrderByIdAsc()).thenReturn(List.of(existente));

        assertEquals(List.of(existente), servicio.listarProductosActivos());
    }

    @Test
    void buscarProductoPorIdDevuelveElProductoOVacio() {
        Producto existente = productoExistente();
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertEquals(Optional.of(existente), servicio.buscarProductoPorId(1L));
        assertTrue(servicio.buscarProductoPorId(99L).isEmpty());
    }

    @Test
    void buscarProductosPorNombreUsaElTextoSinEspacios() {
        Producto existente = productoExistente();
        when(repositorio.findByNombreContainingIgnoreCaseOrderByIdAsc("pastel")).thenReturn(List.of(existente));

        assertEquals(List.of(existente), servicio.buscarProductosPorNombre("  pastel "));
    }

    @Test
    void buscarProductosPorNombreSinTextoDevuelveTodos() {
        Producto existente = productoExistente();
        when(repositorio.findAllByOrderByIdAsc()).thenReturn(List.of(existente));

        assertEquals(List.of(existente), servicio.buscarProductosPorNombre("   "));
        assertEquals(List.of(existente), servicio.buscarProductosPorNombre(null));
        verify(repositorio, never()).findByNombreContainingIgnoreCaseOrderByIdAsc(anyString());
    }
}
