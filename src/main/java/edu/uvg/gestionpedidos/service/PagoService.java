package edu.uvg.gestionpedidos.service;

import java.math.BigDecimal;
import java.util.List;

import edu.uvg.gestionpedidos.dto.ResumenPagoDTO;
import edu.uvg.gestionpedidos.dto.AbonoHistorialDTO;
import edu.uvg.gestionpedidos.entity.PlanPago;
import edu.uvg.gestionpedidos.entity.PartePago;
import edu.uvg.gestionpedidos.repository.PlanPagoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.NoSuchElementException;
import edu.uvg.gestionpedidos.entity.Abono;
import edu.uvg.gestionpedidos.entity.EstadoPago;
import edu.uvg.gestionpedidos.entity.MetodoPago;
import edu.uvg.gestionpedidos.entity.Pago;
import edu.uvg.gestionpedidos.entity.Pedido;
import edu.uvg.gestionpedidos.repository.AbonoRepository;
import edu.uvg.gestionpedidos.repository.PagoRepository;
import edu.uvg.gestionpedidos.repository.PedidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final AbonoRepository abonoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final EmprendimientoService emprendimientoService;

    public PagoService(PagoRepository pagoRepository, PedidoRepository pedidoRepository, AbonoRepository abonoRepository,
        PlanPagoRepository planPagoRepository, EmprendimientoService emprendimientoService) {

        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.abonoRepository = abonoRepository;
        this.planPagoRepository = planPagoRepository;
        this.emprendimientoService = emprendimientoService;
    }

    
    @Transactional
    public Pago obtenerOCrearPago(Long pedidoId) {

        Pedido pedido = bloquearPedido(pedidoId);

        verificarPropietario(
                pedido,
                obtenerCorreoAutenticado()
        );

        return obtenerOCrearPagoBloqueado(pedido);
    }


    public Pago registrarAbono(Long pedidoId, BigDecimal monto) {
        throw new IllegalArgumentException("tienes escoger un método de pago.");
    }

    
    @Transactional
    public Pago registrarAbono(
            Long pedidoId,
            BigDecimal monto,
            MetodoPago metodoPago,
            String referencia) {

        Pedido pedido = bloquearPedido(pedidoId);

        verificarPropietario(
                pedido,
                obtenerCorreoAutenticado()
        );

        return aplicarAbonoBloqueado(
                pedido,
                monto,
                metodoPago,
                referencia,
                null
        );
    }


    @Transactional
    public Pago registrarAbono(Long pedidoId, BigDecimal monto, MetodoPago metodoPago,
            String referencia, Long parteId, String correo) {
        // El bloqueo y la comprobación de acceso están en la misma transacción.
        Pedido pedido = bloquearPedido(pedidoId);
        verificarPropietario(pedido, correo);
        return aplicarAbonoBloqueado(pedido, monto, metodoPago, referencia, parteId);
    }

    private Pago aplicarAbono(Long pedidoId, BigDecimal monto,
            MetodoPago metodoPago, String referencia, Long parteId) {
        return aplicarAbonoBloqueado(bloquearPedido(pedidoId), monto, metodoPago, referencia, parteId);
    }

    private Pago aplicarAbonoBloqueado(Pedido pedido, BigDecimal monto,
            MetodoPago metodoPago, String referencia, Long parteId) {
        Pago pago = obtenerOCrearPagoBloqueado(pedido);
        PlanPago plan = planPagoRepository.findByPago_Id(pago.getId()).orElse(null);
        PartePago parte = null;
        if (plan != null) {
            parte = plan.buscarParte(parteId);
        } else if (parteId != null) {
            throw new IllegalArgumentException("Este pedido no tiene un plan de pagos.");
        }
        Abono abono = new Abono(pago, monto, metodoPago, referencia, parte);
        if (parte != null) { parte.validarAbono(abono.getMonto()); }
        pago.registrarAbono(abono.getMonto());
        if (parte != null) { parte.registrarAbono(abono.getMonto()); }
        abonoRepository.save(abono);
        return pagoRepository.save(pago);
    }

    @Transactional(readOnly = true)
    public void verificarAcceso(Long pedidoId, String correo) {
        validarPedidoId(pedidoId);
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(
                () -> new IllegalArgumentException("El pedido no existe."));
        verificarPropietario(pedido, correo);
    }

    private void verificarPropietario(Pedido pedido, String correo) {
        if (pedido.getEmprendimiento() == null) {
            throw new AccessDeniedException("El pedido no está disponible para tu cuenta.");
        }
        try {
            emprendimientoService.consultarEmprendimiento(pedido.getEmprendimiento().getId(), correo);
        } catch (NoSuchElementException e) {
            throw new AccessDeniedException("El pedido no está disponible para tu cuenta.");
        }
    }

    
    @Transactional(readOnly = true)
    public List<AbonoHistorialDTO> consultarHistorial(Long pedidoId) {

        verificarAcceso(
                pedidoId,
                obtenerCorreoAutenticado()
        );

        return abonoRepository
                .findByPago_Pedido_IdOrderByFechaDescIdDesc(pedidoId)
                .stream()
                .map(AbonoHistorialDTO::new)
                .toList();
    }


    @Transactional(readOnly = true)
    public BigDecimal consultarMontoSinDetalle(Long pedidoId) {
        ResumenPagoDTO resumen = consultarResumen(pedidoId);

        BigDecimal totalConDetalle =
                abonoRepository.sumarMontosPorPedido(pedidoId);

        BigDecimal montoSinDetalle = resumen.getTotalAbonado()
                .subtract(totalConDetalle);

        if (montoSinDetalle.signum() < 0) {
            throw new IllegalStateException("El historial de abonos no coincide " + "con el total abonado del pedido.");
        }

        return montoSinDetalle;
    }

    @Transactional(readOnly = true)
    public ResumenPagoDTO consultarResumen(Long pedidoId) {
        validarPedidoId(pedidoId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("El pedido es inexistente."));
        
        verificarPropietario(pedido,obtenerCorreoAutenticado());

        BigDecimal totalPedido = pedido.calcularTotal();

        Pago pago = pagoRepository.findByPedidoId(pedidoId)
                .orElse(null);

        BigDecimal totalAbonado = BigDecimal.ZERO;
        EstadoPago estado = EstadoPago.PENDIENTE;

        if (pago != null) {
            totalAbonado = pago.getTotalAbonado();
            estado = pago.getEstado();
        }

        BigDecimal saldoPendiente =
                totalPedido.subtract(totalAbonado);

        return new ResumenPagoDTO(pedidoId, totalPedido, totalAbonado, saldoPendiente, estado);
    }

    private Pedido bloquearPedido(Long pedidoId) {
        validarPedidoId(pedidoId);

        return pedidoRepository.buscarParaPago(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("El pedido es inexistente."));
    }

    private Pago obtenerOCrearPagoBloqueado(Pedido pedido) {
        return pagoRepository
                .findWithLockByPedidoId(pedido.getId())
                .orElseGet(() -> {
                    Pago pago = new Pago(pedido);

                    return pagoRepository.saveAndFlush(pago);
                });
    }

    private void validarPedidoId(Long pedidoId) {
        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException("El identificador del pedido no es válido.");
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

}