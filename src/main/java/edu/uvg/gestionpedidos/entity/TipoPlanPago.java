package edu.uvg.gestionpedidos.entity;

public enum TipoPlanPago {
    CUOTAS("Cuotas mensuales"),
    DIVIDIDO("Dividido entre personas");

    private final String descripcion;
    TipoPlanPago(String descripcion) { 
        this.descripcion = descripcion; 
    }

    public String getDescripcion() { 
        return descripcion; 
    }
}
