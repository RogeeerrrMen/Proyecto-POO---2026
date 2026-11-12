package edu.uvg.gestionpedidos.service;


import edu.uvg.gestionpedidos.dto.GastoDTO;
import edu.uvg.gestionpedidos.entity.Gasto;
import edu.uvg.gestionpedidos.repository.GastoRepository;
import edu.uvg.gestionpedidos.entity.Emprendimiento;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//Clase de prueba para GastoService, usando Mockito para simular el repositorio.

class GastoServiceTest {

    private GastoRepository repositorio;
    private EmprendimientoService emprendimientoService;
    private GastoService servicio;
    private Emprendimiento emprendimiento;
    private LocalDate fecha;

    private static final Long EMPRENDIMIENTO_ID = 1L;

    
        @BeforeEach
        void setUp() {

        repositorio = mock(GastoRepository.class);
        emprendimientoService = mock(EmprendimientoService.class);

        servicio = new GastoService(
                repositorio,
                emprendimientoService
        );

        fecha = LocalDate.now().minusDays(1);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "prueba@example.com",
                        "password",
                        List.of()
                )
        );

        emprendimiento = mock(Emprendimiento.class);

        when(emprendimiento.getId())
                .thenReturn(EMPRENDIMIENTO_ID);

        when(emprendimientoService.buscarMiEmprendimiento(
                "prueba@example.com"))
                .thenReturn(Optional.of(emprendimiento));

        when(repositorio.save(any(Gasto.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));
        }

    @AfterEach
    void limpiarSeguridad() {

        SecurityContextHolder.clearContext();
    }    


    //Crea unos datos para los casos de prueba.
    private GastoDTO datos(
            String descripcion,
            String monto,
            LocalDate fechaGasto
    ) {
        GastoDTO dto = new GastoDTO();
        dto.setDescripcion(descripcion);
        dto.setMonto(monto == null ? null : new BigDecimal(monto));
        dto.setFecha(fechaGasto);
        return dto;
    }

    //Simula un gasto que ya existe en la base de datos.
    private Gasto gastoExistente() {

        Gasto gasto = new Gasto();
        gasto.setDescripcion("Compra de materiales");
        gasto.setMonto(new BigDecimal("125.50"));
        gasto.setFecha(fecha);

        when(repositorio.findByIdAndEmprendimientoId(
        1L, EMPRENDIMIENTO_ID))
        .thenReturn(Optional.of(gasto));

        return gasto;
    }

    //Realiza una comprobación para asegurar que los datos inválidos nunca lleguen a save.
    private void assertRegistroRechazado(GastoDTO dto) {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarGasto(dto)
        );

        verify(repositorio, never()).save(any(Gasto.class));
    }

    // REGISTRO

    @Test
    void registrarGastoGuardaDatosLimpiosYNormalizaMonto() {
        Gasto resultado = servicio.registrarGasto(
                datos("  Compra de materiales  ", "125.5", fecha)
        );

        ArgumentCaptor<Gasto> captor =
                ArgumentCaptor.forClass(Gasto.class);

        verify(repositorio).save(captor.capture());
        Gasto guardado = captor.getValue();

        assertEquals("Compra de materiales", guardado.getDescripcion());
        assertEquals(new BigDecimal("125.50"), guardado.getMonto());
        assertEquals(fecha, guardado.getFecha());
        assertSame(guardado, resultado);
        assertSame(emprendimiento,guardado.getEmprendimiento());
    }

    @Test
    void registrarGastoAceptaLimitesPermitidos() {
        LocalDate hoy = LocalDate.now();
        String descripcion = "a".repeat(500);

        Gasto resultado = servicio.registrarGasto(
                datos(descripcion, "999999999999.99", hoy)
        );

        assertEquals(descripcion, resultado.getDescripcion());
        assertEquals(
                new BigDecimal("999999999999.99"),
                resultado.getMonto()
        );
        assertEquals(hoy, resultado.getFecha());
        verify(repositorio).save(resultado);
    }

    @Test
    void registrarGastoRechazaDatosNulos() {
        assertRegistroRechazado(null);
    }

    @Test
    void registrarGastoRechazaDescripcionInvalida() {
        assertRegistroRechazado(datos(null, "10.00", fecha));
        assertRegistroRechazado(datos("", "10.00", fecha));
        assertRegistroRechazado(datos("   ", "10.00", fecha));
        assertRegistroRechazado(
                datos("a".repeat(501), "10.00", fecha)
        );
    }

    @Test
    void registrarGastoRechazaMontoNuloCeroONegativo() {
        assertRegistroRechazado(datos("Materiales", null, fecha));
        assertRegistroRechazado(datos("Materiales", "0.00", fecha));
        assertRegistroRechazado(datos("Materiales", "-0.01", fecha));
    }

    @Test
    void registrarGastoRechazaMontoSuperiorAlLimite() {
        assertRegistroRechazado(
                datos("Materiales", "1000000000000.00", fecha)
        );
    }

    @Test
    void registrarGastoRechazaMontoQueRequiereRedondeo() {
        assertRegistroRechazado(
                datos("Materiales", "12.501", fecha)
        );
    }

    @Test
    void registrarGastoRechazaFechaNulaOFutura() {
        assertRegistroRechazado(datos("Materiales", "10.00", null));

        assertRegistroRechazado(
                datos("Materiales", "10.00", LocalDate.now().plusDays(1))
        );
    }

    // MODIFICACIÓN

    @Test
    void modificarGastoActualizaElObjetoExistente() {
        Gasto existente = gastoExistente();
        LocalDate nuevaFecha = fecha.minusDays(2);

        Gasto resultado = servicio.modificarGasto(
                1L,
                datos("  Pago de transporte  ", "80.75", nuevaFecha)
        );

        assertSame(existente, resultado);
        assertEquals("Pago de transporte", resultado.getDescripcion());
        assertEquals(new BigDecimal("80.75"), resultado.getMonto());
        assertEquals(nuevaFecha, resultado.getFecha());
        verify(repositorio).save(existente);
    }

    @Test
    void modificarGastoInexistenteLanzaError() {

        when(repositorio.findByIdAndEmprendimientoId(
        99L, EMPRENDIMIENTO_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> servicio.modificarGasto(
                        99L,
                        datos("Materiales", "20.00", fecha)
                )
        );

        verify(repositorio, never()).save(any(Gasto.class));
    }

    @Test
    void modificarConDatosInvalidosConservaLosDatosOriginales() {
        Gasto existente = gastoExistente();

        List<GastoDTO> cambiosInvalidos = List.of(
                datos("Otra descripción", "-1.00", fecha),
                datos("   ", "50.00", fecha),
                datos("Otra descripción", "50.00",
                        LocalDate.now().plusDays(1)),
                datos("Otra descripción", "50.001", fecha)
        );

        for (GastoDTO cambio : cambiosInvalidos) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.modificarGasto(1L, cambio)
            );

            assertEquals(
                    "Compra de materiales",
                    existente.getDescripcion()
            );
            assertEquals(
                    new BigDecimal("125.50"),
                    existente.getMonto()
            );
            assertEquals(fecha, existente.getFecha());
        }

        verify(repositorio, never()).save(any(Gasto.class));
    }

    // CONSULTAS


        @Test
        void listarGastosDevuelveLosRegistrosDelRepositorio() {

        Gasto existente = gastoExistente();

        when(repositorio.findByEmprendimientoIdOrderByFechaDescIdDesc(
                EMPRENDIMIENTO_ID))
                .thenReturn(List.of(existente));

        assertEquals(
                List.of(existente),
                servicio.listarGastos()
        );

        verify(repositorio)
                .findByEmprendimientoIdOrderByFechaDescIdDesc(
                        EMPRENDIMIENTO_ID);
        }

    
        @Test
        void listarGastosSinRegistrosDevuelveListaVacia() {

        when(repositorio.findByEmprendimientoIdOrderByFechaDescIdDesc(
                EMPRENDIMIENTO_ID))
                .thenReturn(List.of());

        assertTrue(servicio.listarGastos().isEmpty());
        }


    @Test
    void buscarGastoDevuelveElRegistroOVacioSiNoExiste() {
        Gasto existente = gastoExistente();

        when(repositorio.findByIdAndEmprendimientoId(
        99L, EMPRENDIMIENTO_ID))
                .thenReturn(Optional.empty());

        assertSame(
                existente,
                servicio.buscarGastoPorId(1L).orElseThrow()
        );
        assertTrue(servicio.buscarGastoPorId(99L).isEmpty());
    }

    @Test
    void buscarOModificarConIdInvalidoNoConsultaElRepositorio() {
        GastoDTO dto = datos("Materiales", "10.00", fecha);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.buscarGastoPorId(null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.buscarGastoPorId(0L));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.buscarGastoPorId(-1L));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificarGasto(null, dto));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificarGasto(0L, dto));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificarGasto(-1L, dto));

        verifyNoInteractions(repositorio);
    }

    // PERÍODOS Y TOTALES

    @Test
    void consultarPeriodoUsaLasFechasSolicitadas() {
        Gasto existente = gastoExistente();
        LocalDate inicio = fecha.minusDays(7);

        when(repositorio.findByEmprendimientoIdAndFechaBetweenOrderByFechaDescIdDesc(
        EMPRENDIMIENTO_ID, inicio, fecha)).thenReturn(List.of(existente));

        assertEquals(
                List.of(existente),
                servicio.consultarGastosPorPeriodo(inicio, fecha)
        );

        verify(repositorio)
        .findByEmprendimientoIdAndFechaBetweenOrderByFechaDescIdDesc(
                EMPRENDIMIENTO_ID,
                inicio,
                fecha);
    }

    @Test
        void calcularTotalSinGastosDevuelveCero() {

        when(repositorio.sumarMontosPorEmprendimientoYPeriodo(
                EMPRENDIMIENTO_ID,
                fecha,
                fecha))
                .thenReturn(null);

        assertEquals(
                BigDecimal.ZERO,
                servicio.calcularTotalPorPeriodo(fecha, fecha)
        );
        }

    @Test
    void calcularTotalConservaLaPrecisionDelResultadoDelRepositorio() {
        LocalDate inicio = fecha.minusDays(7);

        //Un total puede superar el límite de un gasto individual
        BigDecimal total = new BigDecimal("1234567890123456.78");

        when(repositorio.sumarMontosPorEmprendimientoYPeriodo(
        EMPRENDIMIENTO_ID, inicio, fecha))
                .thenReturn(total);

        assertEquals(
                total,
                servicio.calcularTotalPorPeriodo(inicio, fecha)
        );

        verify(repositorio)
        .sumarMontosPorEmprendimientoYPeriodo(
                EMPRENDIMIENTO_ID,
                inicio,
                fecha);
    }


    @Test
    void consultarOCalcularConPeriodoInvalidoNoConsultaElRepositorio() {
        assertPeriodoRechazado(null, fecha);
        assertPeriodoRechazado(fecha, null);
        assertPeriodoRechazado(null, null);
        assertPeriodoRechazado(fecha, fecha.minusDays(1));

        verifyNoInteractions(repositorio);
    }

    private void assertPeriodoRechazado(
            LocalDate inicio,
            LocalDate fin
    ) {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.consultarGastosPorPeriodo(inicio, fin)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.calcularTotalPorPeriodo(inicio, fin)
        );
    }
}