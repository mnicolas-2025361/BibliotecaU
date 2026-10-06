package Biblioteca.BibliotecaU.dto;

import jakarta.validation.constraints.NotNull;

/**
 * usuarioId es opcional: un LECTOR siempre pide para sí mismo (se toma del token),
 * BIBLIOTECARIO y ADMIN deben indicarlo.
 */
public record PrestamoRequest(
        @NotNull Long libroId,
        Long usuarioId
) {}