package edu.uvg.gestionpedidos.dto;

import edu.uvg.gestionpedidos.entity.Producto;


//Datos que viajan entre el formulario de productos y el Service.
//Se usa en lugar de la entidad para que el formulario nunca pueda alterar
//campos que no le corresponden (como el id o el estado activo).

public class ProductoDTO {

    private String nombre;
    private String descripcion;

    /** Wrapper (Double) para poder distinguir "no se escribió precio" de un precio en cero. */
    private Double precio;

    public ProductoDTO() {
    }

    /** Crea un DTO con los datos actuales de un producto, para prellenar el formulario de edición. */
    public static ProductoDTO desde(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        return dto;
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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }
}
