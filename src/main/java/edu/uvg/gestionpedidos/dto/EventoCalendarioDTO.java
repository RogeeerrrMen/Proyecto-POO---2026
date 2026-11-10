package edu.uvg.gestionpedidos.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import edu.uvg.gestionpedidos.entity.EstadoPedido;

public class EventoCalendarioDTO {
    private final Long pedidoId;
    private final String codigo;
    private final String cliente;
    private final EstadoPedido estado;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String tipo;
    private final String etiqueta;

    public EventoCalendarioDTO(Long pedidoId, String codigo, String cliente, EstadoPedido estado, LocalDate fecha, LocalTime hora, String tipo, String etiqueta) {
        this.pedidoId = pedidoId;
        this.codigo = codigo;
        this.cliente = cliente;
        this.estado = estado;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.etiqueta = etiqueta;
    }

    public Long getPedidoId() { 
        return pedidoId; 
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

    public LocalDate getFecha() { 
        return fecha; 
    }

    public LocalTime getHora() { 
        return hora; 
    }

    public String getTipo() { 
        return tipo; 
    }
    
    public String getEtiqueta() { 
        return etiqueta; 
    }
}
