package edu.uvg.gestionpedidos.dto;

import java.time.LocalDate;
import java.util.List;

public class DiaCalendarioDTO {
    private final LocalDate fecha;
    private final boolean delMes;
    private final boolean hoy;
    private final List<EventoCalendarioDTO> eventos;

    public DiaCalendarioDTO(LocalDate fecha, boolean delMes, boolean hoy, List<EventoCalendarioDTO> eventos) {
        this.fecha = fecha;
        this.delMes = delMes;
        this.hoy = hoy;
        this.eventos = List.copyOf(eventos);
    }

    public LocalDate getFecha() { 
        return fecha; 
    }

    public boolean isDelMes() { 
        return delMes; 
    }

    public boolean isHoy() { 
        return hoy; 
    }

    public List<EventoCalendarioDTO> getEventos() { 
        return eventos; 
    }

}
