package edu.uvg.gestionpedidos.repository;

import edu.uvg.gestionpedidos.entity.Emprendimiento;
import edu.uvg.gestionpedidos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmprendimientoRepository
        extends JpaRepository<Emprendimiento, Long> {

    //Busca el emprendimiento por el ususario propietario. Esto es útil para comprobar si un usuario ya tiene un emprendimiento.
    Optional<Emprendimiento> findByPropietario(Usuario propietario);

    //Busca un emprendimiento comprobando también su propietario
    Optional<Emprendimiento> findByIdAndPropietario(
            Long id,
            Usuario propietario
    );

    //Comprueba si el usuario tiene un emprendimiento.
    boolean existsByPropietario(Usuario propietario);
}