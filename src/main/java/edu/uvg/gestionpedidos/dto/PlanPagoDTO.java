package edu.uvg.gestionpedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import edu.uvg.gestionpedidos.entity.PlanPago;
import edu.uvg.gestionpedidos.entity.TipoPlanPago;

public class PlanPagoDTO {
    private final TipoPlanPago tipo;
    private final BigDecimal saldoInicial;
    private final List<PartePagoDTO> partes;

    public PlanPagoDTO(PlanPago plan, LocalDate hoy) {
        this.tipo = plan.getTipo(); this.saldoInicial = plan.getSaldoInicial();
        this.partes = plan.getPartes().stream().map(p -> new PartePagoDTO(p, hoy)).toList();
    }
    public TipoPlanPago getTipo() { 
        return tipo; 
    }
    
    public BigDecimal getSaldoInicial() { 
        return saldoInicial; 
    }

    public List<PartePagoDTO> getPartes() { 
        return partes; 
    }
}
