package edu.uvg.gestionpedidos.repository;
import edu.uvg.gestionpedidos.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    //Crea la lista de los gastos desde la fecha más reciente.
    //Si tienen la misma fecha, ordena por identificador descendente.
    List<Gasto> findAllByOrderByFechaDescIdDesc();

    //Consulta los gastos dentro de un período, incluyendo ambas fechas.
    List<Gasto> findByFechaBetweenOrderByFechaDescIdDesc(
            LocalDate inicio,
            LocalDate fin
    );

    //Realiza el cálculo del total de los gastos dentro del período.
    @Query("""
            SELECT SUM(g.monto)
            FROM Gasto g
            WHERE g.fecha BETWEEN :inicio AND :fin
            """)
    BigDecimal sumarMontosPorPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );
}