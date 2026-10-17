package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.GastoDTO;
import edu.uvg.gestionpedidos.entity.Gasto;
import edu.uvg.gestionpedidos.repository.GastoRepository;
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

    //Límite de una columna con precision = 14 y scale = 2.
    private static final BigDecimal MONTO_MAX =
            new BigDecimal("999999999999.99");

    private final GastoRepository gastoRepository;

    public GastoService(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @Transactional(readOnly = true)
    public List<Gasto> listarGastos() {
        return gastoRepository.findAllByOrderByFechaDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Gasto> buscarGastoPorId(Long id) {
        validarId(id);
        return gastoRepository.findById(id);
    }

    @Transactional
    public Gasto registrarGasto(GastoDTO datos) {
        BigDecimal montoValidado = validarDatos(datos);

        Gasto gasto = new Gasto();
        aplicarDatos(gasto, datos, montoValidado);

        return gastoRepository.save(gasto);
    }

    @Transactional
    public Gasto modificarGasto(Long id, GastoDTO datos) {
        Gasto gasto = obtenerGasto(id);

        //Realiza una validacion general antes de modificar el objeto existente.
        BigDecimal montoValidado = validarDatos(datos);
        aplicarDatos(gasto, datos, montoValidado);

        return gastoRepository.save(gasto);
    }

    @Transactional(readOnly = true)
    public List<Gasto> consultarGastosPorPeriodo(
            LocalDate inicio,
            LocalDate fin
    ) {
        validarPeriodo(inicio, fin);

        return gastoRepository
                .findByFechaBetweenOrderByFechaDescIdDesc(inicio, fin);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPorPeriodo(
            LocalDate inicio,
            LocalDate fin
    ) {
        validarPeriodo(inicio, fin);

        BigDecimal total =
                gastoRepository.sumarMontosPorPeriodo(inicio, fin);

        return total == null ? BigDecimal.ZERO : total;
    }

    private Gasto obtenerGasto(Long id) {
        validarId(id);

        return gastoRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("El gasto no existe."));
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
                    "El monto supera el máximo permitido: " + MONTO_MAX);
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

    //Aplica los datos del DTO al objeto Gasto, después de la validación.
    private void aplicarDatos(
            Gasto gasto,
            GastoDTO datos,
            BigDecimal montoValidado
    ) {
        gasto.setDescripcion(datos.getDescripcion().trim());
        gasto.setMonto(montoValidado);
        gasto.setFecha(datos.getFecha());
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "Las fechas inicial y final son obligatorias.");
        }

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final.");
        }
    }
}