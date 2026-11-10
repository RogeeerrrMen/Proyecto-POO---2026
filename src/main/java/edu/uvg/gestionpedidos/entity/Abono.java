package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "abonos")
public class Abono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 255)
    private String referencia;

    protected Abono() {
    }

    public Abono(Pago pago, BigDecimal monto, MetodoPago metodoPago, String referencia) {

        if (pago == null) {
            throw new IllegalArgumentException("El pago del pedido es obligatorio.");
        }

        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El abono debe ser mayor que cero.");
        }

        BigDecimal montoValidado;

        try {
            montoValidado = monto.setScale(2, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("El abono no puede tener más de dos decimales.");
        }

        if (montoValidado.precision() > 14) {
            throw new IllegalArgumentException("El monto del abono supera el máximo permitido.");
        }

        if (metodoPago == null) {
            throw new IllegalArgumentException("Debes seleccionar un método de pago.");
        }

        String referenciaLimpia = referencia == null
                || referencia.isBlank()
                ? null
                : referencia.trim();

        if (referenciaLimpia != null
                && referenciaLimpia.length() > 255) {
            throw new IllegalArgumentException("La referencia no puede superar los 255 caracteres.");
        }

        this.pago = pago;
        this.monto = montoValidado;
        this.metodoPago = metodoPago;
        this.referencia = referenciaLimpia;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Pago getPago() {
        return pago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferencia() {
        return referencia;
    }
}