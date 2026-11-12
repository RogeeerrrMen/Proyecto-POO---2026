package edu.uvg.gestionpedidos.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import edu.uvg.gestionpedidos.dto.PlanPagoDTO;
import edu.uvg.gestionpedidos.entity.Pago;
import edu.uvg.gestionpedidos.entity.PlanPago;
import edu.uvg.gestionpedidos.repository.PlanPagoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanPagoService {
    private final PagoService pagoService;
    private final PlanPagoRepository planPagoRepository;
    private final Clock clock;

    public PlanPagoService(PagoService pagoService, PlanPagoRepository planPagoRepository,
            @Qualifier("calendarioClock") Clock clock) {
        this.pagoService = pagoService;
        this.planPagoRepository = planPagoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PlanPagoDTO consultar(Long pedidoId, String correo) {
        pagoService.verificarAcceso(pedidoId, correo);
        return planPagoRepository.findByPago_Pedido_Id(pedidoId)
                .map(plan -> new PlanPagoDTO(plan, LocalDate.now(clock))).orElse(null);
    }

    @Transactional
    public void crearCuotas(Long pedidoId, Integer cantidad, LocalDate primerVencimiento, String correo) {
        Pago pago = prepararPago(pedidoId, correo);
        PlanPago plan = PlanPago.crearCuotas(pago, cantidad, primerVencimiento);
        planPagoRepository.save(plan);
    }

    @Transactional
    public void crearDividido(Long pedidoId, List<String> personas, List<BigDecimal> montos, String correo) {
        Pago pago = prepararPago(pedidoId, correo);
        PlanPago plan = PlanPago.crearDividido(pago, personas, montos);
        planPagoRepository.save(plan);
    }

    
    private Pago prepararPago(Long pedidoId, String correo) {

        // Verificar primero que el pedido pertenece al usuario.
        pagoService.verificarAcceso(pedidoId, correo);

        // Obtener o crear el pago después de autorizar.
        Pago pago = pagoService.obtenerOCrearPago(pedidoId);

        if (planPagoRepository.findByPago_Id(pago.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "El pedido ya tiene un plan de pagos.");
        }

        if (pago.getSaldoPendiente().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El pedido ya no tiene saldo pendiente.");
        }

        return pago;
    }

}
