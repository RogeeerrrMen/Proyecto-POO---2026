package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.RegistroUsuarioDTO;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

@Service
public class UsuarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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

        String correo = datos.getCorreo().trim();

        if (correo.length() > 255) {

            throw new IllegalArgumentException("El correo no puede superar los 255 caracteres");
        }

        if (!EMAIL_PATTERN.matcher(correo).matches()) {

            throw new IllegalArgumentException("El formato del correo no es válido");
        }

        if (datos.getPassword() == null || datos.getPassword().isBlank()) {

            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        if (!datos.getPassword().equals(datos.getConfirmarPassword())) {

            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        if (datos.getPassword().length() < 8) {

            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        if (datos.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException("La contraseña no puede superar los 72 bytes UTF-8");
        }


        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {

            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo");
        }

    }

}