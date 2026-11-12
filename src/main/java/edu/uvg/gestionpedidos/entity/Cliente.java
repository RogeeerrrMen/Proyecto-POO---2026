package edu.uvg.gestionpedidos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emprendimiento_id")
    private Emprendimiento emprendimiento;

    public Cliente() {
    }

    public Cliente(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public Cliente(String nombre,String telefono,Emprendimiento emprendimiento) {

        if (emprendimiento == null) {
            throw new IllegalArgumentException(
                "El emprendimiento es obligatorio"
            );
        }

        this.nombre = nombre;
        this.telefono = telefono;
        this.emprendimiento = emprendimiento;
    }

    public Long getId() {
        return id;
    }

    public Emprendimiento getEmprendimiento() {
        
        return emprendimiento;
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