package edu.uvg.gestionpedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import edu.uvg.gestionpedidos.entity.Abono;
import edu.uvg.gestionpedidos.entity.MetodoPago;

public class AbonoHistorialDTO {
    private final Long id;
    private final BigDecimal monto;
    private final MetodoPago metodoPago;
    private final LocalDateTime fecha;
    private final String referencia;
    private final String destino;

    public AbonoHistorialDTO(Abono abono) {
        this.id = abono.getId(); this.monto = abono.getMonto();
        this.metodoPago = abono.getMetodoPago(); this.fecha = abono.getFecha();
        this.referencia = abono.getReferencia();
        this.destino = abono.getPartePago() == null ? "Abono libre / anterior al plan"
                : abono.getPartePago().getEtiqueta();
    }
    public Long getId() { 
        return id; 
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

    public String getDestino() { 
        return destino; 
    }
}
