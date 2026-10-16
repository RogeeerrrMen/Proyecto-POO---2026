package edu.uvg.gestionpedidos.repository;
import java.util.Optional;

import edu.uvg.gestionpedidos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    boolean existsByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    
}