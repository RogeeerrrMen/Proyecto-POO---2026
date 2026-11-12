package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "partes_pago", uniqueConstraints =
        @UniqueConstraint(name = "uk_parte_plan_numero", columnNames = {"plan_id", "numero"}))
public class PartePago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanPago plan;

    @Column(nullable = false)
    private int numero;
    @Column(length = 120)
    private String persona;
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;
    @Column(name = "total_abonado", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAbonado = BigDecimal.ZERO.setScale(2);

    protected PartePago() { }

    PartePago(PlanPago plan, int numero, String persona, LocalDate fecha, BigDecimal monto) {
        if (plan == null || numero < 1) {
            throw new IllegalArgumentException("La parte del pago es inválida.");
        }
        this.plan = plan;
        this.numero = numero;
        this.persona = persona;
        this.fechaVencimiento = fecha;
        this.monto = validarImporte(monto);
    }

    public static BigDecimal validarImporte(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("Cada importe debe ser mayor que cero.");
        }
        try {
            BigDecimal normalizado = monto.setScale(2, RoundingMode.UNNECESSARY);
            if (normalizado.precision() > 14) {
                throw new IllegalArgumentException("El importe supera el máximo permitido.");
            }
            return normalizado;
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Los importes deben tener como máximo dos decimales.");
        }
    }

    public void validarAbono(BigDecimal importe) {
        BigDecimal validado = validarImporte(importe);
        if (validado.compareTo(getSaldoPendiente()) > 0) {
            throw new IllegalArgumentException("El abono supera el saldo pendiente de " + getEtiqueta() + ".");
        }
    }

    public void registrarAbono(BigDecimal importe) {
        validarAbono(importe);
        totalAbonado = totalAbonado.add(importe.setScale(2, RoundingMode.UNNECESSARY));
    }

    public String calcularEstado(LocalDate hoy) {
        if (getSaldoPendiente().signum() == 0) { return "PAGADO"; }
        if (fechaVencimiento != null && fechaVencimiento.isBefore(hoy)) { return "VENCIDO"; }
        return totalAbonado.signum() > 0 ? "PARCIAL" : "PENDIENTE";
    }

    public Long getId() { 
        return id; 
    }

    public PlanPago getPlan() { 
        return plan; 
    }

    public int getNumero() { 
        return numero; 
    }

    public String getPersona() { 
        return persona; 
    }

    public LocalDate getFechaVencimiento() { 
        return fechaVencimiento; 
    }

    public BigDecimal getMonto() { 
        return monto; 
    }

    public BigDecimal getTotalAbonado() { 
        return totalAbonado; 
    }

    public BigDecimal getSaldoPendiente() { 
        return monto.subtract(totalAbonado); 
    }

    public String getEtiqueta() { 
        return persona == null ? "Cuota " + numero : persona; 
    }
}
