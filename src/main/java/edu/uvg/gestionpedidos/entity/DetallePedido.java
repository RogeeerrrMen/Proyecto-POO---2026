package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, columnDefinition = "numeric")
    private BigDecimal precioUnitario;

    protected DetallePedido() {
    // Constructor requerido por JPA.
    }

    public DetallePedido(Pedido pedido, Producto producto, int cantidad, BigDecimal precioUnitario) {

        if (pedido == null) {
            throw new IllegalArgumentException("El detalle debe pertenecer a un pedido.");
        }

        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }

        if (!producto.isActivo()) {
            throw new IllegalArgumentException("No se pueden agregar productos inactivos.");
        }

        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario debe ser válido y no negativo.");
        }

        this.pedido = pedido;
        this.producto = producto;
        this.precioUnitario = precioUnitario;

        cambiarCantidad(cantidad);
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public void cambiarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return calcularSubtotal();
    }
}