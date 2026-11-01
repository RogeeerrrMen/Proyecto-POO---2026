package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.RegistroUsuarioDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.EmprendimientoRepository;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private EmprendimientoRepository emprendimientoRepository;
    private UsuarioService usuarioService;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);

        emprendimientoRepository =
                mock(EmprendimientoRepository.class);

        usuarioService = new UsuarioService(
                usuarioRepository,
                emprendimientoRepository
        );
    }

    private RegistroUsuarioDTO datosValidos() {
        RegistroUsuarioDTO datos = new RegistroUsuarioDTO();

        datos.setNombre("Roger");
        datos.setApellido("Mendez");
        datos.setCorreo("prueba@example.com");
        datos.setPassword("PruebaSegura2026!");
        datos.setConfirmarPassword("PruebaSegura2026!");
        datos.setEmprendimiento("  Dulces Roger  ");

        return datos;
    }

    //Comprueba que un registro inválido no intente guardar ningún objeto
    private void assertRegistroRechazado(RegistroUsuarioDTO datos) {
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos)
        );

        verify(usuarioRepository, never())
                .save(any(Usuario.class));

        verify(emprendimientoRepository, never())
                .save(any(Emprendimiento.class));
    }

    @Test
    void registrarUsuarioValido() {
        RegistroUsuarioDTO datos = datosValidos();

        usuarioService.registrarUsuario(datos);

        ArgumentCaptor<Usuario> usuarioCaptor =
                ArgumentCaptor.forClass(Usuario.class);

        ArgumentCaptor<Emprendimiento> emprendimientoCaptor =
                ArgumentCaptor.forClass(Emprendimiento.class);

        verify(usuarioRepository).save(usuarioCaptor.capture());

        verify(emprendimientoRepository)
                .save(emprendimientoCaptor.capture());

        Usuario guardado = usuarioCaptor.getValue();
        Emprendimiento negocio = emprendimientoCaptor.getValue();

        assertEquals("Roger", guardado.getNombre());
        assertEquals("Mendez", guardado.getApellido());
        assertEquals("prueba@example.com", guardado.getCorreo());

        assertNotEquals(
                datos.getPassword(),
                guardado.getPasswordHash()
        );

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        assertTrue(
                encoder.matches(
                        datos.getPassword(),
                        guardado.getPasswordHash()
                )
        );

        assertEquals("Dulces Roger", negocio.getNombre());

        //El negocio debe pertenecer al usuario que se acaba de registrar
        assertSame(guardado, negocio.getPropietario());
    }

    @Test
    void rechazarNombreVacio() {
        RegistroUsuarioDTO datos = datosValidos();
        datos.setNombre("   ");

        assertRegistroRechazado(datos);
    }

    @Test
    void rechazarCorreoInvalido() {
        RegistroUsuarioDTO datos = datosValidos();
        datos.setCorreo("correo-invalido");

        assertRegistroRechazado(datos);
    }

    @Test
    void rechazarContrasenasDiferentes() {
        RegistroUsuarioDTO datos = datosValidos();
        datos.setConfirmarPassword("OtraContrasena2026!");

        assertRegistroRechazado(datos);
    }

    @Test
    void rechazarCorreoDuplicado() {
        RegistroUsuarioDTO datos = datosValidos();

        when(usuarioRepository.existsByCorreoIgnoreCase(
                "prueba@example.com"
        )).thenReturn(true);

        assertRegistroRechazado(datos);
    }

    @Test
    void rechazarContrasenaCorta() {
        RegistroUsuarioDTO datos = datosValidos();

        datos.setPassword("123");
        datos.setConfirmarPassword("123");

        assertRegistroRechazado(datos);
    }

    @Test
    void rechazarEmprendimientoInvalidoSinGuardarLaCuenta() {
        String[] nombresInvalidos = {
                null, "", "   ", "a".repeat(121)
        };

        for (String nombre : nombresInvalidos) {
            RegistroUsuarioDTO datos = datosValidos();
            datos.setEmprendimiento(nombre);

            assertRegistroRechazado(datos);
        }
    }
}