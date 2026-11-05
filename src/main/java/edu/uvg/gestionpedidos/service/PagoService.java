package edu.uvg.gestionpedidos.service;
import edu.uvg.gestionpedidos.dto.ResumenPagoDTO;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uvg.gestionpedidos.entity.Pago;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.entity.EstadoPago;

import edu.uvg.gestionpedidos.repository.PagoRepository;
import edu.uvg.gestionpedidos.repository.PedidoRepository;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    public PagoService(PagoRepository pagoRepository,PedidoRepository pedidoRepository) {

        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pago obtenerOCrearPago(Long pedidoId) {

        validarPedidoId(pedidoId);

        return pagoRepository.findWithLockByPedidoId(pedidoId).orElseGet(() -> crearPago(pedidoId));
    }

    @Transactional
    public Pago registrarAbono(Long pedidoId, BigDecimal monto) {

        validarPedidoId(pedidoId);

        Pago pago = pagoRepository.findWithLockByPedidoId(pedidoId).orElseGet(() -> crearPago(pedidoId));

        pago.registrarAbono(monto);

        return pagoRepository.save(pago);
    }

    private Pago crearPago(Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new IllegalArgumentException(
                    "El pedido no existe."
                ));

        Pago pago = new Pago(pedido);

        return pagoRepository.saveAndFlush(pago);
    }

    private void validarPedidoId(Long pedidoId) {

        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException("El identificador del pedido no es válido.");
        }
    }

    
    @Transactional(readOnly = true)
    public ResumenPagoDTO consultarResumen(Long pedidoId) {

        validarPedidoId(pedidoId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "El pedido no existe."
                ));

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

        return new ResumenPagoDTO(
            pedidoId,
            totalPedido,
            totalAbonado,
            saldoPendiente,
            estado
        );
    }

}
