package edu.uvg.gestionpedidos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;

import edu.uvg.gestionpedidos.dto.DetallePedidoDTO;
import edu.uvg.gestionpedidos.dto.PedidoDTO;
import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.EstadoPedido;
import edu.uvg.gestionpedidos.entity.ModalidadEntrega;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.entity.Producto;
import edu.uvg.gestionpedidos.repository.ClienteRepository;
import edu.uvg.gestionpedidos.repository.PedidoRepository;
import edu.uvg.gestionpedidos.repository.ProductoRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private ClienteRepository clienteRepository;
    private ProductoRepository productoRepository;
    private PedidoService pedidoService;
    private EmprendimientoService emprendimientoService;
    private Emprendimiento emprendimiento;
    private static final String CORREO = "prueba@example.com";
    private static final Long EMPRENDIMIENTO_ID = 42L;

    @BeforeEach
    void preparar() {
        pedidoRepository = mock(PedidoRepository.class);
        clienteRepository = mock(ClienteRepository.class);
        productoRepository = mock(ProductoRepository.class);

        emprendimientoService = mock(EmprendimientoService.class);
        emprendimiento = new Emprendimiento("Negocio de prueba", new Usuario());

        try {

            Field campoId = Emprendimiento.class.getDeclaredField("id");
            campoId.setAccessible(true);
            campoId.set(emprendimiento, EMPRENDIMIENTO_ID);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "No se pudo configurar el emprendimiento de prueba.", e);
        }

        when(emprendimientoService.buscarMiEmprendimiento(CORREO))
                .thenReturn(Optional.of(emprendimiento));

        pedidoService = new PedidoService(
                pedidoRepository,
                clienteRepository,
                productoRepository,
                emprendimientoService);
        
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        "password",
                        List.of()
                )
        );
    }

    private DetallePedidoDTO crearDetalle(Long productoId, int cantidad) {
        DetallePedidoDTO detalle = new DetallePedidoDTO();
        detalle.setProductoId(productoId);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(new BigDecimal(productoId == 20L ? "7.25" : "25.50"));
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

    @AfterEach
    void limpiarSeguridad() {

        SecurityContextHolder.clearContext();
    }

    
    private void prepararClienteExistente() {

        when(clienteRepository.findByIdAndEmprendimientoId(
                1L,
                EMPRENDIMIENTO_ID))
                .thenReturn(Optional.of(
                        new Cliente("Ana", "12345678")
                ));
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

        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(producto));

        Pedido pedido = pedidoService.registrar(crearFormulario(), CORREO);

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

        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(crearProducto(25.50)));

        when(productoRepository.findByIdAndEmprendimientoId(20L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(crearProducto(7.25)));

        PedidoDTO datos = crearFormulario();
        datos.getDetalles().add(crearDetalle(20L, 3));

        Pedido pedido = pedidoService.registrar(datos, CORREO);

        assertEquals(2, pedido.getDetalles().size());

        comprobarMonto("21.75", pedido.getDetalles().get(1).getSubtotal());

        comprobarMonto("72.75", pedido.getTotal());

        verify(pedidoRepository).save(pedido);
    }

    @Test
    void rechazarPedidoSinCliente() {
        PedidoDTO datos = crearFormulario();
        datos.setClienteId(null);

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(datos, CORREO));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarClienteInexistente() {

        when(clienteRepository.findByIdAndEmprendimientoId(
        1L,
        EMPRENDIMIENTO_ID))
        .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(crearFormulario(), CORREO));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarPedidoSinProductos() {
        PedidoDTO datos = crearFormulario();
        datos.getDetalles().clear();

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(datos, CORREO));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarCantidadesInvalidas() {
        for (int cantidad : new int[] {0, -2}) {
            PedidoDTO datos = crearFormulario();
            datos.getDetalles().get(0).setCantidad(cantidad);

            assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(datos, CORREO));
        }

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarProductoInexistente() {
        prepararClienteExistente();

        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(crearFormulario(), CORREO));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void rechazarProductoInactivo() {
        prepararClienteExistente();

        Producto producto = crearProducto(25.50);
        producto.desactivar();

        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(producto));

        assertThrows(
                IllegalArgumentException.class,() -> pedidoService.registrar(crearFormulario(), CORREO));

        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void consultarPedidoPorId() {
        prepararClienteExistente();
        prepararGuardado();

        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(crearProducto(25.50)));

        Pedido registrado = pedidoService.registrar(crearFormulario(), CORREO);

        when(pedidoRepository.findByIdAndEmprendimientoId(
        5L,
        EMPRENDIMIENTO_ID))
        .thenReturn(Optional.of(registrado));

        Pedido consultado = pedidoService.obtenerPorId(5L);

        assertSame(registrado, consultado);
        comprobarMonto("51.00", consultado.getTotal());
    }

    @Test
    void listarPedidos() {

        when(pedidoRepository
        .findByEmprendimientoIdOrderByFechaPedidoDescIdDesc(
                EMPRENDIMIENTO_ID))
        .thenReturn(List.of());

        assertTrue(pedidoService.listar().isEmpty());

        verify(pedidoRepository)
        .findByEmprendimientoIdOrderByFechaPedidoDescIdDesc(
                EMPRENDIMIENTO_ID);
    }

    @Test
    void registrarFechasSinHoras() {
        prepararClienteExistente();
        prepararGuardado();

        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(crearProducto(25.50)));

        PedidoDTO datos = crearFormulario();
        LocalDate fecha = LocalDate.of(2026, 11, 5);

        datos.setFechaPreparacion(fecha);
        datos.setFechaEnvio(fecha.plusDays(1));
        datos.setFechaEntrega(fecha.plusDays(2));

        Pedido pedido = pedidoService.registrar(datos, CORREO);

        assertEquals(fecha, pedido.getFechaPreparacion());
        assertEquals(fecha.plusDays(1), pedido.getFechaEnvio());
        assertEquals(fecha.plusDays(2), pedido.getFechaEntrega());

        assertNull(pedido.getHoraPreparacion());
        assertNull(pedido.getHoraEnvio());
        assertNull(pedido.getHoraEntrega());

        verify(pedidoRepository).save(pedido);
    }

    @Test
    void registrarFechasConHoras() {
        prepararClienteExistente();
        prepararGuardado();

        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(crearProducto(25.50)));

        PedidoDTO datos = crearFormulario();
        LocalDate fecha = LocalDate.of(2026, 11, 5);

        datos.setFechaPreparacion(fecha);
        datos.setHoraPreparacion(LocalTime.of(8, 30));

        datos.setFechaEnvio(fecha.plusDays(1));
        datos.setHoraEnvio(LocalTime.of(10, 0));

        datos.setFechaEntrega(fecha.plusDays(2));
        datos.setHoraEntrega(LocalTime.MIDNIGHT);

        Pedido pedido = pedidoService.registrar(datos, CORREO);

        assertEquals(fecha, pedido.getFechaPreparacion());
        assertEquals(fecha.plusDays(1), pedido.getFechaEnvio());
        assertEquals(fecha.plusDays(2), pedido.getFechaEntrega());

        assertEquals(LocalTime.of(8, 30), pedido.getHoraPreparacion());
        assertEquals(LocalTime.of(10, 0), pedido.getHoraEnvio());
        assertEquals(LocalTime.MIDNIGHT, pedido.getHoraEntrega());

        verify(pedidoRepository).save(pedido);
    }

    @Test
    void rechazarHorasSinFechas() {
        PedidoDTO preparacion = crearFormulario();
        preparacion.setHoraPreparacion(LocalTime.of(8, 30));

        PedidoDTO envio = crearFormulario();
        envio.setHoraEnvio(LocalTime.of(10, 0));

        PedidoDTO entrega = crearFormulario();
        entrega.setHoraEntrega(LocalTime.of(14, 0));

        for (PedidoDTO datos : List.of(preparacion, envio, entrega)) {
            assertThrows(IllegalArgumentException.class,() -> pedidoService.registrar(datos, CORREO));
        }

        verifyNoInteractions(pedidoRepository, clienteRepository, productoRepository);
    }
    @Test
    void registrarPrecioAcordadoSinModificarCatalogo() {
        prepararClienteExistente();
        prepararGuardado();
        Producto producto = crearProducto(100.00);
        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(producto));
        PedidoDTO datos = crearFormulario();
        datos.getDetalles().get(0).setPrecioUnitario(new BigDecimal("90.00"));

        Pedido pedido = pedidoService.registrar(datos, CORREO);

        comprobarMonto("90.00", pedido.getDetalles().get(0).getPrecioUnitario());
        comprobarMonto("180.00", pedido.getTotal());
        assertEquals(100.00, producto.getPrecio());
        verify(productoRepository, never()).save(any(Producto.class));
        producto.actualizarPrecio(120.00);
        comprobarMonto("180.00", pedido.getTotal());
    }

    @Test
    void rechazarPrecioAusenteNegativoOConFraccionesDeCentavo() {
        for (BigDecimal precio : new BigDecimal[] {
                null, new BigDecimal("-1.00"), new BigDecimal("10.001")}) {
            PedidoDTO datos = crearFormulario();
            datos.getDetalles().get(0).setPrecioUnitario(precio);
            prepararClienteExistente();
            when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(crearProducto(25.50)));

            assertThrows(IllegalArgumentException.class, () -> pedidoService.registrar(datos, CORREO));
        }
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void permitirPrecioCero() {
        prepararClienteExistente();
        prepararGuardado();
        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID)).thenReturn(Optional.of(crearProducto(25.50)));
        PedidoDTO datos = crearFormulario();
        datos.getDetalles().get(0).setPrecioUnitario(BigDecimal.ZERO);

        comprobarMonto("0.00", pedidoService.registrar(datos, CORREO).getTotal());
    }
    @Test
    void copiarFlujoYConservarloCuandoCambiaLaConfiguracion() {
        prepararClienteExistente();
        prepararGuardado();
        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID))
                .thenReturn(Optional.of(crearProducto(25.50)));

        List<EstadoPedido> flujoInicial = List.of(
                EstadoPedido.REGISTRADO,
                EstadoPedido.LISTO,
                EstadoPedido.ENVIADO,
                EstadoPedido.ENTREGADO);
        emprendimiento.actualizarFlujoEstados(flujoInicial);

        Pedido anterior = pedidoService.registrar(crearFormulario(), CORREO);
        assertSame(emprendimiento, anterior.getEmprendimiento());
        assertEquals(flujoInicial, anterior.getFlujoEstados());
        assertTrue(anterior.getEstadosPermitidos().contains(EstadoPedido.LISTO));
        assertFalse(anterior.getEstadosPermitidos().contains(EstadoPedido.CONFIRMADO));
        assertThrows(IllegalArgumentException.class, anterior::confirmar);

        List<EstadoPedido> flujoNuevo = List.of(
                EstadoPedido.REGISTRADO,
                EstadoPedido.CONFIRMADO,
                EstadoPedido.ENVIADO,
                EstadoPedido.ENTREGADO);
        emprendimiento.actualizarFlujoEstados(flujoNuevo);

        Pedido nuevo = pedidoService.registrar(crearFormulario(), CORREO);
        assertEquals(flujoInicial, anterior.getFlujoEstados());
        assertEquals(flujoNuevo, nuevo.getFlujoEstados());
    }

    @Test
    void recogerSinDireccionUsandoElFlujoConfigurado() {
        prepararClienteExistente();
        prepararGuardado();
        when(productoRepository.findByIdAndEmprendimientoId(10L, EMPRENDIMIENTO_ID))
                .thenReturn(Optional.of(crearProducto(25.50)));
        emprendimiento.actualizarFlujoEstados(List.of(
                EstadoPedido.REGISTRADO,
                EstadoPedido.LISTO,
                EstadoPedido.ENVIADO,
                EstadoPedido.ENTREGADO));
        PedidoDTO datos = crearFormulario();
        datos.setModalidadEntrega(ModalidadEntrega.RECOGER_NEGOCIO);
        datos.setDireccion(null);
        datos.setNombreReceptor(null);
        datos.setTelefonoReceptor(null);

        Pedido pedido = pedidoService.registrar(datos, CORREO);
        assertNull(pedido.getDireccionEntrega());
        pedido.marcarListo();
        assertFalse(pedido.getEstadosPermitidos().contains(EstadoPedido.ENVIADO));
        assertTrue(pedido.getEstadosPermitidos().contains(EstadoPedido.ENTREGADO));
        assertThrows(IllegalArgumentException.class, pedido::marcarEnviado);
        pedido.marcarEntregado();
        assertTrue(pedido.getEstadosPermitidos().isEmpty());
    }

    @Test
    void rechazarRegistroSinEmprendimiento() {
        when(emprendimientoService.buscarMiEmprendimiento(CORREO))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.registrar(crearFormulario(), CORREO));
        verifyNoInteractions(pedidoRepository, clienteRepository, productoRepository);
    }
}
