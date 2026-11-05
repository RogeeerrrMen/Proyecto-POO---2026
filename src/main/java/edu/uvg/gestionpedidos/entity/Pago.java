package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)

    private Pedido pedido;

    @Column(nullable = false, precision = 14, scale = 2)

    private BigDecimal totalAbonado = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)

    private EstadoPago estado = EstadoPago.PENDIENTE;

    protected Pago() {

        
    }

    public Pago(Pedido pedido) {
        if (pedido == null) {

            throw new IllegalArgumentException("El pedido es obligatorio.");
        }

        this.pedido = pedido;
        this.totalAbonado = BigDecimal.ZERO.setScale(2);
        this.estado = EstadoPago.PENDIENTE;
    }

    public BigDecimal getTotalPedido() {

        return pedido.calcularTotal().setScale(2, RoundingMode.UNNECESSARY);
    }

    public BigDecimal getSaldoPendiente() {

        return getTotalPedido().subtract(totalAbonado);
    }

    public void registrarAbono(BigDecimal monto) {

        if (monto == null || monto.signum() <= 0) {

            throw new IllegalArgumentException("El abono debe ser mayor que cero.");
        }

        BigDecimal abono;

        try {

            abono = monto.setScale(2, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException ex) {

            throw new IllegalArgumentException("El abono no puede tener más de dos decimales.");

        }

        if (abono.compareTo(getSaldoPendiente()) > 0) {

            throw new IllegalArgumentException("El abono supera el saldo pendiente.");

        }

        this.totalAbonado = totalAbonado.add(abono);
        actualizarEstado();
    }

    private void actualizarEstado() {

        if (totalAbonado.signum() == 0) {

            estado = EstadoPago.PENDIENTE;

        } else if (getSaldoPendiente().signum() == 0) {

            estado = EstadoPago.PAGADO;

        } else {

            estado = EstadoPago.ABONADO;
        }
    }

    public Long getId() {

        return id;
    }

    public Pedido getPedido() {

        return pedido;
    }

    public BigDecimal getTotalAbonado() {

        return totalAbonado;
    }

    public EstadoPago getEstado() {
        
        return estado;
    }
}
