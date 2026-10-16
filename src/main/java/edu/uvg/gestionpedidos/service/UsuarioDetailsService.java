package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo)

        throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim()).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getCorreo()).password(usuario.getPasswordHash()).roles("USER").build();

    }
}
