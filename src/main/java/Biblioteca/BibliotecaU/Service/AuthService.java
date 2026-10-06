package Biblioteca.BibliotecaU.Service;

import Biblioteca.BibliotecaU.dto.AuthResponse;
import Biblioteca.BibliotecaU.dto.LoginRequest;
import Biblioteca.BibliotecaU.dto.RegistroRequest;
import Biblioteca.BibliotecaU.Entity.Rol;
import Biblioteca.BibliotecaU.Entity.Usuario;
import Biblioteca.BibliotecaU.Exceptions.RecursoDuplicadoException;
import Biblioteca.BibliotecaU.Repository.UsuarioRepository;
import Biblioteca.BibliotecaU.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** El registro público solo crea LECTORES. */
    public AuthResponse registrar(RegistroRequest r) {
        String email = normalizar(r.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con el email " + email);
        }
        // Se calcula el hash antes de tocar la base de datos
        String hash = passwordEncoder.encode(r.password());

        // Si dos registros iguales llegan a la vez, el índice único responde con 409
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre(r.nombre().trim())
                .email(email)
                .password(hash)
                .rol(Rol.LECTOR)
                .build());
        return construirRespuesta(usuario);
    }

    public AuthResponse login(LoginRequest r) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(r.email()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(r.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return construirRespuesta(usuario);
    }

    private AuthResponse construirRespuesta(Usuario u) {
        return new AuthResponse(
                jwtService.generarToken(u), "Bearer", jwtService.getExpiracionSegundos(),
                u.getId(), u.getNombre(), u.getEmail(), u.getRol());
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}