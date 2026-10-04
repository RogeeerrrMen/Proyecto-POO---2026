package edu.uvg.gestionpedidos.model;

/**
 * Almacena los datos de los clientes de un emprendimiento.
 * (OJO) Versión simple: todavía sin repositorio ni historial de pedidos. 
 * Requerirá cambios cuando se agregue la funcionalidad de historial de pedidos (Base de datos).
 */
public class Cliente {

    private Long id;
    private String nombre;
    private String telefono;
    private Emprendimiento emprendimiento;

    public Cliente() {
    }

    public Cliente(String nombre, String telefono, Emprendimiento emprendimiento) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.emprendimiento = emprendimiento;
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

    public String getTelefono() {
        return telefono;
    }

    public Emprendimiento getEmprendimiento() {
        return emprendimiento;
    }

    //Modifica el nombre o teléfono del cliente.
    public void actualizarDatos(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono del cliente no puede estar vacío.");
        }
        this.nombre = nombre;
        this.telefono = telefono;
    }

    //Compara el número telefónico recibido con el del cliente.
    public boolean coincideConTelefono(String telefono) {
        return this.telefono != null && this.telefono.equals(telefono);
    }
}
