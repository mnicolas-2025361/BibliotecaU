package Biblioteca.BibliotecaU.dto;

import Biblioteca.BibliotecaU.Entity.Rol;

public record AuthResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        Long usuarioId,
        String nombre,
        String email,
        Rol rol
) {}