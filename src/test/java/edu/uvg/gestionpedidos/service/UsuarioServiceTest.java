
package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.RegistroUsuarioDTO;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioService usuarioService;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioService = new UsuarioService(usuarioRepository);
    }

    private RegistroUsuarioDTO datosValidos() {

        RegistroUsuarioDTO datos = new RegistroUsuarioDTO();

        datos.setNombre("Roger");
        datos.setApellido("Mendez");
        datos.setCorreo("prueba@example.com");
        datos.setPassword("PruebaSegura2026!");
        datos.setConfirmarPassword("PruebaSegura2026!");

        return datos;
    }

    @Test
    void registrarUsuarioValido() {

        RegistroUsuarioDTO datos = datosValidos();

        usuarioService.registrarUsuario(datos);

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario guardado = captor.getValue();

        assertEquals("Roger", guardado.getNombre());
        assertEquals("prueba@example.com", guardado.getCorreo());

        assertNotEquals(datos.getPassword(),guardado.getPasswordHash());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertTrue(encoder.matches(datos.getPassword(),guardado.getPasswordHash()));
    }

    @Test
    void rechazarNombreVacio() {

        RegistroUsuarioDTO datos = datosValidos();
        datos.setNombre("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarCorreoInvalido() {

        RegistroUsuarioDTO datos = datosValidos();
        datos.setCorreo("correo-invalido");

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarContrasenasDiferentes() {

        RegistroUsuarioDTO datos = datosValidos();
        datos.setConfirmarPassword("OtraContrasena2026!");

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarCorreoDuplicado() {

        RegistroUsuarioDTO datos = datosValidos();

        when(usuarioRepository.existsByCorreoIgnoreCase(
                "prueba@example.com"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rechazarContrasenaCorta() {

        RegistroUsuarioDTO datos = datosValidos();

        datos.setPassword("123");
        datos.setConfirmarPassword("123");

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
