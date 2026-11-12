package edu.uvg.gestionpedidos.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import edu.uvg.gestionpedidos.dto.CalendarioDTO;
import edu.uvg.gestionpedidos.dto.DiaCalendarioDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.EstadoPedido;
import edu.uvg.gestionpedidos.entity.ModalidadEntrega;
import edu.uvg.gestionpedidos.repository.CalendarioRepository;
import edu.uvg.gestionpedidos.repository.PedidoCalendarioVista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class CalendarioServiceTest {
    private CalendarioRepository repository;
    private EmprendimientoService emprendimientos;
    private CalendarioService service;
    private final LocalDate hoy = LocalDate.of(2026, 11, 10);

    @BeforeEach
    void preparar() {
        repository = mock(CalendarioRepository.class);
        emprendimientos = mock(EmprendimientoService.class);
        Emprendimiento negocio = mock(Emprendimiento.class);
        when(negocio.getId()).thenReturn(42L);
        when(negocio.getNombre()).thenReturn("Negocio de Ana");
        when(emprendimientos.buscarMiEmprendimiento("ana@example.com"))
                .thenReturn(Optional.of(negocio));
        when(repository.buscarProgramados(eq(42L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(repository.buscarSinFechas(42L)).thenReturn(List.of());
        Clock clock = Clock.fixed(Instant.parse("2026-11-10T18:00:00Z"),
                ZoneId.of("America/Guatemala"));
        service = new CalendarioService(repository, emprendimientos, clock);
    }

    @Test
    void usarMesActualYConsultarSoloElNegocioAutenticado() {
        CalendarioDTO calendario = service.consultar("ana@example.com", null);
        assertEquals(YearMonth.of(2026, 11), calendario.getMes());
        assertEquals("Negocio de Ana", calendario.getEmprendimiento());
        verify(repository).buscarProgramados(42L, hoy.withDayOfMonth(1), hoy.withDayOfMonth(30));
        verify(repository).buscarProgramados(42L, hoy, hoy.plusDays(6));
        verify(repository).buscarSinFechas(42L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void formarSemanasCompletasEnUnMesBisiesto() {
        CalendarioDTO calendario = service.consultar("ana@example.com", "2024-02");
        assertEquals(5, calendario.getSemanas().size());
        assertEquals(LocalDate.of(2024, 1, 29), calendario.getSemanas().get(0).get(0).getFecha());
        assertEquals(LocalDate.of(2024, 3, 3), calendario.getSemanas().get(4).get(6).getFecha());
        assertTrue(calendario.getSemanas().stream().allMatch(s -> s.size() == 7));
        assertEquals(29L, calendario.getSemanas().stream().flatMap(List::stream)
                .filter(DiaCalendarioDTO::isDelMes).count());
    }

    @Test
    void permitirMesesDeCuatroYSeisSemanas() {
        assertEquals(4, service.consultar("ana@example.com", "2021-02").getSemanas().size());
        assertEquals(6, service.consultar("ana@example.com", "2020-08").getSemanas().size());
    }

    @Test
    void navegarEntreDiciembreYEnero() {
        CalendarioDTO diciembre = service.consultar("ana@example.com", "2026-12");
        assertEquals("2026-11", diciembre.getMesAnterior());
        assertEquals("2027-01", diciembre.getMesSiguiente());
    }

    @Test
    void mostrarTresActividadesSinInventarHoras() {
        PedidoCalendarioVista pedido = pedido(1L, EstadoPedido.REGISTRADO);
        when(pedido.getFechaPreparacion()).thenReturn(hoy);
        when(pedido.getFechaEnvio()).thenReturn(hoy);
        when(pedido.getFechaEntrega()).thenReturn(hoy);
        when(repository.buscarProgramados(42L, hoy.withDayOfMonth(1), hoy.withDayOfMonth(30)))
                .thenReturn(List.of(pedido));

        CalendarioDTO calendario = service.consultar("ana@example.com", "2026-11");
        DiaCalendarioDTO dia = calendario.getSemanas().stream().flatMap(List::stream)
                .filter(d -> d.getFecha().equals(hoy)).findFirst().orElseThrow();
        assertTrue(dia.isHoy());
        assertEquals(3, calendario.getCantidadEventos());
        assertEquals(List.of("preparacion", "envio", "entrega"),
                dia.getEventos().stream().map(e -> e.getTipo()).toList());
        assertTrue(dia.getEventos().stream().allMatch(e -> e.getHora() == null));
        assertTrue(dia.getEventos().stream().allMatch(e -> e.getPedidoId().equals(1L)));
    }

    @Test
    void excluirFechasFueraDelMesAunqueElPedidoTengaOtraFechaDentro() {
        PedidoCalendarioVista pedido = pedido(1L, EstadoPedido.REGISTRADO);
        when(pedido.getFechaPreparacion()).thenReturn(hoy);
        when(pedido.getFechaEnvio()).thenReturn(LocalDate.of(2026, 12, 1));
        when(repository.buscarProgramados(42L, hoy.withDayOfMonth(1), hoy.withDayOfMonth(30)))
                .thenReturn(List.of(pedido));
        assertEquals(1, service.consultar("ana@example.com", "2026-11").getCantidadEventos());
    }

    @Test
    void ordenarHorasYColocarSinHoraAlFinal() {
        PedidoCalendarioVista sinHora = pedido(1L, EstadoPedido.REGISTRADO);
        PedidoCalendarioVista tarde = pedido(2L, EstadoPedido.REGISTRADO);
        PedidoCalendarioVista temprano = pedido(3L, EstadoPedido.REGISTRADO);
        for (PedidoCalendarioVista pedido : List.of(sinHora, tarde, temprano)) {
            when(pedido.getFechaEntrega()).thenReturn(hoy);
        }
        when(tarde.getHoraEntrega()).thenReturn(LocalTime.of(15, 0));
        when(temprano.getHoraEntrega()).thenReturn(LocalTime.of(9, 0));
        when(repository.buscarProgramados(42L, hoy.withDayOfMonth(1), hoy.withDayOfMonth(30)))
                .thenReturn(List.of(sinHora, tarde, temprano));
        DiaCalendarioDTO dia = service.consultar("ana@example.com", "2026-11")
                .getSemanas().stream().flatMap(List::stream)
                .filter(d -> d.getFecha().equals(hoy)).findFirst().orElseThrow();
        assertEquals(List.of(3L, 2L, 1L),
                dia.getEventos().stream().map(e -> e.getPedidoId()).toList());
    }

    @Test
    void recordatoriosIncluyenHoyYDiaSeisPeroNoOtrosDiasNiEstadosFinales() {
        PedidoCalendarioVista primero = pedido(1L, EstadoPedido.REGISTRADO);
        PedidoCalendarioVista ultimo = pedido(2L, EstadoPedido.LISTO);
        PedidoCalendarioVista fuera = pedido(3L, EstadoPedido.REGISTRADO);
        PedidoCalendarioVista pasado = pedido(4L, EstadoPedido.REGISTRADO);
        PedidoCalendarioVista entregado = pedido(5L, EstadoPedido.ENTREGADO);
        PedidoCalendarioVista cancelado = pedido(6L, EstadoPedido.CANCELADO);
        PedidoCalendarioVista fallido = pedido(7L, EstadoPedido.FALLIDO);
        when(primero.getFechaEntrega()).thenReturn(hoy);
        when(ultimo.getFechaEntrega()).thenReturn(hoy.plusDays(6));
        when(fuera.getFechaEntrega()).thenReturn(hoy.plusDays(7));
        when(pasado.getFechaEntrega()).thenReturn(hoy.minusDays(1));
        when(entregado.getFechaEntrega()).thenReturn(hoy);
        when(cancelado.getFechaEntrega()).thenReturn(hoy);
        when(fallido.getFechaEntrega()).thenReturn(hoy);
        when(repository.buscarProgramados(42L, hoy, hoy.plusDays(6)))
                .thenReturn(List.of(primero, ultimo, fuera, pasado, entregado, cancelado, fallido));
        CalendarioDTO calendario = service.consultar("ana@example.com", "2027-01");
        assertEquals(List.of(1L, 2L), calendario.getRecordatorios().stream()
                .map(e -> e.getPedidoId()).toList());
        assertEquals(hoy.plusDays(6), calendario.getFinRecordatorios());
    }

    @Test
    void mantenerEstadosFinalesEnElCalendarioHistoricoYUsarEtiquetaRecogida() {
        PedidoCalendarioVista pedido = pedido(1L, EstadoPedido.ENTREGADO);
        when(pedido.getFechaEntrega()).thenReturn(hoy);
        when(pedido.getModalidadEntrega()).thenReturn(ModalidadEntrega.RECOGER_NEGOCIO);
        when(repository.buscarProgramados(42L, hoy.withDayOfMonth(1), hoy.withDayOfMonth(30)))
                .thenReturn(List.of(pedido));
        DiaCalendarioDTO dia = service.consultar("ana@example.com", "2026-11")
                .getSemanas().stream().flatMap(List::stream)
                .filter(d -> d.getFecha().equals(hoy)).findFirst().orElseThrow();
        assertEquals("Recogida", dia.getEventos().get(0).getEtiqueta());
        assertEquals(EstadoPedido.ENTREGADO, dia.getEventos().get(0).getEstado());
    }

    
        @Test
        void listarPedidosSinFechaYNoCrearEventosParaEllos() {

        // Crear primero el objeto simulado.
        PedidoCalendarioVista pedidoSinFecha =
                pedido(9L, EstadoPedido.REGISTRADO);

        // Configurar después el repositorio.
        when(repository.buscarSinFechas(42L))
                .thenReturn(List.of(pedidoSinFecha));

        CalendarioDTO calendario =
                service.consultar("ana@example.com", "2026-11");

        assertEquals(0, calendario.getCantidadEventos());

        assertEquals(
                9L,
                calendario.getSinFecha().get(0).getId()
        );

        assertThrows(
                UnsupportedOperationException.class,
                calendario.getSinFecha()::clear
        );
        }


    @Test
    void rechazarMesInvalidoSinConsultarPedidos() {
        for (String mes : List.of("2026-13", "2026-00", "0000-01", "2026-1", "texto")) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.consultar("ana@example.com", mes));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void noConsultarPedidosSiNoHayEmprendimiento() {
        when(emprendimientos.buscarMiEmprendimiento("ana@example.com"))
                .thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.consultar("ana@example.com", null));
        verifyNoInteractions(repository);
    }

    private PedidoCalendarioVista pedido(Long id, EstadoPedido estado) {
        PedidoCalendarioVista pedido = mock(PedidoCalendarioVista.class);
        when(pedido.getId()).thenReturn(id);
        when(pedido.getCodigo()).thenReturn("PED-" + id);
        when(pedido.getNombreCliente()).thenReturn("Ana");
        when(pedido.getEstado()).thenReturn(estado);
        when(pedido.getModalidadEntrega()).thenReturn(ModalidadEntrega.ENVIO_DOMICILIO);
        return pedido;
    }
}
