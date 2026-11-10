package edu.uvg.gestionpedidos.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
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
    @JoinColumn(name = "propietario_id", nullable = false, unique = true
    )
    private Usuario propietario;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "emprendimiento_estados", joinColumns = @JoinColumn(name = "emprendimiento_id"))
    @OrderColumn(name = "posicion")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private List<EstadoPedido> flujoEstados = new ArrayList<>();

    protected Emprendimiento() {
    }

    public Emprendimiento(String nombre, Usuario propietario) {
        if (propietario == null) {
            throw new IllegalArgumentException("El propietario del emprendimiento es obligatorio.");
        }

        actualizarNombre(nombre);
        this.propietario = propietario;
    }

    public void actualizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del emprendimiento es obligatorio.");
        }

        String nombreLimpio = nombre.trim();

        if (nombreLimpio.length() > NOMBRE_MAX) {
            throw new IllegalArgumentException("El nombre no puede superar los " + NOMBRE_MAX + " caracteres.");
        }

        this.nombre = nombreLimpio;
    }

    public void actualizarFlujoEstados(List<EstadoPedido> estados) {
        List<EstadoPedido> flujoValidado =
                MaquinaEstadosPedido.validarFlujo(estados);

        flujoEstados.clear();
        flujoEstados.addAll(flujoValidado);
    }

    public List<EstadoPedido> getFlujoEstados() {
        if (flujoEstados.isEmpty()) {
            return MaquinaEstadosPedido.flujoPredeterminado();
        }

        return List.copyOf(flujoEstados);
    }

    public boolean perteneceA(Usuario usuario) {
        if (usuario == null
                || usuario.getId() == null
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