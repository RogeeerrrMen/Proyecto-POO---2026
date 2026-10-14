package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.RegistroUsuarioDTO;
import edu.uvg.gestionpedidos.entity.Usuario;
import edu.uvg.gestionpedidos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;

    }


    public void registrarUsuario(RegistroUsuarioDTO datos) {

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setCorreo(datos.getCorreo());

        usuario.setPasswordHash(datos.getPassword());

        usuarioRepository.save(usuario);

    }

}