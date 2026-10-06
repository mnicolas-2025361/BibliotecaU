package Biblioteca.BibliotecaU.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato único de error para toda la API. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> errores
) {}