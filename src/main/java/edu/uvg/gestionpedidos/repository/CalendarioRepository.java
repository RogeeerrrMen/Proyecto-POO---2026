package edu.uvg.gestionpedidos.repository;

import java.time.LocalDate;
import java.util.List;
import edu.uvg.gestionpedidos.entity.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CalendarioRepository extends Repository<Pedido, Long> {

    String CAMPOS = "SELECT p.id AS id, p.codigo AS codigo, "
            + "p.cliente.nombre AS nombreCliente, p.estado AS estado, "
            + "p.modalidadEntrega AS modalidadEntrega, "
            + "p.fechaPreparacion AS fechaPreparacion, p.horaPreparacion AS horaPreparacion, "
            + "p.fechaEnvio AS fechaEnvio, p.horaEnvio AS horaEnvio, "
            + "p.fechaEntrega AS fechaEntrega, p.horaEntrega AS horaEntrega "
            + "FROM Pedido p WHERE p.emprendimiento.id = :emprendimientoId ";

    @Query(CAMPOS + "AND (p.fechaPreparacion BETWEEN :desde AND :hasta "
            + "OR p.fechaEnvio BETWEEN :desde AND :hasta "
            + "OR p.fechaEntrega BETWEEN :desde AND :hasta) ORDER BY p.id")
    List<PedidoCalendarioVista> buscarProgramados(
            @Param("emprendimientoId") Long emprendimientoId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query(CAMPOS + "AND p.fechaPreparacion IS NULL "
            + "AND p.fechaEnvio IS NULL AND p.fechaEntrega IS NULL ORDER BY p.id DESC")
    List<PedidoCalendarioVista> buscarSinFechas(
            @Param("emprendimientoId") Long emprendimientoId);
}
