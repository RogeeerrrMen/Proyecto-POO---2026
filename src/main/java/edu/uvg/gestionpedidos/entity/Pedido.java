package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private LocalDateTime fechaPreparacion;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaEntrega;

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

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public LocalDateTime getFechaPreparacion() {
        return fechaPreparacion;
    }

    public void setFechaPreparacion(LocalDateTime fechaPreparacion) {
        this.fechaPreparacion = fechaPreparacion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
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

    public BigDecimal getTotal() {
        return calcularTotal();
    }
}