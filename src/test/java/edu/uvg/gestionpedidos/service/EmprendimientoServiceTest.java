package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.EmprendimientoDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.EmprendimientoRepository;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmprendimientoServiceTest {

    //Correo de prueba para el usuario autenticado
    private static final String CORREO = "santi@example.com";

    private EmprendimientoRepository emprendimientoRepository;
    private UsuarioRepository usuarioRepository;
    private EmprendimientoService servicio;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        emprendimientoRepository =
                mock(EmprendimientoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        servicio = new EmprendimientoService(
                emprendimientoRepository,
                usuarioRepository
        );

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo(CORREO);

        when(usuarioRepository.findByCorreoIgnoreCase(CORREO))
                .thenReturn(Optional.of(usuario));

        when(emprendimientoRepository.save(
                any(Emprendimiento.class)
        )).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private EmprendimientoDTO datos(String nombre) {
        EmprendimientoDTO dto = new EmprendimientoDTO();
        dto.setNombre(nombre);
        return dto;
    }

    private Emprendimiento emprendimientoPropio() {
        Emprendimiento emprendimiento =
                new Emprendimiento("Dulces Santi", usuario);

        when(emprendimientoRepository.findByIdAndPropietario(
                1L, usuario
        )).thenReturn(Optional.of(emprendimiento));

        return emprendimiento;
    }

    private Emprendimiento emprendimientoAjeno() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        otroUsuario.setCorreo("otra@example.com");

        return new Emprendimiento("Negocio ajeno", otroUsuario);
    }

    private void assertRegistroRechazado(EmprendimientoDTO dto) {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarEmprendimiento(dto, CORREO)
        );

        verify(emprendimientoRepository, never())
                .save(any(Emprendimiento.class));
    }

    //REGISTRO

    @Test
    void registrarAsociaElUsuarioCorrectoYLimpiaElNombre() {
        Emprendimiento resultado =
                servicio.registrarEmprendimiento(
                        datos("  Dulces Santi  "),
                        CORREO
                );

        ArgumentCaptor<Emprendimiento> captor =
                ArgumentCaptor.forClass(Emprendimiento.class);

        verify(emprendimientoRepository).save(captor.capture());

        Emprendimiento guardado = captor.getValue();

        assertEquals("Dulces Santi", guardado.getNombre());
        assertSame(usuario, guardado.getPropietario());
        assertSame(guardado, resultado);
    }

    @Test
    void registrarAceptaNombreDe120Caracteres() {
        String nombre = "a".repeat(120);

        Emprendimiento resultado =
                servicio.registrarEmprendimiento(
                        datos(nombre),
                        CORREO
                );

        assertEquals(nombre, resultado.getNombre());
        verify(emprendimientoRepository).save(resultado);
    }

    @Test
    void registrarRechazaDatosNulos() {
        assertRegistroRechazado(null);
    }

    @Test
    void registrarRechazaNombresInvalidos() {
        assertRegistroRechazado(datos(null));
        assertRegistroRechazado(datos(""));
        assertRegistroRechazado(datos("   "));
        assertRegistroRechazado(datos("a".repeat(121)));
    }

    @Test
    void registrarRechazaUnSegundoEmprendimiento() {
        when(emprendimientoRepository.existsByPropietario(usuario))
                .thenReturn(true);

        assertRegistroRechazado(datos("Otro negocio"));
    }

    //CONSULTAS

    @Test
    void buscarMiEmprendimientoDevuelveElNegocioPropio() {
        Emprendimiento propio =
                new Emprendimiento("Dulces Santi", usuario);

        when(emprendimientoRepository.findByPropietario(usuario))
                .thenReturn(Optional.of(propio));

        Optional<Emprendimiento> resultado =
                servicio.buscarMiEmprendimiento(CORREO);

        assertSame(propio, resultado.orElseThrow());
        assertSame(usuario, resultado.orElseThrow().getPropietario());

        verify(emprendimientoRepository).findByPropietario(usuario);
    }

    @Test
    void buscarMiEmprendimientoDevuelveVacioSiNoTieneUno() {
        when(emprendimientoRepository.findByPropietario(usuario))
                .thenReturn(Optional.empty());

        assertTrue(
                servicio.buscarMiEmprendimiento(CORREO).isEmpty()
        );
    }

    @Test
    void consultarPermiteAccederAlEmprendimientoPropio() {
        Emprendimiento propio = emprendimientoPropio();

        Emprendimiento resultado =
                servicio.consultarEmprendimiento(1L, CORREO);

        assertSame(propio, resultado);
    }

    @Test
    void consultarRechazaUnEmprendimientoInexistente() {
        when(emprendimientoRepository.findByIdAndPropietario(
                99L, usuario
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> servicio.consultarEmprendimiento(99L, CORREO)
        );
    }

    @Test
    void consultarRechazaUnEmprendimientoAjeno() {
        Emprendimiento ajeno = emprendimientoAjeno();

        //El negocio existe, pero pertenece a otra cuenta
        when(emprendimientoRepository.findById(2L))
                .thenReturn(Optional.of(ajeno));

        when(emprendimientoRepository.findByIdAndPropietario(
                2L, usuario
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> servicio.consultarEmprendimiento(2L, CORREO)
        );

        verify(emprendimientoRepository)
                .findByIdAndPropietario(2L, usuario);
    }

    //MODIFICACIÓN

    @Test
    void modificarActualizaElNombreYConservaElPropietario() {
        Emprendimiento propio = emprendimientoPropio();

        Emprendimiento resultado =
                servicio.modificarEmprendimiento(
                        1L,
                        datos("  Repostería Santi  "),
                        CORREO
                );

        assertSame(propio, resultado);
        assertEquals("Repostería Santi", resultado.getNombre());
        assertSame(usuario, resultado.getPropietario());

        verify(emprendimientoRepository).save(propio);
    }

    @Test
    void modificarConNombreInvalidoConservaLosDatosOriginales() {
        Emprendimiento propio = emprendimientoPropio();

        String[] nombresInvalidos = {
                null, "", "   ", "a".repeat(121)
        };

        for (String nombre : nombresInvalidos) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.modificarEmprendimiento(
                            1L,
                            datos(nombre),
                            CORREO
                    )
            );

            assertEquals("Dulces Santi", propio.getNombre());
            assertSame(usuario, propio.getPropietario());
        }

        verify(emprendimientoRepository, never())
                .save(any(Emprendimiento.class));
    }

    @Test
    void modificarConDatosNulosConservaLosDatosOriginales() {
        Emprendimiento propio = emprendimientoPropio();

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.modificarEmprendimiento(
                        1L, null, CORREO
                )
        );

        assertEquals("Dulces Santi", propio.getNombre());
        assertSame(usuario, propio.getPropietario());

        verify(emprendimientoRepository, never())
                .save(any(Emprendimiento.class));
    }

    @Test
    void modificarRechazaUnEmprendimientoInexistente() {
        when(emprendimientoRepository.findByIdAndPropietario(
                99L, usuario
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> servicio.modificarEmprendimiento(
                        99L,
                        datos("Nuevo nombre"),
                        CORREO
                )
        );

        verify(emprendimientoRepository, never())
                .save(any(Emprendimiento.class));
    }

    @Test
    void modificarRechazaUnEmprendimientoAjenoSinAlterarlo() {
        Emprendimiento ajeno = emprendimientoAjeno();
        Usuario propietarioOriginal = ajeno.getPropietario();

        when(emprendimientoRepository.findById(2L))
                .thenReturn(Optional.of(ajeno));

        when(emprendimientoRepository.findByIdAndPropietario(
                2L, usuario
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> servicio.modificarEmprendimiento(
                        2L,
                        datos("Nombre alterado"),
                        CORREO
                )
        );

        assertEquals("Negocio ajeno", ajeno.getNombre());
        assertSame(propietarioOriginal, ajeno.getPropietario());

        verify(emprendimientoRepository, never())
                .save(any(Emprendimiento.class));
    }

    //IDENTIDAD E IDENTIFICADORES

    @Test
    void consultarOModificarRechazaIdentificadoresInvalidos() {
        Long[] identificadoresInvalidos = {null, 0L, -1L};

        for (Long id : identificadoresInvalidos) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.consultarEmprendimiento(id, CORREO)
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.modificarEmprendimiento(
                            id,
                            datos("Nuevo nombre"),
                            CORREO
                    )
            );
        }

        verifyNoInteractions(emprendimientoRepository);
    }

    @Test
    void todasLasOperacionesRechazanCorreoNuloOVacio() {
        assertAccesoRechazado(null);
        assertAccesoRechazado("");
        assertAccesoRechazado("   ");

        verifyNoInteractions(
                usuarioRepository,
                emprendimientoRepository
        );
    }

    @Test
    void todasLasOperacionesRechazanUnaCuentaInexistente() {
        String correoInexistente = "inexistente@example.com";

        when(usuarioRepository.findByCorreoIgnoreCase(
                correoInexistente
        )).thenReturn(Optional.empty());

        assertAccesoRechazado(correoInexistente);

        verifyNoInteractions(emprendimientoRepository);
    }

    @Test
    void buscarUsuarioEliminaEspaciosDelCorreo() {
        String correoMayusculas = "SANTI@EXAMPLE.COM";

        when(usuarioRepository.findByCorreoIgnoreCase(
                correoMayusculas
        )).thenReturn(Optional.of(usuario));

        when(emprendimientoRepository.findByPropietario(usuario))
                .thenReturn(Optional.empty());

        assertTrue(
                servicio.buscarMiEmprendimiento(
                        "  SANTI@EXAMPLE.COM  "
                ).isEmpty()
        );

        verify(usuarioRepository)
                .findByCorreoIgnoreCase(correoMayusculas);
    }

    private void assertAccesoRechazado(String correo) {
        assertThrows(
                AccessDeniedException.class,
                () -> servicio.buscarMiEmprendimiento(correo)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> servicio.consultarEmprendimiento(1L, correo)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> servicio.registrarEmprendimiento(
                        datos("Mi negocio"),
                        correo
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () -> servicio.modificarEmprendimiento(
                        1L,
                        datos("Nuevo nombre"),
                        correo
                )
        );
    }
}