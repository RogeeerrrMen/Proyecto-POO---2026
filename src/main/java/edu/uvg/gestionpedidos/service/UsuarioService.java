package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.RegistroUsuarioDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.EmprendimientoRepository;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final UsuarioRepository usuarioRepository;
    private final EmprendimientoRepository emprendimientoRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmprendimientoRepository emprendimientoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.emprendimientoRepository = emprendimientoRepository;
    }

    // Guarda la cuenta y su emprendimiento en una misma transacción.
    @Transactional
    public void registrarUsuario(RegistroUsuarioDTO datos) {
        validarDatos(datos);

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellido(datos.getApellido().trim());
        usuario.setCorreo(
                datos.getCorreo().trim().toLowerCase(Locale.ROOT)
        );

        // El constructor valida el nombre antes de guardar la cuenta.
        Emprendimiento emprendimiento = new Emprendimiento(
                datos.getEmprendimiento(),
                usuario
        );

        usuario.setPasswordHash(
                passwordEncoder.encode(datos.getPassword())
        );

        // Primero se guarda el usuario para generar su identificador.
        usuarioRepository.save(usuario);

        // El emprendimiento conserva la referencia al mismo usuario.
        emprendimientoRepository.save(emprendimiento);
    }

    private void validarDatos(RegistroUsuarioDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del registro son obligatorios."
            );
        }

        if (datos.getNombre() == null
                || datos.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (datos.getApellido() == null
                || datos.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El apellido es obligatorio."
            );
        }

        if (datos.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres."
            );
        }

        if (datos.getApellido().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El apellido no puede superar los 100 caracteres."
            );
        }

        if (datos.getCorreo() == null
                || datos.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El correo es obligatorio."
            );
        }

        String correo = datos.getCorreo().trim();

        if (correo.length() > 255) {
            throw new IllegalArgumentException(
                    "El correo no puede superar los 255 caracteres."
            );
        }

        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            throw new IllegalArgumentException(
                    "El formato del correo no es válido."
            );
        }

        if (datos.getPassword() == null
                || datos.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        if (!datos.getPassword().equals(datos.getConfirmarPassword())) {
            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden."
            );
        }

        if (datos.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres."
            );
        }

        if (datos.getPassword()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "La contraseña no puede superar los 72 bytes UTF-8."
            );
        }

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta registrada con ese correo."
            );
        }
    }
}