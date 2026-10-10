package edu.uvg.gestionpedidos.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Datos que llegan del formulario de registro de pedido.
 * Solo transporta información; las reglas de negocio están en el dominio y en PedidoService.
 */
public class PedidoForm {

    private String nombreCliente;
    private String telefonoCliente;

    /** idProducto -> cantidad solicitada. */
    private Map<Long, Integer> cantidades = new LinkedHashMap<>();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPreparacion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEnvio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEntrega;

    private String observaciones;

    private String nombreReceptor;
    private String telefonoReceptor;
    private String direccion;
    private String referencias;
    private String enlaceMapa;

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public Map<Long, Integer> getCantidades() {
        return cantidades;
    }

    public void setCantidades(Map<Long, Integer> cantidades) {
        this.cantidades = cantidades;
    }

    public LocalDate getFechaPreparacion() {
        return fechaPreparacion;
    }

    public void setFechaPreparacion(LocalDate fechaPreparacion) {
        this.fechaPreparacion = fechaPreparacion;
    }

    public LocalDate getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDate fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public void setNombreReceptor(String nombreReceptor) {
        this.nombreReceptor = nombreReceptor;
    }

    public String getTelefonoReceptor() {
        return telefonoReceptor;
    }

    public void setTelefonoReceptor(String telefonoReceptor) {
        this.telefonoReceptor = telefonoReceptor;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getReferencias() {
        return referencias;
    }

    public void setReferencias(String referencias) {
        this.referencias = referencias;
    }

    public String getEnlaceMapa() {
        return enlaceMapa;
    }

    public void setEnlaceMapa(String enlaceMapa) {
        this.enlaceMapa = enlaceMapa;
    }
}
