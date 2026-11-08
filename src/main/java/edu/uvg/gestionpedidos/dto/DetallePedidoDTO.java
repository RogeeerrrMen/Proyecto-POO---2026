package edu.uvg.gestionpedidos.dto;

import java.math.BigDecimal;

public class DetallePedidoDTO {

    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;

    public DetallePedidoDTO() {
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
