package edu.uvg.gestionpedidos.service;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import edu.uvg.gestionpedidos.dto.CalendarioDTO;
import edu.uvg.gestionpedidos.dto.DiaCalendarioDTO;
import edu.uvg.gestionpedidos.dto.EventoCalendarioDTO;
import edu.uvg.gestionpedidos.dto.PedidoSinFechaDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.EstadoPedido;
import edu.uvg.gestionpedidos.entity.ModalidadEntrega;
import edu.uvg.gestionpedidos.repository.CalendarioRepository;
import edu.uvg.gestionpedidos.repository.PedidoCalendarioVista;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarioService {
    private final CalendarioRepository calendarioRepository;
    private final EmprendimientoService emprendimientoService;
    private final Clock clock;

    public CalendarioService(CalendarioRepository calendarioRepository,
            EmprendimientoService emprendimientoService,
            @Qualifier("calendarioClock") Clock clock) {
        this.calendarioRepository = calendarioRepository;
        this.emprendimientoService = emprendimientoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CalendarioDTO consultar(String correo, String mesSolicitado) {
        LocalDate hoy = LocalDate.now(clock);
        YearMonth mes = resolverMes(mesSolicitado, hoy);
        Emprendimiento negocio = emprendimientoService.buscarMiEmprendimiento(correo)
                .orElseThrow(() -> new NoSuchElementException("Registra tu emprendimiento para acceder al calendario."));

        LocalDate inicio = mes.atDay(1);
        LocalDate fin = mes.atEndOfMonth();
        List<EventoCalendarioDTO> eventos = convertirEventos(calendarioRepository.buscarProgramados(negocio.getId(), inicio, fin), inicio, fin, false);
        List<EventoCalendarioDTO> recordatorios = convertirEventos(calendarioRepository.buscarProgramados(negocio.getId(), hoy, hoy.plusDays(6)), hoy, hoy.plusDays(6), true);
        List<PedidoSinFechaDTO> sinFecha = calendarioRepository
                .buscarSinFechas(negocio.getId()).stream()
                .map(p -> new PedidoSinFechaDTO(p.getId(), p.getCodigo(), p.getNombreCliente(), p.getEstado()))
                .toList();

        return new CalendarioDTO(mes, hoy, negocio.getNombre(), crearSemanas(mes, hoy, eventos), recordatorios, sinFecha, eventos.size());
    }

    private YearMonth resolverMes(String texto, LocalDate hoy) {
        if (texto == null || texto.isBlank()) {
            return YearMonth.from(hoy);
        }
        try {
            if (!texto.matches("[0-9]{4}-(0[1-9]|1[0-2])")) {
                throw new IllegalArgumentException();
            }
            YearMonth mes = YearMonth.parse(texto);
            if (mes.getYear() < 1) {
                throw new IllegalArgumentException();
            }
            return mes;
        } catch (DateTimeException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Selecciona un mes válido con formato AAAA-MM (Año y mes).");
        }
    }

    private List<EventoCalendarioDTO> convertirEventos(List<PedidoCalendarioVista> pedidos, LocalDate desde, LocalDate hasta, boolean soloActivos) {
        List<EventoCalendarioDTO> eventos = new ArrayList<>();
        for (PedidoCalendarioVista pedido : pedidos) {
            if (soloActivos && esFinal(pedido.getEstado())) {
                continue;
            }
            agregarEvento(eventos, pedido, pedido.getFechaPreparacion(), pedido.getHoraPreparacion(), "preparacion", "Preparación", desde, hasta);
            agregarEvento(eventos, pedido, pedido.getFechaEnvio(), pedido.getHoraEnvio(), "envio", "Envío", desde, hasta);
            String etiquetaEntrega = pedido.getModalidadEntrega() == ModalidadEntrega.RECOGER_NEGOCIO
                    ? "Recogida" : "Entrega";
            agregarEvento(eventos, pedido, pedido.getFechaEntrega(), pedido.getHoraEntrega(), "entrega", etiquetaEntrega, desde, hasta);
        }
        eventos.sort(Comparator.comparing(EventoCalendarioDTO::getFecha)
                .thenComparing(EventoCalendarioDTO::getHora, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(EventoCalendarioDTO::getPedidoId)
                .thenComparingInt(evento -> switch (evento.getTipo()) {
                    case "preparacion" -> 0;
                    case "envio" -> 1;
                    default -> 2;
                }));
        return List.copyOf(eventos);
    }

    private void agregarEvento(List<EventoCalendarioDTO> eventos, PedidoCalendarioVista pedido, LocalDate fecha, LocalTime hora, String tipo, String etiqueta, LocalDate desde, LocalDate hasta) {
        if (fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
            eventos.add(new EventoCalendarioDTO(pedido.getId(), pedido.getCodigo(), pedido.getNombreCliente(), pedido.getEstado(), fecha, hora, tipo, etiqueta));
        }
    }

    private boolean esFinal(EstadoPedido estado) {
        return estado == EstadoPedido.ENTREGADO || estado == EstadoPedido.CANCELADO
                || estado == EstadoPedido.FALLIDO;
    }

    private List<List<DiaCalendarioDTO>> crearSemanas(YearMonth mes, LocalDate hoy, List<EventoCalendarioDTO> eventos) {
        Map<LocalDate, List<EventoCalendarioDTO>> porFecha = eventos.stream()
                .collect(Collectors.groupingBy(EventoCalendarioDTO::getFecha));
        LocalDate primero = mes.atDay(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate ultimo = mes.atEndOfMonth()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<List<DiaCalendarioDTO>> semanas = new ArrayList<>();
        List<DiaCalendarioDTO> semana = new ArrayList<>();
        for (LocalDate fecha = primero; !fecha.isAfter(ultimo); fecha = fecha.plusDays(1)) {
            semana.add(new DiaCalendarioDTO(fecha, YearMonth.from(fecha).equals(mes),
                    fecha.equals(hoy), porFecha.getOrDefault(fecha, List.of())));
            if (semana.size() == 7) {
                semanas.add(List.copyOf(semana));
                semana.clear();
            }
        }
        return List.copyOf(semanas);
    }
}
