package modelo;

import java.util.Objects;

/**
 * Identifica cada negocio y separa su información dentro del SaaS.
 */
public class Emprendimiento {

    private Long id;
    private String nombre;
    private Usuario propietario;

    public Emprendimiento() {
    }

    public Emprendimiento(String nombre, Usuario propietario) {
        this.nombre = nombre;
        this.propietario = propietario;
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

    public Usuario getPropietario() {
        return propietario;
    }

    /**
     * Cambia el nombre del negocio.
     */
    public void actualizarNombre(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del emprendimiento no puede estar vacío.");
        }
        this.nombre = nuevoNombre;
    }

    /**
     * Verifica si un usuario es el propietario de este emprendimiento.
     */
    public boolean perteneceA(Usuario usuario) {
        return this.propietario != null && this.propietario.equals(usuario);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Emprendimiento)) return false;
        Emprendimiento that = (Emprendimiento) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
