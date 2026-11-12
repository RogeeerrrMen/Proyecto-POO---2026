package edu.uvg.gestionpedidos.repository;

import java.math.BigDecimal;
import java.util.List;

import edu.uvg.gestionpedidos.entity.Abono;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AbonoRepository extends JpaRepository<Abono, Long> {

    List<Abono> findByPago_Pedido_IdOrderByFechaDescIdDesc(Long pedidoId);

    @Query("SELECT COALESCE(SUM(a.monto), 0) " + "FROM Abono a " + "WHERE a.pago.pedido.id = :pedidoId")
    BigDecimal sumarMontosPorPedido(
            @Param("pedidoId") Long pedidoId);
}