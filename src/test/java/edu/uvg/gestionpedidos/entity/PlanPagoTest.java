package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanPagoTest {
    private Pago pago(String total) {
        Pedido pedido = mock(Pedido.class);
        when(pedido.calcularTotal()).thenReturn(new BigDecimal(total));
        return new Pago(pedido);
    }
    private void monto(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real));
    }

    @Test
    void repartirCentavosExactamenteSinRegistrarCobros() {
        Pago pago = pago("100.00");
        PlanPago plan = PlanPago.crearCuotas(pago, 3, LocalDate.of(2026, 1, 31));
        monto("33.33", plan.getPartes().get(0).getMonto());
        monto("33.33", plan.getPartes().get(1).getMonto());
        monto("33.34", plan.getPartes().get(2).getMonto());
        monto("100.00", plan.getPartes().stream().map(PartePago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        monto("0", pago.getTotalAbonado());
        monto("100", pago.getSaldoPendiente());
    }

    @Test
    void mantenerDiferenciaMaximaDeUnCentavoEntreMuchasCuotas() {
        PlanPago plan = PlanPago.crearCuotas(pago("100"), 60, LocalDate.of(2026, 11, 30));
        BigDecimal minimo = plan.getPartes().stream().map(PartePago::getMonto)
                .min(BigDecimal::compareTo).orElseThrow();
        BigDecimal maximo = plan.getPartes().stream().map(PartePago::getMonto)
                .max(BigDecimal::compareTo).orElseThrow();
        monto("0.01", maximo.subtract(minimo));
        monto("100", plan.getPartes().stream().map(PartePago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    void calcularVencimientosDesdeLaPrimeraFechaSinAcumularDesplazamientos() {
        PlanPago plan = PlanPago.crearCuotas(pago("100"), 3, LocalDate.of(2026, 1, 31));
        assertEquals(LocalDate.of(2026, 2, 28), plan.getPartes().get(1).getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 3, 31), plan.getPartes().get(2).getFechaVencimiento());
    }

    @Test
    void respetarFebreroBisiestoYCambioDeAnio() {
        PlanPago plan = PlanPago.crearCuotas(pago("100"), 3, LocalDate.of(2023, 12, 31));
        assertEquals(LocalDate.of(2024, 1, 31), plan.getPartes().get(1).getFechaVencimiento());
        assertEquals(LocalDate.of(2024, 2, 29), plan.getPartes().get(2).getFechaVencimiento());
    }

    @Test
    void distribuirSoloElSaldoDespuesDeAbonosAnteriores() {
        Pago pago = pago("100"); pago.registrarAbono(new BigDecimal("25"));
        PlanPago plan = PlanPago.crearCuotas(pago, 3, LocalDate.of(2026, 11, 1));
        monto("75", plan.getSaldoInicial());
        monto("25", plan.getPartes().get(0).getMonto());
        monto("25", pago.getTotalAbonado());
    }

    @Test
    void rechazarCantidadInvalidaYFechaVacia() {
        for (Integer cantidad : Arrays.asList(null, 1, 61)) {
            assertThrows(IllegalArgumentException.class, () -> PlanPago.crearCuotas(
                    pago("100"), cantidad, LocalDate.of(2026, 11, 1)));
        }
        assertThrows(IllegalArgumentException.class, () -> PlanPago.crearCuotas(pago("100"), 2, null));
    }

    @Test
    void rechazarSaldoInsuficienteParaCuotasPositivas() {
        assertThrows(IllegalArgumentException.class, () -> PlanPago.crearCuotas(
                pago("0.01"), 2, LocalDate.of(2026, 11, 1)));
        Pago pagado = pago("10"); pagado.registrarAbono(new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class, () -> PlanPago.crearCuotas(
                pagado, 2, LocalDate.of(2026, 11, 1)));
    }

    @Test
    void dividirEntrePersonasConImportesDistintos() {
        Pago pago = pago("100");
        PlanPago plan = PlanPago.crearDividido(pago, List.of(" Ana ", "Luis"),
                List.of(new BigDecimal("40"), new BigDecimal("60")));
        assertEquals(TipoPlanPago.DIVIDIDO, plan.getTipo());
        assertEquals("Ana", plan.getPartes().get(0).getEtiqueta());
        assertNull(plan.getPartes().get(0).getFechaVencimiento());
        monto("0", pago.getTotalAbonado());
    }

    @Test
    void exigirQueLaSumaCoincidaConElSaldo() {
        for (String importe : List.of("59.99", "60.01")) {
            assertThrows(IllegalArgumentException.class, () -> PlanPago.crearDividido(
                    pago("100"), List.of("Ana", "Luis"),
                    List.of(new BigDecimal("40"), new BigDecimal(importe))));
        }
    }

    @Test
    void rechazarNombresVaciosDuplicadosYListasIncompletas() {
        for (List<String> nombres : List.of(List.of("Ana", " ana "), List.of("", "Luis"))) {
            assertThrows(IllegalArgumentException.class, () -> PlanPago.crearDividido(
                    pago("100"), nombres, List.of(new BigDecimal("40"), new BigDecimal("60"))));
        }
        assertThrows(IllegalArgumentException.class, () -> PlanPago.crearDividido(
                pago("100"), List.of("Ana"), List.of(new BigDecimal("100"))));
        assertThrows(IllegalArgumentException.class, () -> PlanPago.crearDividido(
                pago("100"), List.of("Ana", "Luis"), List.of(new BigDecimal("100"))));
    }

    @Test
    void rechazarImportesInvalidosSinRedondearlos() {
        for (BigDecimal importe : Arrays.asList(null, BigDecimal.ZERO,
                new BigDecimal("-1"), new BigDecimal("1.001"), new BigDecimal("1000000000000"))) {
            assertThrows(IllegalArgumentException.class, () -> PartePago.validarImporte(importe));
        }
    }

    @Test
    void permitirAbonosParcialesHastaCompletarUnaParte() {
        PartePago parte = PlanPago.crearCuotas(pago("100"), 2,
                LocalDate.of(2026, 11, 11)).getPartes().get(0);
        parte.registrarAbono(new BigDecimal("20"));
        monto("30", parte.getSaldoPendiente());
        assertEquals("PARCIAL", parte.calcularEstado(LocalDate.of(2026, 11, 11)));
        parte.registrarAbono(new BigDecimal("30"));
        monto("0", parte.getSaldoPendiente());
        assertEquals("PAGADO", parte.calcularEstado(LocalDate.of(2026, 12, 1)));
    }

    @Test
    void rechazarSobrepagoSinModificarElAcumulado() {
        PartePago parte = PlanPago.crearCuotas(pago("100"), 2,
                LocalDate.of(2026, 11, 11)).getPartes().get(0);
        assertThrows(IllegalArgumentException.class, () -> parte.registrarAbono(new BigDecimal("50.01")));
        monto("0", parte.getTotalAbonado());
    }

    @Test
    void marcarVencidaSoloDespuesDelVencimiento() {
        PartePago parte = PlanPago.crearCuotas(pago("100"), 2,
                LocalDate.of(2026, 11, 11)).getPartes().get(0);
        assertEquals("PENDIENTE", parte.calcularEstado(LocalDate.of(2026, 11, 11)));
        assertEquals("VENCIDO", parte.calcularEstado(LocalDate.of(2026, 11, 12)));
    }

    @Test
    void noPermitirAbonosDeUnaParteDeOtroPago() {
        Pago uno = pago("100"), otro = pago("100");
        PartePago parte = PlanPago.crearCuotas(uno, 2, LocalDate.of(2026, 11, 11)).getPartes().get(0);
        assertThrows(IllegalArgumentException.class,
                () -> new Abono(otro, BigDecimal.ONE, MetodoPago.EFECTIVO, null, parte));
    }

    @Test
    void mantenerListaDePartesProtegida() {
        PlanPago plan = PlanPago.crearCuotas(pago("100"), 2, LocalDate.of(2026, 11, 11));
        assertThrows(UnsupportedOperationException.class, () -> plan.getPartes().clear());
        assertThrows(IllegalArgumentException.class, () -> plan.buscarParte(null));
        assertThrows(IllegalArgumentException.class, () -> plan.buscarParte(999L));
    }
}
