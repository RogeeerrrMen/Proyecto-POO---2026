package edu.uvg.gestionpedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "emprendimientos")
public class Emprendimiento {

    public static final int NOMBRE_MAX = 120;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NOMBRE_MAX)
    private String nombre;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "propietario_id",
            nullable = false,
            unique = true
    )
    private Usuario propietario;

    //Constructor necesario para JPA.
    protected Emprendimiento() {
    }

    public Emprendimiento(String nombre, Usuario propietario) {
        if (propietario == null) {
            throw new IllegalArgumentException(
                    "El propietario del emprendimiento es obligatorio.");
        }

        actualizarNombre(nombre);
        this.propietario = propietario;
    }

    public void actualizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del emprendimiento es obligatorio.");
        }

        String nombreLimpio = nombre.trim();

        if (nombreLimpio.length() > NOMBRE_MAX) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los "
                            + NOMBRE_MAX + " caracteres.");
        }

        this.nombre = nombreLimpio;
    }

    public boolean perteneceA(Usuario usuario) {
        if (usuario == null || usuario.getId() == null
                || propietario == null) {
            return false;
        }

        return usuario.getId().equals(propietario.getId());
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Usuario getPropietario() {
        return propietario;
    }
}