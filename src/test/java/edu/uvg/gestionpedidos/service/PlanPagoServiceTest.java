package edu.uvg.gestionpedidos.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import edu.uvg.gestionpedidos.entity.Pago;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.entity.PlanPago;
import edu.uvg.gestionpedidos.repository.PlanPagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanPagoServiceTest {
    private PagoService pagos;
    private PlanPagoRepository repository;
    private PlanPagoService service;
    private Pago pago;

    @BeforeEach
    void preparar() {
        pagos = mock(PagoService.class); repository = mock(PlanPagoRepository.class);
        Pedido pedido = mock(Pedido.class);
        when(pedido.calcularTotal()).thenReturn(new BigDecimal("100.00"));
        pago = new Pago(pedido);
        when(pagos.obtenerOCrearPago(1L)).thenReturn(pago);
        when(repository.findByPago_Id(null)).thenReturn(Optional.empty());
        service = new PlanPagoService(pagos, repository, Clock.fixed(
                Instant.parse("2026-11-11T18:00:00Z"), ZoneId.of("America/Guatemala")));
    }

    @Test
    void guardarUnPlanSinRegistrarUnCobro() {
        service.crearCuotas(1L, 3, LocalDate.of(2026, 11, 30), "ana@example.com");
        ArgumentCaptor<PlanPago> captor = ArgumentCaptor.forClass(PlanPago.class);
        verify(repository).save(captor.capture());
        assertEquals(3, captor.getValue().getPartes().size());
        assertEquals(0, pago.getTotalAbonado().signum());
        verify(pagos).verificarAcceso(1L, "ana@example.com");
    }

    @Test
    void impedirReemplazarUnPlanExistente() {
        when(repository.findByPago_Id(null)).thenReturn(Optional.of(
                PlanPago.crearCuotas(pago, 2, LocalDate.of(2026, 11, 30))));
        assertThrows(IllegalArgumentException.class, () -> service.crearCuotas(
                1L, 3, LocalDate.of(2026, 11, 30), "ana@example.com"));
        verify(repository, never()).save(any(PlanPago.class));
    }

    @Test
    void impedirCrearUnPlanParaUnPagoCompletado() {
        pago.registrarAbono(new BigDecimal("100"));
        assertThrows(IllegalArgumentException.class, () -> service.crearCuotas(
                1L, 3, LocalDate.of(2026, 11, 30), "ana@example.com"));
        verify(repository, never()).save(any(PlanPago.class));
    }

    @Test
    void impedirCrearPlanParaOtraCuenta() {
        doThrow(new AccessDeniedException("No disponible")).when(pagos)
                .verificarAcceso(1L, "otra@example.com");
        assertThrows(AccessDeniedException.class, () -> service.crearCuotas(
                1L, 3, LocalDate.of(2026, 11, 30), "otra@example.com"));
        verifyNoInteractions(repository);
    }

    @Test
    void consultarPlanComoDtoConEstadoVencido() {
        PlanPago plan = PlanPago.crearCuotas(pago, 2, LocalDate.of(2026, 11, 10));
        when(repository.findByPago_Pedido_Id(1L)).thenReturn(Optional.of(plan));
        assertEquals("VENCIDO", service.consultar(1L, "ana@example.com")
                .getPartes().get(0).getEstado());
    }
}
