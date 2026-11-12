package edu.uvg.gestionpedidos.repository;

import edu.uvg.gestionpedidos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Consultas originales
    List<Producto> findAllByOrderByIdAsc();

    List<Producto> findByActivoTrueOrderByIdAsc();

    List<Producto> findByNombre(String nombre);

    List<Producto> findByNombreContainingIgnoreCaseOrderByIdAsc(
            String texto);

    // Consultas filtradas por emprendimiento
    List<Producto> findByEmprendimientoIdOrderByIdAsc(
            Long emprendimientoId);

    List<Producto> findByEmprendimientoIdAndActivoTrueOrderByIdAsc(
            Long emprendimientoId);

    Optional<Producto> findByIdAndEmprendimientoId(
            Long id,
            Long emprendimientoId);

    List<Producto> findByEmprendimientoIdAndNombreContainingIgnoreCaseOrderByIdAsc(
            Long emprendimientoId,
            String nombre);
            
}
