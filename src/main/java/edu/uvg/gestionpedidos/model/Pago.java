package edu.uvg.gestionpedidos.model;

/**
 * Controla los abonos y el estado del pago de un pedido.
 */
public class Pago {

    private Long id;
    private EstadoPago estado;
    private double totalPedido;
    private double totalAbonado;
    private double saldoPendiente;

    public Pago() {
    }

    public Pago(double totalPedido) {
        this.totalPedido = totalPedido;
        this.totalAbonado = 0;
        this.saldoPendiente = totalPedido;
        this.estado = EstadoPago.PENDIENTE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public double getTotalPedido() {
        return totalPedido;
    }

    public double getTotalAbonado() {
        return totalAbonado;
    }

    public double getSaldoPendiente() {
        return saldoPendiente;
    }

    /**
     * Registra un abono y actualiza el saldo y el estado del pago.
     */
    public void registrarAbono(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El abono debe ser mayor a cero.");
        }
        if (monto > saldoPendiente) {
            throw new IllegalArgumentException("El abono no puede ser mayor al saldo pendiente.");
        }
        this.totalAbonado += monto;
        calcularSaldo();
        actualizarEstado();
    }

    /**
     * Calcula el saldo pendiente a partir del total y lo abonado.
     */
    public double calcularSaldo() {
        this.saldoPendiente = totalPedido - totalAbonado;
        return saldoPendiente;
    }

    /**
     * Actualiza el estado del pago según el monto abonado.
     */
    public void actualizarEstado() {
        if (totalAbonado <= 0) {
            this.estado = EstadoPago.PENDIENTE;
        } else if (saldoPendiente > 0) {
            this.estado = EstadoPago.ABONADO;
        } else {
            this.estado = EstadoPago.PAGADO;
        }
    }
}
