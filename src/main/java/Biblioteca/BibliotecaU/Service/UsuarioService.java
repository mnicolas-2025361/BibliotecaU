package Biblioteca.BibliotecaU.Service;

import Biblioteca.BibliotecaU.Entity.Usuario;
import Biblioteca.BibliotecaU.Exceptions.RecursoNoEncontradoException;
import Biblioteca.BibliotecaU.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /** Resuelve el id del usuario autenticado a partir del username del token (su email). */
    @Transactional(readOnly = true)
    public Long idPorEmail(String email) {
        return usuarioRepository.findByEmail(email.toLowerCase())
                .map(Usuario::getId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + email));
    }
}