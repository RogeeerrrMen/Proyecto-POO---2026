package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.EmprendimientoDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.EmprendimientoRepository;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class EmprendimientoService {

    private final EmprendimientoRepository emprendimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public EmprendimientoService(
            EmprendimientoRepository emprendimientoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.emprendimientoRepository = emprendimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Consulta el emprendimiento del usuario autenticado
    @Transactional(readOnly = true)
    public Optional<Emprendimiento> buscarMiEmprendimiento(
            String correoAutenticado
    ) {
        Usuario propietario = obtenerUsuario(correoAutenticado);

        return emprendimientoRepository.findByPropietario(propietario);
    }

    //Consulta un identificador solamente si pertenece al usuario
    @Transactional(readOnly = true)
    public Emprendimiento consultarEmprendimiento(
            Long id,
            String correoAutenticado
    ) {
        Usuario propietario = obtenerUsuario(correoAutenticado);

        return obtenerEmprendimientoPropio(id, propietario);
    }

    //Permite registrar un negocio a usuarios que todavía no tienen uno
    @Transactional
    public Emprendimiento registrarEmprendimiento(
            EmprendimientoDTO datos,
            String correoAutenticado
    ) {
        Usuario propietario = obtenerUsuario(correoAutenticado);
        validarDatos(datos);

        if (emprendimientoRepository.existsByPropietario(propietario)) {
            throw new IllegalArgumentException(
                    "Ya tienes un emprendimiento registrado.");
        }

        //El constructor valida el nombre y exige un propietario
        Emprendimiento emprendimiento = new Emprendimiento(
                datos.getNombre(),
                propietario
        );

        return emprendimientoRepository.save(emprendimiento);
    }

    //Modifica el nombre conservando el propietario original
    @Transactional
    public Emprendimiento modificarEmprendimiento(
            Long id,
            EmprendimientoDTO datos,
            String correoAutenticado
    ) {
        Usuario propietario = obtenerUsuario(correoAutenticado);

        Emprendimiento emprendimiento =
                obtenerEmprendimientoPropio(id, propietario);

        validarDatos(datos);

        //La entidad valida el nombre antes de cambiarlo
        emprendimiento.actualizarNombre(datos.getNombre());

        return emprendimientoRepository.save(emprendimiento);
    }

    private Usuario obtenerUsuario(String correoAutenticado) {
        if (correoAutenticado == null || correoAutenticado.isBlank()) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para administrar tu emprendimiento.");
        }

        return usuarioRepository
                .findByCorreoIgnoreCase(correoAutenticado.trim())
                .orElseThrow(() -> new AccessDeniedException(
                        "No se encontró la cuenta autenticada."));
    }

    private Emprendimiento obtenerEmprendimientoPropio(
            Long id,
            Usuario propietario
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del emprendimiento debe ser positivo.");
        }

        return emprendimientoRepository
                .findByIdAndPropietario(id, propietario)
                .orElseThrow(() -> new NoSuchElementException(
                        "El emprendimiento no existe o no está disponible "
                                + "para tu cuenta."));
    }

    private void validarDatos(EmprendimientoDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del emprendimiento son obligatorios.");
        }
    }
}