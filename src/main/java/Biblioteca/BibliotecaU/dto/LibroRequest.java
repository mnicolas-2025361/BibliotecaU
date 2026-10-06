package Biblioteca.BibliotecaU.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LibroRequest(
        @NotBlank @Size(max = 20) String isbn,
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank @Size(max = 150) String autor,
        @Size(max = 80) String categoria,
        @Size(max = 120) String editorial,
        @NotNull @Min(0) Integer stockTotal
) {}