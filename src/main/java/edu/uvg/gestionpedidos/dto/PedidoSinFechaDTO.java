package edu.uvg.gestionpedidos.dto;

import edu.uvg.gestionpedidos.entity.EstadoPedido;

public class PedidoSinFechaDTO {
    private final Long id;
    private final String codigo;
    private final String cliente;
    private final EstadoPedido estado;

    public PedidoSinFechaDTO(Long id, String codigo, String cliente,
            EstadoPedido estado) {
        this.id = id;
        this.codigo = codigo;
        this.cliente = cliente;
        this.estado = estado;
    }

    public Long getId() { 
        return id; 
    }

    public String getCodigo() { 
        return codigo; 
    }

    public String getCliente() { 
        return cliente; 
    }

    public EstadoPedido getEstado() { 
        return estado; 
    }

}
