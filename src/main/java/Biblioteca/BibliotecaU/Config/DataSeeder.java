package Biblioteca.BibliotecaU.Config;

import Biblioteca.BibliotecaU.Entity.Rol;
import Biblioteca.BibliotecaU.Entity.Usuario;
import Biblioteca.BibliotecaU.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String email = "admin@biblioteca.com";

        if (!usuarioRepository.existsByEmail(email)) {

            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .email(email)
                    .password(passwordEncoder.encode("Admin123*"))
                    .rol(Rol.ADMIN)
                    .build();

            usuarioRepository.save(admin);

            System.out.println("ADMIN creado correctamente: " + email);

        } else {
            System.out.println("ADMIN ya existe: " + email);
        }
    }
}

