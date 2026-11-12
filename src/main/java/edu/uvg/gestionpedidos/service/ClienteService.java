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
        return registrar(nombre, telefono, null, null);
    }

    public Cliente registrar(String nombre, String telefono, String nit, String razonSocial) {

        validarDatos(nombre, telefono);
        validarDatosFacturacion(nit, razonSocial);

        Cliente cliente = new Cliente(nombre.trim(), telefono.trim());
        cliente.setNit(nit);
        cliente.setRazonSocial(razonSocial);

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Cliente obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El identificador del cliente no es valido.");
        }

        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no fue encontrado."));
    }

    public List<Cliente> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listar();
        }

        String busqueda = texto.trim();

        return clienteRepository
                .findByNombreContainingIgnoreCaseOrTelefonoContaining(busqueda, busqueda);
    }

    public Cliente modificar(Long id, String nombre, String telefono) {
        validarDatos(nombre, telefono);

        Cliente cliente = obtenerPorId(id);
        cliente.actualizarDatos(nombre.trim(), telefono.trim());

        return clienteRepository.save(cliente);
    }

    public Cliente modificar(Long id, String nombre, String telefono, String nit, String razonSocial) {

        validarDatos(nombre, telefono);
        validarDatosFacturacion(nit, razonSocial);

        Cliente cliente = obtenerPorId(id);

        cliente.actualizarDatos(nombre.trim(), telefono.trim(), nit, razonSocial);

        return clienteRepository.save(cliente);
    }

    private void validarDatos(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (nombre.trim().length() > 255) {
            throw new IllegalArgumentException("El nombre no puede superar los 255 caracteres.");
        }

        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }

        if (telefono.trim().length() > 255) {
            throw new IllegalArgumentException("El teléfono no puede superar los 255 caracteres.");
        }

        if (!telefono.trim().matches("\\+?[0-9]+")) {
            throw new IllegalArgumentException("El teléfono debe contener solo números, " + "con un + opcional al inicio.");
        }
    }

    private void validarDatosFacturacion(String nit, String razonSocial) {
        if (nit != null && nit.trim().length() > 255) {
            throw new IllegalArgumentException("El NIT no puede superar los 255 caracteres.");
        }

        if (razonSocial != null && razonSocial.trim().length() > 255) {
            throw new IllegalArgumentException("La razón social no puede superar los 255 caracteres.");
        }
    }
}