package Biblioteca.BibliotecaU.Config;

import Biblioteca.BibliotecaU.Entity.Rol;
import Biblioteca.BibliotecaU.Entity.Usuario;
import Biblioteca.BibliotecaU.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean habilitado;

    @Value("${app.seed.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (!habilitado || password.isBlank()) {
            return;
        }
        crearSiNoExiste("Administrador", "admin@biblioteca.com", Rol.ADMIN);
        crearSiNoExiste("Bibliotecario", "bibliotecario@biblioteca.com", Rol.BIBLIOTECARIO);
    }

    private void crearSiNoExiste(String nombre, String email, Rol rol) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .email(email)
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .build());
        log.info("Usuario inicial creado: {} ({})", email, rol);
    }
}