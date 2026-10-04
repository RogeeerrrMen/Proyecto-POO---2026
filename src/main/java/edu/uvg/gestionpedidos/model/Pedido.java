package edu.uvg.gestionpedidos.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Clase que representa un pedido realizado por un cliente a un emprendimiento.
public class Pedido {

    private Long id;
    private String codigo;
    private Cliente cliente;
    private Emprendimiento emprendimiento;
    private List<DetallePedido> detalles;
    private Pago pago;
    private DireccionEntrega direccionEntrega;
    private LocalDate fechaPreparacion;
    private LocalDate fechaEnvio;
    private LocalDate fechaEntrega;
    private EstadoPedido estado;
    private String observaciones;

    public Pedido() {
        this.detalles = new ArrayList<>();
        this.estado = EstadoPedido.REGISTRADO;
    }

    public Pedido(String codigo, Cliente cliente, Emprendimiento emprendimiento) {
        this();
        this.codigo = codigo;
        this.cliente = cliente;
        this.emprendimiento = emprendimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Emprendimiento getEmprendimiento() {
        return emprendimiento;
    }

    /** Devuelve una vista de solo lectura; usa agregarProducto()/eliminarProducto() para modificarla. */
    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public DireccionEntrega getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(DireccionEntrega direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public LocalDate getFechaPreparacion() {
        return fechaPreparacion;
    }

    public void setFechaPreparacion(LocalDate fechaPreparacion) {
        this.fechaPreparacion = fechaPreparacion;
    }

    public LocalDate getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDate fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // Gestión de productos del pedido 

    //Agrega un producto y su cantidad como un nuevo detalle del pedido
    public void agregarProducto(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        detalles.add(new DetallePedido(producto, cantidad));
    }

    //Elimina el detalle asociado a un producto del pedido
    public void eliminarProducto(Producto producto) {
        detalles.removeIf(detalle -> detalle.getProducto().equals(producto));
    }

    //Suma los subtotales de todos los detalles del pedido
    public double calcularTotal() {
        double total = 0;
        for (DetallePedido detalle : detalles) {
            total += detalle.calcularSubtotal();
        }
        return total;
    }

    // --- Máquina de estados ---
    // Transiciones válidas:
    // REGISTRADO -> CONFIRMADO -> EN_PREPARACION -> LISTO -> ENVIADO -> ENTREGADO
    //                                                        ENVIADO -> FALLIDO
    // Cancelar es válido en cualquier estado anterior a ENVIADO.

    //Confirma el pedido. Solo válido si está registrado.
    public void confirmar() {
        validarTransicion(EstadoPedido.REGISTRADO);
        this.estado = EstadoPedido.CONFIRMADO;
    }

    //Inicia la preparación. Solo válido si está confirmado.
    public void iniciarPreparacion() {
        validarTransicion(EstadoPedido.CONFIRMADO);
        this.estado = EstadoPedido.EN_PREPARACION;
    }

    // Marca el pedido como listo. Solo válido si está en preparación.
    public void marcarListo() {
        validarTransicion(EstadoPedido.EN_PREPARACION);
        this.estado = EstadoPedido.LISTO;
    }

    // Registra el envío. Solo válido si está listo. 
    public void marcarEnviado() {
        validarTransicion(EstadoPedido.LISTO);
        this.estado = EstadoPedido.ENVIADO;
    }

    // Registra la entrega. Solo válido si está enviado.
    public void marcarEntregado() {
        validarTransicion(EstadoPedido.ENVIADO);
        this.estado = EstadoPedido.ENTREGADO;
    }

    // Registra una entrega fallida. Solo válido si está enviado.
    public void marcarFallido() {
        validarTransicion(EstadoPedido.ENVIADO);
        this.estado = EstadoPedido.FALLIDO;
    }

    // Cancela el pedido, siempre que no esté ya entregado, fallido o cancelado.
    public void cancelar() {
        if (estado == EstadoPedido.ENTREGADO || estado == EstadoPedido.FALLIDO || estado == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se puede cancelar un pedido en estado " + estado + ".");
        }
        this.estado = EstadoPedido.CANCELADO;
    }

    private void validarTransicion(EstadoPedido estadoRequerido) {
        if (this.estado != estadoRequerido) {
            throw new IllegalStateException(
                    "No se puede hacer esta transición desde " + this.estado
                            + "; se requiere estar en " + estadoRequerido + ".");
        }
    }
}
