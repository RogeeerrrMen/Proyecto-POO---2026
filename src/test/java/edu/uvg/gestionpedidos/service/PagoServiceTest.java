package edu.uvg.gestionpedidos.service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import edu.uvg.gestionpedidos.entity.*;
import edu.uvg.gestionpedidos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PagoServiceTest {
    private PagoRepository pagos;
    private PedidoRepository pedidos;
    private AbonoRepository abonos;
    private PlanPagoRepository planes;
    private EmprendimientoService negocios;
    private PagoService service;
    private Pago pago;

    @BeforeEach
    void preparar() throws Exception {
        pagos = mock(PagoRepository.class); pedidos = mock(PedidoRepository.class);
        abonos = mock(AbonoRepository.class); planes = mock(PlanPagoRepository.class);
        negocios = mock(EmprendimientoService.class);
        Pedido pedido = mock(Pedido.class);
        Emprendimiento negocio = mock(Emprendimiento.class);
        when(negocio.getId()).thenReturn(42L);
        when(pedido.getEmprendimiento()).thenReturn(negocio);
        when(pedido.getId()).thenReturn(1L);
        when(pedido.calcularTotal()).thenReturn(new BigDecimal("100.00"));
        pago = new Pago(pedido); asignarId(pago, 7L);
        when(pedidos.buscarParaPago(1L)).thenReturn(Optional.of(pedido));
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidos.existsById(1L)).thenReturn(true);
        when(pagos.findWithLockByPedidoId(1L)).thenReturn(Optional.of(pago));
        when(pagos.findByPedidoId(1L)).thenReturn(Optional.of(pago));
        when(pagos.save(any(Pago.class))).thenAnswer(i -> i.getArgument(0));
        when(planes.findByPago_Id(7L)).thenReturn(Optional.empty());
        service = new PagoService(pagos, pedidos, abonos, planes, negocios);
    }

    private void asignarId(Object entidad, Long id) throws Exception {
        Field field = entidad.getClass().getDeclaredField("id");
        field.setAccessible(true); field.set(entidad, id);
    }

    private PlanPago configurarPlan() throws Exception {
        PlanPago plan = PlanPago.crearCuotas(pago, 2, LocalDate.of(2026, 11, 30));
        asignarId(plan.getPartes().get(0), 11L); asignarId(plan.getPartes().get(1), 12L);
        when(planes.findByPago_Id(7L)).thenReturn(Optional.of(plan));
        return plan;
    }

    @Test
    void registrarAbonoLibreConservandoElHistorial() {
        service.registrarAbono(1L, new BigDecimal("20"), MetodoPago.EFECTIVO, null,
                null, "ana@example.com");
        assertEquals(0, new BigDecimal("20").compareTo(pago.getTotalAbonado()));
        ArgumentCaptor<Abono> captor = ArgumentCaptor.forClass(Abono.class);
        verify(abonos).save(captor.capture());
        assertNull(captor.getValue().getPartePago());
    }

    @Test
    void actualizarLaCuotaElPagoYElHistorial() throws Exception {
        PlanPago plan = configurarPlan();
        service.registrarAbono(1L, new BigDecimal("20"), MetodoPago.TRANSFERENCIA,
                "ABC", 11L, "ana@example.com");
        assertEquals(0, new BigDecimal("20").compareTo(pago.getTotalAbonado()));
        assertEquals(0, new BigDecimal("20").compareTo(plan.getPartes().get(0).getTotalAbonado()));
        ArgumentCaptor<Abono> captor = ArgumentCaptor.forClass(Abono.class);
        verify(abonos).save(captor.capture());
        assertSame(plan.getPartes().get(0), captor.getValue().getPartePago());
    }

    @Test
    void rechazarSobrepagoDeCuotaAntesDeModificarLosMontos() throws Exception {
        PlanPago plan = configurarPlan();
        assertThrows(IllegalArgumentException.class, () -> service.registrarAbono(
                1L, new BigDecimal("50.01"), MetodoPago.EFECTIVO, null, 11L, "ana@example.com"));
        assertEquals(0, pago.getTotalAbonado().signum());
        assertEquals(0, plan.getPartes().get(0).getTotalAbonado().signum());
        verify(abonos, never()).save(any(Abono.class));
    }

    @Test
    void rechazarParteDeOtroPlanYOmitirSeleccionSiYaExistePlan() throws Exception {
        configurarPlan();
        for (Long parteId : new Long[] {999L, null}) {
            assertThrows(IllegalArgumentException.class, () -> service.registrarAbono(
                    1L, BigDecimal.ONE, MetodoPago.EFECTIVO, null, parteId, "ana@example.com"));
        }
        assertEquals(0, pago.getTotalAbonado().signum());
        verify(abonos, never()).save(any(Abono.class));
    }

    @Test
    void impedirAbonarAPedidosDeOtraCuenta() {
        when(negocios.consultarEmprendimiento(42L, "otra@example.com"))
                .thenThrow(new NoSuchElementException("No disponible"));
        assertThrows(AccessDeniedException.class, () -> service.registrarAbono(
                1L, BigDecimal.ONE, MetodoPago.EFECTIVO, null, null, "otra@example.com"));
        assertEquals(0, pago.getTotalAbonado().signum());
        verify(abonos, never()).save(any(Abono.class));
    }

    @Test
    void distinguirMontosAnterioresSinDesglose() {
        pago.registrarAbono(new BigDecimal("30"));
        when(abonos.sumarMontosPorPedido(1L)).thenReturn(new BigDecimal("20"));
        assertEquals(0, new BigDecimal("10").compareTo(service.consultarMontoSinDetalle(1L)));
    }

    @Test
    void convertirElHistorialADatosIndependientesDeLaSesion() throws Exception {
        PlanPago plan = configurarPlan();
        Abono abono = new Abono(pago, BigDecimal.ONE, MetodoPago.EFECTIVO,
                null, plan.getPartes().get(0));
        when(abonos.findByPago_Pedido_IdOrderByFechaDescIdDesc(1L)).thenReturn(List.of(abono));
        assertEquals("Cuota 1", service.consultarHistorial(1L).get(0).getDestino());
    }
}
