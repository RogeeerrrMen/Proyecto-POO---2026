package edu.uvg.gestionpedidos.repository;

import edu.uvg.gestionpedidos.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    // Métodos originales
    List<Gasto> findAllByOrderByFechaDescIdDesc();

    List<Gasto> findByFechaBetweenOrderByFechaDescIdDesc(
            LocalDate inicio,
            LocalDate fin);

    @Query("""
            SELECT SUM(g.monto)
            FROM Gasto g
            WHERE g.fecha BETWEEN :inicio AND :fin
            """)
    BigDecimal sumarMontosPorPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    // Métodos filtrados por emprendimiento
    List<Gasto> findByEmprendimientoIdOrderByFechaDescIdDesc(
            Long emprendimientoId);

    Optional<Gasto> findByIdAndEmprendimientoId(
            Long id,
            Long emprendimientoId);

    List<Gasto> findByEmprendimientoIdAndFechaBetweenOrderByFechaDescIdDesc(
            Long emprendimientoId,
            LocalDate inicio,
            LocalDate fin);

    @Query("""
            SELECT SUM(g.monto)
            FROM Gasto g
            WHERE g.emprendimiento.id = :emprendimientoId
              AND g.fecha BETWEEN :inicio AND :fin
            """)
    BigDecimal sumarMontosPorEmprendimientoYPeriodo(
            @Param("emprendimientoId") Long emprendimientoId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);
}
