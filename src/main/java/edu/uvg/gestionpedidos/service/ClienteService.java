package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.entity.Cliente;
import edu.uvg.gestionpedidos.repository.ClienteRepository;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmprendimientoService emprendimientoService;

    public ClienteService(ClienteRepository clienteRepository, EmprendimientoService emprendimientoService) {

        this.clienteRepository = clienteRepository;
        this.emprendimientoService = emprendimientoService;
    }

    public Cliente registrar(String nombre, String telefono) {
        return registrar(nombre, telefono, null, null);
    }

    
    public Cliente registrar(String nombre,String telefono,String nit,String razonSocial) {

        validarDatos(nombre, telefono);
        validarDatosFacturacion(nit, razonSocial);

        Emprendimiento emprendimiento = obtenerEmprendimientoActual();

        Cliente cliente = new Cliente(nombre.trim(),telefono.trim(),emprendimiento);

        cliente.setNit(nit);
        cliente.setRazonSocial(razonSocial);

        return clienteRepository.save(cliente);
    }


    public List<Cliente> listar() {

        Emprendimiento emprendimiento = obtenerEmprendimientoActual();

        return clienteRepository.findByEmprendimientoId(emprendimiento.getId());
    }

    
    public Cliente obtenerPorId(Long id) {

        if (id == null || id <= 0) {

            throw new IllegalArgumentException("El identificador del cliente no es válido.");
        }

        Emprendimiento emprendimiento = obtenerEmprendimientoActual();

        return clienteRepository.findByIdAndEmprendimientoId(

                    id,
                    emprendimiento.getId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                    "El cliente no existe o no está disponible."
                ));
    }


    
    public List<Cliente> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return listar();
        }

        Emprendimiento emprendimiento =
                obtenerEmprendimientoActual();

        String busqueda = texto.trim();
        Long emprendimientoId = emprendimiento.getId();

        return clienteRepository
            .findByEmprendimientoIdAndNombreContainingIgnoreCaseOrEmprendimientoIdAndTelefonoContaining(
                emprendimientoId,
                busqueda,
                emprendimientoId,
                busqueda
            );
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


    private String obtenerCorreoAutenticado() {

        var autenticacion = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (autenticacion == null|| !autenticacion.isAuthenticated()|| "anonymousUser".equals(autenticacion.getPrincipal())) {

            throw new AccessDeniedException(
                "Debes iniciar sesión."
            );
        }

        return autenticacion.getName();
    }

    private Emprendimiento obtenerEmprendimientoActual() {

        return emprendimientoService
                .buscarMiEmprendimiento(
                    obtenerCorreoAutenticado()
                )
                .orElseThrow(() -> new AccessDeniedException(
                    "Debes registrar tu emprendimiento."
                ));
    }

}