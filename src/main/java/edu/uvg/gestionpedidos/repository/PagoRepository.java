package edu.uvg.gestionpedidos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import edu.uvg.gestionpedidos.entity.Pago;
import jakarta.persistence.LockModeType;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pago> findWithLockByPedidoId(Long pedidoId);
    
}
