package edu.uvg.gestionpedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private boolean activo;

    public Producto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isActivo() {
        return activo;
    }

    //Cambia el precio del producto. Debe ser un número válido, mayor o igual a cero.
    public void actualizarPrecio(double nuevoPrecio) {
        if (!Double.isFinite(nuevoPrecio) || nuevoPrecio < 0) {
            throw new IllegalArgumentException("El precio debe ser un número mayor o igual a cero.");
        }
        this.precio = nuevoPrecio;
    }

    //Habilita el producto.
    public void activar() {
        this.activo = true;
    }

    //Deshabilita el producto.
    public void desactivar() {
        this.activo = false;
    }
}
