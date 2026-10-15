package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente registrar(String nombre, String telefono) {
        validarDatos(nombre, telefono);

        Cliente cliente = new Cliente(nombre.trim(), telefono.trim()
        );

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
    }

    public List<Cliente> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listar();
        }

        String busqueda = texto.trim();

        return clienteRepository.findByNombreContainingIgnoreCaseOrTelefonoContaining(busqueda, busqueda);
    }

    public Cliente modificar(Long id, String nombre, String telefono) {
        validarDatos(nombre, telefono);

        Cliente cliente = obtenerPorId(id);
        cliente.actualizarDatos(nombre.trim(), telefono.trim());

        return clienteRepository.save(cliente);
    }

    private void validarDatos(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }

        if (!telefono.trim().matches("\\+?[0-9]+")) {
            throw new IllegalArgumentException("El teléfono debe contener solo números, con un + opcional al inicio.");
        }
    }
}