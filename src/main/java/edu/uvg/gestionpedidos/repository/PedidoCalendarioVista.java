package edu.uvg.gestionpedidos.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import edu.uvg.gestionpedidos.entity.EstadoPedido;
import edu.uvg.gestionpedidos.entity.ModalidadEntrega;

public interface PedidoCalendarioVista {
    Long getId();
    String getCodigo();
    String getNombreCliente();
    EstadoPedido getEstado();
    ModalidadEntrega getModalidadEntrega();
    LocalDate getFechaPreparacion();
    LocalTime getHoraPreparacion();
    LocalDate getFechaEnvio();
    LocalTime getHoraEnvio();
    LocalDate getFechaEntrega();
    LocalTime getHoraEntrega();
}
