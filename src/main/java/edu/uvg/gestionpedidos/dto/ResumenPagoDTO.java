package edu.uvg.gestionpedidos.dto;

import java.math.BigDecimal;

import edu.uvg.gestionpedidos.entity.EstadoPago;

public class ResumenPagoDTO {

    private final Long pedidoId;
    private final BigDecimal totalPedido;
    private final BigDecimal totalAbonado;
    private final BigDecimal saldoPendiente;
    private final EstadoPago estado;

    public ResumenPagoDTO(Long pedidoId, BigDecimal totalPedido, BigDecimal totalAbonado, BigDecimal saldoPendiente,
    EstadoPago estado) {

        this.pedidoId = pedidoId;
        this.totalPedido = totalPedido;
        this.totalAbonado = totalAbonado;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
    }

    public Long getPedidoId() {

        return pedidoId;
    }

    public BigDecimal getTotalPedido() {

        return totalPedido;
    }

    public BigDecimal getTotalAbonado() {

        return totalAbonado;
    }

    public BigDecimal getSaldoPendiente() {

        return saldoPendiente;
    }

    public EstadoPago getEstado() {
        
        return estado;
    }
}
