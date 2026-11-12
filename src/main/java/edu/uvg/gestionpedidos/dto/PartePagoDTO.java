package edu.uvg.gestionpedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import edu.uvg.gestionpedidos.entity.PartePago;

public class PartePagoDTO {
    private final Long id;
    private final String etiqueta;
    private final BigDecimal monto;
    private final BigDecimal totalAbonado;
    private final BigDecimal saldoPendiente;
    private final LocalDate fechaVencimiento;
    private final String estado;

    public PartePagoDTO(PartePago parte, LocalDate hoy) {
        this.id = parte.getId(); this.etiqueta = parte.getEtiqueta();
        this.monto = parte.getMonto(); this.totalAbonado = parte.getTotalAbonado();
        this.saldoPendiente = parte.getSaldoPendiente();
        this.fechaVencimiento = parte.getFechaVencimiento(); this.estado = parte.calcularEstado(hoy);
    }
    public Long getId() { 
        return id; 
    }
    
    public String getEtiqueta() { 
        return etiqueta; 
    }

    public BigDecimal getMonto() { 
        return monto; 
    }

    public BigDecimal getTotalAbonado() { 
        return totalAbonado; 
    }

    public BigDecimal getSaldoPendiente() { 
        return saldoPendiente; 
    }

    public LocalDate getFechaVencimiento() { 
        return fechaVencimiento; 
    }

    public String getEstado() { 
        return estado; 
    }
}
