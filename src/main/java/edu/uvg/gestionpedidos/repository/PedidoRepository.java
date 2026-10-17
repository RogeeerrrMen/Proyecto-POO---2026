package edu.uvg.gestionpedidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}