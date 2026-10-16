package edu.uvg.gestionpedidos.repository;

import edu.uvg.gestionpedidos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /** Todos los productos (activos e inactivos), del más antiguo al más reciente. */
    List<Producto> findAllByOrderByIdAsc();

    /** Solo los productos disponibles para vender. */
    List<Producto> findByActivoTrueOrderByIdAsc();

    /** Búsqueda por nombre exacto (definida en el diseño de la segunda entrega). */
    List<Producto> findByNombre(String nombre);

    /** Búsqueda por coincidencia parcial, sin distinguir mayúsculas de minúsculas. */
    List<Producto> findByNombreContainingIgnoreCaseOrderByIdAsc(String texto);
}
