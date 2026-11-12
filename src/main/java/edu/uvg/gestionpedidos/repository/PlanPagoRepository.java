package edu.uvg.gestionpedidos.repository;

import java.util.Optional;
import edu.uvg.gestionpedidos.entity.PlanPago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {
    Optional<PlanPago> findByPago_Id(Long pagoId);
    Optional<PlanPago> findByPago_Pedido_Id(Long pedidoId);
}
