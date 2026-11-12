package edu.uvg.gestionpedidos.repository;

import edu.uvg.gestionpedidos.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNombreContainingIgnoreCaseOrTelefonoContaining(
            String nombre, String telefono);

    List<Cliente> findByEmprendimientoId(Long emprendimientoId);

    Optional<Cliente> findByIdAndEmprendimientoId(
            Long id,
            Long emprendimientoId);

    List<Cliente>
    findByEmprendimientoIdAndNombreContainingIgnoreCaseOrEmprendimientoIdAndTelefonoContaining(
            Long emprendimientoIdNombre,
            String nombre,
            Long emprendimientoIdTelefono,
            String telefono);
}
