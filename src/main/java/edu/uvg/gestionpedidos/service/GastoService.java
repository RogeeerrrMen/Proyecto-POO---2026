package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.GastoDTO;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.Gasto;
import edu.uvg.gestionpedidos.repository.GastoRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class GastoService {

    private static final int DESCRIPCION_MAX = 500;

    private static final BigDecimal MONTO_MAX =
            new BigDecimal("999999999999.99");

    private final GastoRepository gastoRepository;
    private final EmprendimientoService emprendimientoService;

    public GastoService(
            GastoRepository gastoRepository,
            EmprendimientoService emprendimientoService) {

        this.gastoRepository = gastoRepository;
        this.emprendimientoService = emprendimientoService;
    }

    @Transactional(readOnly = true)
    public List<Gasto> listarGastos() {

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        return gastoRepository
                .findByEmprendimientoIdOrderByFechaDescIdDesc(
                        emprendimientoId);
    }

    @Transactional(readOnly = true)
    public Optional<Gasto> buscarGastoPorId(Long id) {

        validarId(id);

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        return gastoRepository.findByIdAndEmprendimientoId(
                id,
                emprendimientoId);
    }

    @Transactional
    public Gasto registrarGasto(GastoDTO datos) {

        BigDecimal montoValidado = validarDatos(datos);

        Emprendimiento emprendimiento =
                obtenerEmprendimientoActual();

        Gasto gasto = new Gasto();

        gasto.asignarEmprendimiento(emprendimiento);

        aplicarDatos(gasto, datos, montoValidado);

        return gastoRepository.save(gasto);
    }

    @Transactional
    public Gasto modificarGasto(Long id, GastoDTO datos) {

        Gasto gasto = obtenerGasto(id);

        BigDecimal montoValidado = validarDatos(datos);

        aplicarDatos(gasto, datos, montoValidado);

        return gastoRepository.save(gasto);
    }

    @Transactional(readOnly = true)
    public List<Gasto> consultarGastosPorPeriodo(
            LocalDate inicio,
            LocalDate fin) {

        validarPeriodo(inicio, fin);

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        return gastoRepository
                .findByEmprendimientoIdAndFechaBetweenOrderByFechaDescIdDesc(
                        emprendimientoId,
                        inicio,
                        fin);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPorPeriodo(
            LocalDate inicio,
            LocalDate fin) {

        validarPeriodo(inicio, fin);

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        BigDecimal total = gastoRepository
                .sumarMontosPorEmprendimientoYPeriodo(
                        emprendimientoId,
                        inicio,
                        fin);

        return total == null ? BigDecimal.ZERO : total;
    }

    private Gasto obtenerGasto(Long id) {

        validarId(id);

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        return gastoRepository
                .findByIdAndEmprendimientoId(
                        id,
                        emprendimientoId)
                .orElseThrow(() -> new NoSuchElementException(
                        "El gasto no existe o no está disponible."));
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del gasto debe ser positivo.");
        }
    }

    private BigDecimal validarDatos(GastoDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del gasto son obligatorios.");
        }

        String descripcion = datos.getDescripcion();

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción del gasto es obligatoria.");
        }

        if (descripcion.trim().length() > DESCRIPCION_MAX) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los "
                            + DESCRIPCION_MAX + " caracteres.");
        }

        BigDecimal monto = datos.getMonto();

        if (monto == null) {
            throw new IllegalArgumentException(
                    "El monto del gasto es obligatorio.");
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto del gasto debe ser mayor que cero.");
        }

        if (monto.compareTo(MONTO_MAX) > 0) {
            throw new IllegalArgumentException(
                    "El monto supera el máximo permitido: "
                            + MONTO_MAX);
        }

        LocalDate fecha = datos.getFecha();

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha del gasto es obligatoria.");
        }

        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha del gasto no puede ser futura.");
        }

        try {
            return monto.setScale(2, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "El monto debe poder expresarse con dos decimales.");
        }
    }

    private void aplicarDatos(
            Gasto gasto,
            GastoDTO datos,
            BigDecimal montoValidado) {

        gasto.setDescripcion(datos.getDescripcion().trim());
        gasto.setMonto(montoValidado);
        gasto.setFecha(datos.getFecha());
    }

    private void validarPeriodo(
            LocalDate inicio,
            LocalDate fin) {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "Las fechas inicial y final son obligatorias.");
        }

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final.");
        }
    }

    private String obtenerCorreoAutenticado() {

        var autenticacion = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || "anonymousUser".equals(
                        autenticacion.getPrincipal())) {

            throw new AccessDeniedException(
                    "Debes iniciar sesión.");
        }

        return autenticacion.getName();
    }

    private Emprendimiento obtenerEmprendimientoActual() {

        return emprendimientoService
                .buscarMiEmprendimiento(
                        obtenerCorreoAutenticado())
                .orElseThrow(() -> new AccessDeniedException(
                        "Debes registrar tu emprendimiento."));
    }
}
