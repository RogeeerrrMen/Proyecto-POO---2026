package modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmprendimientoTest {

    private Usuario propietario;
    private Usuario otroUsuario;
    private Emprendimiento emprendimiento;

    @BeforeEach
    void setUp() {
        propietario = new Usuario("Kellie López", "kellie@correo.com", "clave123");
        otroUsuario = new Usuario("Roger Méndez", "roger@correo.com", "clave456");
        emprendimiento = new Emprendimiento("Postres Kellie", propietario);
    }

    @Test
    void actualizarNombreCambiaElNombre() {
        emprendimiento.actualizarNombre("Postres y más");
        assertEquals("Postres y más", emprendimiento.getNombre());
    }

    @Test
    void actualizarNombreRechazaValoresVacios() {
        assertThrows(IllegalArgumentException.class, () -> emprendimiento.actualizarNombre(" "));
    }

    @Test
    void perteneceADetectaAlPropietarioCorrecto() {
        assertTrue(emprendimiento.perteneceA(propietario));
        assertFalse(emprendimiento.perteneceA(otroUsuario));
    }
}
