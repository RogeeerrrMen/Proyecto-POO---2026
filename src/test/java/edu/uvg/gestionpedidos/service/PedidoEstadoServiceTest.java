package edu.uvg.gestionpedidos.service;

import java.util.Optional;
import java.util.List;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.entity.DireccionEntrega;
import edu.uvg.gestionpedidos.entity.EstadoPedido;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.repository.ClienteRepository;
import edu.uvg.gestionpedidos.repository.PedidoRepository;
import edu.uvg.gestionpedidos.repository.ProductoRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.uvg.gestionpedidos.entity.Emprendimiento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoEstadoServiceTest {

    private PedidoRepository pedidoRepository;
    private PedidoService pedidoService;
    private Pedido pedido;
    private static final Long EMPRENDIMIENTO_ID = 42L;
    private static final String CORREO = "prueba@example.com";

    
    @BeforeEach
    void preparar() {

        pedidoRepository = mock(PedidoRepository.class);

        EmprendimientoService emprendimientoService =
                mock(EmprendimientoService.class);

        Emprendimiento emprendimiento =
                mock(Emprendimiento.class);

        when(emprendimiento.getId())
                .thenReturn(EMPRENDIMIENTO_ID);

        when(emprendimientoService.buscarMiEmprendimiento(CORREO))
                .thenReturn(Optional.of(emprendimiento));

        pedidoService = new PedidoService(
                pedidoRepository,
                mock(ClienteRepository.class),
                mock(ProductoRepository.class),
                emprendimientoService
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        "password",
                        List.of()
                )
        );

        Cliente cliente = new Cliente("Ana", "12345678");

        DireccionEntrega direccion = new DireccionEntrega();
        direccion.setNombreReceptor("Ana");
        direccion.setTelefonoReceptor("12345678");
        direccion.setDireccion("Zona 1, Ciudad de Guatemala");

        pedido = new Pedido("PED-PRUEBA", cliente, direccion);

        when(pedidoRepository.findByIdAndEmprendimientoId(
                1L,
                EMPRENDIMIENTO_ID))
                .thenReturn(Optional.of(pedido));

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

    }

    @AfterEach
    void limpiarSeguridad() {

        SecurityContextHolder.clearContext();
    }


    @Test
    void avanzarHastaEntregado() {
        EstadoPedido[] recorrido = {
                EstadoPedido.CONFIRMADO,
                EstadoPedido.EN_PREPARACION,
                EstadoPedido.LISTO,
                EstadoPedido.ENVIADO,
                EstadoPedido.ENTREGADO
        };

        for (EstadoPedido estado : recorrido) {
            Pedido resultado = pedidoService.actualizarEstado(1L, estado);

            assertSame(pedido, resultado);
            assertEquals(estado, pedido.getEstado());
        }

        verify(pedidoRepository, times(5)).save(pedido);
    }

    @Test
    void cancelarPedidoRegistrado() {
        pedidoService.actualizarEstado(1L, EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void cancelarPedidoConfirmado() {
        pedido.confirmar();

        pedidoService.actualizarEstado(1L, EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void marcarEntregaFallida() {
        prepararPedidoEnviado();

        pedidoService.actualizarEstado(1L, EstadoPedido.FALLIDO);

        assertEquals(EstadoPedido.FALLIDO, pedido.getEstado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void rechazarSaltoDeRegistradoAEntregado() {
        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(1L, EstadoPedido.ENTREGADO));

        assertEquals(EstadoPedido.REGISTRADO, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void rechazarCancelacionDurantePreparacion() {
        pedido.confirmar();
        pedido.iniciarPreparacion();

        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(1L, EstadoPedido.CANCELADO));

        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void rechazarRetrocesoYCambioAlMismoEstado() {
        pedido.confirmar();

        for (EstadoPedido destino : new EstadoPedido[] {
                EstadoPedido.REGISTRADO,
                EstadoPedido.CONFIRMADO
        }) {
            assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(1L, destino));

            assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
        }

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void impedirCambiosDesdeEntregado() {
        prepararPedidoEnviado();
        pedido.marcarEntregado();

        comprobarEstadoFinal();
    }

    @Test
    void impedirCambiosDesdeFallido() {
        prepararPedidoEnviado();
        pedido.marcarFallido();

        comprobarEstadoFinal();
    }

    @Test
    void impedirCambiosDesdeCancelado() {
        pedido.cancelar();

        comprobarEstadoFinal();
    }

    @Test
    void rechazarEstadoNulo() {
        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(1L, null));

        assertEquals(EstadoPedido.REGISTRADO, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void rechazarPedidoInexistente() {

        when(pedidoRepository.findByIdAndEmprendimientoId(
        99L,
        EMPRENDIMIENTO_ID))
        .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(99L, EstadoPedido.CONFIRMADO));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void rechazarIdentificadoresInvalidos() {
        for (Long id : new Long[] {null, 0L, -1L}) {
            assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(id, EstadoPedido.CONFIRMADO));
        }

        verifyNoInteractions(pedidoRepository);
    }

    private void prepararPedidoEnviado() {
        pedido.confirmar();
        pedido.iniciarPreparacion();
        pedido.marcarListo();
        pedido.marcarEnviado();
    }

    private void comprobarEstadoFinal() {
        EstadoPedido estadoOriginal = pedido.getEstado();

        for (EstadoPedido destino : EstadoPedido.values()) {
            assertThrows(IllegalArgumentException.class, () -> pedidoService.actualizarEstado(1L, destino));

            assertEquals(estadoOriginal, pedido.getEstado());
        }

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}