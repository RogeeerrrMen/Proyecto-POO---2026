package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "direccion_entrega_id", nullable = false, unique = true)
    private DireccionEntrega direccionEntrega;

    @Column(nullable = false)
    private LocalDateTime fechaPedido;

    private LocalDate fechaPreparacion;
    private LocalTime horaPreparacion;

    private LocalDate fechaEnvio;
    private LocalTime horaEnvio;

    private LocalDate fechaEntrega;
    private LocalTime horaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(columnDefinition = "text")
    private String observaciones;

    protected Pedido() {
    // Constructor requerido por JPA.
    }

    public Pedido(String codigo, Cliente cliente, DireccionEntrega direccionEntrega) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del pedido es obligatorio.");
        }

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }

        if (direccionEntrega == null || !direccionEntrega.validarDatos()) {
            throw new IllegalArgumentException("Los datos de entrega están incompletos o son inválidos.");
        }

        this.codigo = codigo.trim();
        this.cliente = cliente;
        this.direccionEntrega = direccionEntrega;
        this.fechaPedido = LocalDateTime.now();
        this.estado = EstadoPedido.REGISTRADO;
    }

    public void agregarProducto(Producto producto, int cantidad, BigDecimal precioUnitario) {

        DetallePedido detalle = new DetallePedido(this, producto, cantidad, precioUnitario);

        detalles.add(detalle);
    }

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedido detalle : detalles) {
            total = total.add(detalle.calcularSubtotal());
        }

        return total;
    }

    public void validarParaRegistro() {
        if (cliente == null) {
            throw new IllegalArgumentException("El pedido debe tener un cliente.");
        }

        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto.");
        }

        if (direccionEntrega == null || !direccionEntrega.validarDatos()) {
            throw new IllegalArgumentException("Los datos de entrega son inválidos.");
        }
    }

    private void cambiarEstado(EstadoPedido nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Debes seleccionar un estado.");
        }

        if (!estado.puedeCambiarA(nuevoEstado)) {
            throw new IllegalArgumentException("No se puede cambiar el pedido de " + estado + " a " + nuevoEstado + ".");
        }

        this.estado = nuevoEstado;
    }

    public void confirmar() {
        cambiarEstado(EstadoPedido.CONFIRMADO);
    }

    public void iniciarPreparacion() {
        cambiarEstado(EstadoPedido.EN_PREPARACION);
    }

    public void marcarListo() {
        cambiarEstado(EstadoPedido.LISTO);
    }

    public void marcarEnviado() {
        cambiarEstado(EstadoPedido.ENVIADO);
    }

    public void marcarEntregado() {
        cambiarEstado(EstadoPedido.ENTREGADO);
    }

    public void marcarFallido() {
        cambiarEstado(EstadoPedido.FALLIDO);
    }

    public void cancelar() {
        cambiarEstado(EstadoPedido.CANCELADO);
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public DireccionEntrega getDireccionEntrega() {
        return direccionEntrega;
    }

    public LocalDate getFechaPreparacion() {
        return fechaPreparacion;
    }

    public void setFechaPreparacion(LocalDate fechaPreparacion) {
        this.fechaPreparacion = fechaPreparacion;
    }

    public LocalTime getHoraPreparacion() {
        return horaPreparacion;
    }

    public void setHoraPreparacion(LocalTime horaPreparacion) {
        this.horaPreparacion = horaPreparacion;
    }

    public LocalDate getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDate fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalTime getHoraEnvio() {
        return horaEnvio;
    }

    public void setHoraEnvio(LocalTime horaEnvio) {
        this.horaEnvio = horaEnvio;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public LocalTime getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(LocalTime horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
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

    public BigDecimal getTotal() {
        return calcularTotal();
    }
}