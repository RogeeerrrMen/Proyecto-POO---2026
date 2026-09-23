package modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Santiago Aguilón", "santiago@correo.com", "clave123");
    }

    @Test
    void seCreaActivoPorDefecto() {
        assertTrue(usuario.isActivo());
    }

    @Test
    void laPasswordSeGuardaComoHashYNoEnTextoPlano() {
        assertNotNull(usuario.getPasswordHash());
        assertNotEquals("clave123", usuario.getPasswordHash());
    }

    @Test
    void cambiarPasswordActualizaElHash() {
        String hashAnterior = usuario.getPasswordHash();
        usuario.cambiarPassword("nuevaClave456");
        assertNotEquals(hashAnterior, usuario.getPasswordHash());
    }

    @Test
    void cambiarPasswordRechazaValoresVacios() {
        assertThrows(IllegalArgumentException.class, () -> usuario.cambiarPassword(""));
    }

    @Test
    void activarYDesactivarCambianElEstado() {
        usuario.desactivar();
        assertFalse(usuario.isActivo());

        usuario.activar();
        assertTrue(usuario.isActivo());
    }
}
