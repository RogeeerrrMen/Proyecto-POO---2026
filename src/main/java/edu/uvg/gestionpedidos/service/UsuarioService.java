package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.RegistroUsuarioDTO;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;

    }


    public void registrarUsuario(RegistroUsuarioDTO datos) {

        validarDatos(datos);

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellido(datos.getApellido().trim());
        usuario.setCorreo(datos.getCorreo().trim().toLowerCase(java.util.Locale.ROOT));

        usuario.setPasswordHash(passwordEncoder.encode(datos.getPassword()));

        usuarioRepository.save(usuario);

    }

    private void validarDatos(RegistroUsuarioDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException("Los datos del registro son obligatorios");
        }

        if (datos.getNombre() == null || datos.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (datos.getApellido() == null || datos.getApellido().trim().isEmpty()) {

            throw new IllegalArgumentException("El apellido es obligatorio");
        }

        if (datos.getNombre().trim().length() > 100) {

            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        }

        if (datos.getApellido().trim().length() > 100) {
            
            throw new IllegalArgumentException("El apellido no puede superar los 100 caracteres");

        }

        if (datos.getCorreo() == null || datos.getCorreo().trim().isEmpty()) {

            throw new IllegalArgumentException("El correo es obligatorio");
        }

        if (datos.getPassword() == null || datos.getPassword().isBlank()) {

            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        if (!datos.getPassword().equals(datos.getConfirmarPassword())) {

            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        String correo = datos.getCorreo().trim();

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {

            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo");
        }
    }

}