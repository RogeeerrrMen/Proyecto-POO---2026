package edu.uvg.gestionpedidos.model;

/**
 * Representa los productos ofrecidos por el emprendimiento.
 * Por ahora es un objeto en memoria: todavía no hay Repository ni base de datos.
 */
public class Producto {

    private Long id;
    private String nombre;
    private String descripcion;
    private double precio;
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

    /**
     * Cambia el precio del producto.
     */
    public void actualizarPrecio(double nuevoPrecio) {
        if (nuevoPrecio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = nuevoPrecio;
    }

    /** Habilita el producto. */
    public void activar() {
        this.activo = true;
    }

    /** Deshabilita el producto. */
    public void desactivar() {
        this.activo = false;
    }
}
