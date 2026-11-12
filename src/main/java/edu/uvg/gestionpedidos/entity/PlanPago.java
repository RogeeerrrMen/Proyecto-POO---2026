package edu.uvg.gestionpedidos.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "planes_pago")
public class PlanPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_id", nullable = false, unique = true)
    private Pago pago;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPlanPago tipo;
    @Column(name = "saldo_inicial", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoInicial;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    @OrderBy("numero ASC")
    private List<PartePago> partes = new ArrayList<>();

    protected PlanPago() { }

    private PlanPago(Pago pago, TipoPlanPago tipo) {
        if (pago == null) { throw new IllegalArgumentException("El pago es obligatorio."); }
        this.pago = pago;
        this.tipo = tipo;
        this.saldoInicial = PartePago.validarImporte(pago.getSaldoPendiente());
        this.fechaCreacion = LocalDateTime.now();
    }

    public static PlanPago crearCuotas(Pago pago, Integer cantidad, LocalDate primerVencimiento) {
        if (cantidad == null || cantidad < 2 || cantidad > 60) {
            throw new IllegalArgumentException("Selecciona entre 2 y 60 cuotas.");
        }
        if (primerVencimiento == null) {
            throw new IllegalArgumentException("Indica la fecha de la primera cuota.");
        }
        PlanPago plan = new PlanPago(pago, TipoPlanPago.CUOTAS);
        BigDecimal cuota = plan.saldoInicial.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.DOWN);
        if (cuota.signum() == 0) {
            throw new IllegalArgumentException("El saldo es insuficiente para esa cantidad de cuotas.");
        }
        int centavosRestantes = plan.saldoInicial
                .subtract(cuota.multiply(BigDecimal.valueOf(cantidad)))
                .movePointRight(2).intValueExact();
        for (int i = 0; i < cantidad; i++) {
            BigDecimal monto = i >= cantidad - centavosRestantes
                    ? cuota.add(new BigDecimal("0.01")) : cuota;
            plan.partes.add(new PartePago(plan, i + 1, null,
                    primerVencimiento.plusMonths(i), monto));
        }
        return plan;
    }

    public static PlanPago crearDividido(Pago pago, List<String> personas, List<BigDecimal> montos) {
        if (personas == null || montos == null || personas.size() != montos.size()
                || personas.size() < 2 || personas.size() > 60) {
            throw new IllegalArgumentException("Agrega entre 2 y 60 personas con su importe.");
        }
        PlanPago plan = new PlanPago(pago, TipoPlanPago.DIVIDIDO);
        BigDecimal suma = BigDecimal.ZERO;
        Set<String> nombres = new HashSet<>();
        for (int i = 0; i < personas.size(); i++) {
            String nombre = personas.get(i) == null ? "" : personas.get(i).trim();
            if (nombre.isEmpty() || nombre.length() > 120) {
                throw new IllegalArgumentException("Cada persona debe tener un nombre de hasta 120 caracteres.");
            }
            if (!nombres.add(nombre.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("No repitas nombres; agrega un apellido para distinguir personas.");
            }
            BigDecimal monto = PartePago.validarImporte(montos.get(i));
            suma = suma.add(monto);
            plan.partes.add(new PartePago(plan, i + 1, nombre, null, monto));
        }
        if (suma.compareTo(plan.saldoInicial) != 0) {
            throw new IllegalArgumentException("La suma asignada debe coincidir con el saldo pendiente: Q" + plan.saldoInicial.toPlainString() + ".");
        }
        return plan;
    }

    public PartePago buscarParte(Long parteId) {
        if (parteId == null) {
            throw new IllegalArgumentException("Selecciona la cuota o persona a la que corresponde el abono.");
        }
        return partes.stream().filter(parte -> parteId.equals(parte.getId())).findFirst().orElseThrow(() -> new IllegalArgumentException("La cuota o persona no pertenece a este pedido."));
    }

    public Long getId() { 
        return id; 
    }

    public Pago getPago() { 
        return pago; 
    }

    public TipoPlanPago getTipo() { 
        return tipo; 
    }

    public BigDecimal getSaldoInicial() { 
        return saldoInicial; 
    }

    public LocalDateTime getFechaCreacion() { 
        return fechaCreacion; 
    }

    public List<PartePago> getPartes() { 
        return List.copyOf(partes); 
    }

}
