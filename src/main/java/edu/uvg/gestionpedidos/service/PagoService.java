package edu.uvg.gestionpedidos.service;

import java.math.BigDecimal;
import java.util.List;

import edu.uvg.gestionpedidos.dto.ResumenPagoDTO;
import edu.uvg.gestionpedidos.entity.Abono;
import edu.uvg.gestionpedidos.entity.EstadoPago;
import edu.uvg.gestionpedidos.entity.MetodoPago;
import edu.uvg.gestionpedidos.entity.Pago;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.repository.AbonoRepository;
import edu.uvg.gestionpedidos.repository.PagoRepository;
import edu.uvg.gestionpedidos.repository.PedidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final AbonoRepository abonoRepository;

    public PagoService(PagoRepository pagoRepository, PedidoRepository pedidoRepository, AbonoRepository abonoRepository) {

        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.abonoRepository = abonoRepository;
    }

    @Transactional
    public Pago obtenerOCrearPago(Long pedidoId) {
        Pedido pedido = bloquearPedido(pedidoId);

        return obtenerOCrearPagoBloqueado(pedido);
    }

    public Pago registrarAbono(Long pedidoId, BigDecimal monto) {
        throw new IllegalArgumentException("Debes escoger un método de pago.");
    }

    @Transactional
    public Pago registrarAbono(Long pedidoId, BigDecimal monto, MetodoPago metodoPago, String referencia) {

        Pedido pedido = bloquearPedido(pedidoId);

        Pago pago = obtenerOCrearPagoBloqueado(pedido);

        Abono abono = new Abono(pago, monto, metodoPago, referencia);

        // Comprueba el saldo y actualiza el total abonado.
        pago.registrarAbono(abono.getMonto());

        // El historial y el total se guardan en la misma transacción.
        abonoRepository.save(abono);

        return pagoRepository.save(pago);
    }

    @Transactional(readOnly = true)
    public List<Abono> consultarHistorial(Long pedidoId) {
        validarPedidoId(pedidoId);

        if (!pedidoRepository.existsById(pedidoId)) {
            throw new IllegalArgumentException("El pedido es inexistente.");
        }

        return abonoRepository
                .findByPago_Pedido_IdOrderByFechaDescIdDesc(pedidoId);
    }

    @Transactional(readOnly = true)
    public BigDecimal consultarMontoSinDetalle(Long pedidoId) {
        ResumenPagoDTO resumen = consultarResumen(pedidoId);

        BigDecimal totalConDetalle =
                abonoRepository.sumarMontosPorPedido(pedidoId);

        BigDecimal montoSinDetalle = resumen.getTotalAbonado()
                .subtract(totalConDetalle);

        if (montoSinDetalle.signum() < 0) {
            throw new IllegalStateException("El historial de abonos no coincide " + "con el total abonado del pedido.");
        }

        return montoSinDetalle;
    }

    @Transactional(readOnly = true)
    public ResumenPagoDTO consultarResumen(Long pedidoId) {
        validarPedidoId(pedidoId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("El pedido es inexistente."));

        BigDecimal totalPedido = pedido.calcularTotal();

        Pago pago = pagoRepository.findByPedidoId(pedidoId)
                .orElse(null);

        BigDecimal totalAbonado = BigDecimal.ZERO;
        EstadoPago estado = EstadoPago.PENDIENTE;

        if (pago != null) {
            totalAbonado = pago.getTotalAbonado();
            estado = pago.getEstado();
        }

        BigDecimal saldoPendiente =
                totalPedido.subtract(totalAbonado);

        return new ResumenPagoDTO(pedidoId, totalPedido, totalAbonado, saldoPendiente, estado);
    }

    private Pedido bloquearPedido(Long pedidoId) {
        validarPedidoId(pedidoId);

        return pedidoRepository.buscarParaPago(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("El pedido es inexistente."));
    }

    private Pago obtenerOCrearPagoBloqueado(Pedido pedido) {
        return pagoRepository
                .findWithLockByPedidoId(pedido.getId())
                .orElseGet(() -> {
                    Pago pago = new Pago(pedido);

                    return pagoRepository.saveAndFlush(pago);
                });
    }

    private void validarPedidoId(Long pedidoId) {
        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException("El identificador del pedido no es válido.");
        }
    }
}