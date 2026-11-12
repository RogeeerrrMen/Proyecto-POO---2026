package edu.uvg.gestionpedidos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String telefono;
    private String nit;
    private String razonSocial;

    public Cliente() {
    }

    public Cliente(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = limpiarTexto(nit);
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = limpiarTexto(razonSocial);
    }

    public void actualizarDatos(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public void actualizarDatos(String nombre, String telefono, String nit, String razonSocial) {

        actualizarDatos(nombre, telefono);
        setNit(nit);
        setRazonSocial(razonSocial);
    }

    public boolean coincideConTelefono(String telefono) {
        return this.telefono != null
                && this.telefono.equals(telefono);
    }

    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}