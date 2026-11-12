package edu.uvg.gestionpedidos.dto;

import java.util.ArrayList;
import java.util.List;

import edu.uvg.gestionpedidos.entity.EstadoPedido;

public class ConfiguracionEstadosDTO {

    private List<EstadoPedido> estados = new ArrayList<>();

    public ConfiguracionEstadosDTO() {
    }

    public List<EstadoPedido> getEstados() {
        return estados;
    }

    public void setEstados(List<EstadoPedido> estados) {
        this.estados = estados == null
                ? new ArrayList<>()
                : new ArrayList<>(estados);
    }
}