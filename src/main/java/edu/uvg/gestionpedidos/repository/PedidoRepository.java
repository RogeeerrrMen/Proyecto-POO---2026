package edu.uvg.gestionpedidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import edu.uvg.gestionpedidos.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {
            "cliente",
            "direccionEntrega",
            "detalles.producto"
    })
    Optional<Pedido> findByCodigo(String codigo);

    @Override
    @EntityGraph(attributePaths = {
            "cliente",
            "direccionEntrega",
            "detalles.producto" 
    })
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = {
            "cliente",
            "direccionEntrega",
            "detalles.producto"
    })
    List<Pedido> findAllByOrderByFechaPedidoDescIdDesc();

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT p FROM Pedido p WHERE p.id = :pedidoId")
        Optional<Pedido> buscarParaPago(@Param("pedidoId") Long pedidoId);



        @EntityGraph(attributePaths = {
                "cliente",
                "direccionEntrega",
                "detalles.producto"
        })
        List<Pedido> findByEmprendimientoIdOrderByFechaPedidoDescIdDesc(
                Long emprendimientoId);

        @EntityGraph(attributePaths = {
                "cliente",
                "direccionEntrega",
                "detalles.producto"
        })
        Optional<Pedido> findByIdAndEmprendimientoId(
                Long id,
                Long emprendimientoId);


}