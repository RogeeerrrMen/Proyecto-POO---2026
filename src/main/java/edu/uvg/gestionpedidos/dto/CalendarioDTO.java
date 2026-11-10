package edu.uvg.gestionpedidos.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CalendarioDTO {
    private final YearMonth mes;
    private final LocalDate hoy;
    private final String emprendimiento;
    private final List<List<DiaCalendarioDTO>> semanas;
    private final List<EventoCalendarioDTO> recordatorios;
    private final List<PedidoSinFechaDTO> sinFecha;
    private final int cantidadEventos;

    public CalendarioDTO(YearMonth mes, LocalDate hoy, String emprendimiento, List<List<DiaCalendarioDTO>> semanas, List<EventoCalendarioDTO> recordatorios, List<PedidoSinFechaDTO> sinFecha, int cantidadEventos) {
        this.mes = mes;
        this.hoy = hoy;
        this.emprendimiento = emprendimiento;
        this.semanas = semanas.stream().map(List::copyOf).toList();
        this.recordatorios = List.copyOf(recordatorios);
        this.sinFecha = List.copyOf(sinFecha);
        this.cantidadEventos = cantidadEventos;
    }

    public YearMonth getMes() { 
        return mes; 
    }

    public LocalDate getHoy() { 
        return hoy; 
    }

    public String getEmprendimiento() { 
        return emprendimiento; 
    }

    public List<List<DiaCalendarioDTO>> getSemanas() { 
        return semanas; 
    }

    public List<EventoCalendarioDTO> getRecordatorios() { 
        return recordatorios; 
    }

    public List<PedidoSinFechaDTO> getSinFecha() { 
        return sinFecha; 
    }

    public int getCantidadEventos() { 
        return cantidadEventos; 
    }

    public LocalDate getFinRecordatorios() { 
        return hoy.plusDays(6); 
    }


    public String getTitulo() {
        String titulo = mes.format(DateTimeFormatter.ofPattern(
                "MMMM 'de' uuuu", Locale.forLanguageTag("es-GT")));
        return Character.toUpperCase(titulo.charAt(0)) + titulo.substring(1);
    }

    public String getMesAnterior() {
        return mes.equals(YearMonth.of(1, 1)) ? null : mes.minusMonths(1).toString();
    }

    public String getMesSiguiente() {
        return mes.equals(YearMonth.of(9999, 12)) ? null : mes.plusMonths(1).toString();
    }
}
