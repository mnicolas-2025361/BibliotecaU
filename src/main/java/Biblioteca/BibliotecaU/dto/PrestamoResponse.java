package Biblioteca.BibliotecaU.dto;

import Biblioteca.BibliotecaU.Entity.EstadoPrestamo;
import Biblioteca.BibliotecaU.Entity.Prestamo;

import java.time.LocalDateTime;

public record PrestamoResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        Long libroId,
        String libroTitulo,
        LocalDateTime fechaPrestamo,
        LocalDateTime fechaVencimiento,
        LocalDateTime fechaDevolucion,
        EstadoPrestamo estado,
        boolean atrasado
) {
    /** Debe llamarse dentro de una transacción (usuario y libro son LAZY). */
    public static PrestamoResponse from(Prestamo p) {
        return new PrestamoResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNombre(),
                p.getLibro().getId(),
                p.getLibro().getTitulo(),
                p.getFechaPrestamo(),
                p.getFechaVencimiento(),
                p.getFechaDevolucion(),
                p.getEstado(),
                p.estaAtrasado());
    }
}