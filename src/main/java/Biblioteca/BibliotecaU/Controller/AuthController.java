package Biblioteca.BibliotecaU.Controller;

import Biblioteca.BibliotecaU.dto.AuthResponse;
import Biblioteca.BibliotecaU.dto.LoginRequest;
import Biblioteca.BibliotecaU.dto.RegistroRequest;
import Biblioteca.BibliotecaU.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}