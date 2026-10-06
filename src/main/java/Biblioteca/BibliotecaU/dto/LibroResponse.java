package Biblioteca.BibliotecaU.dto;

import Biblioteca.BibliotecaU.Entity.Libro;

public record LibroResponse(
        Long id,
        String isbn,
        String titulo,
        String autor,
        String categoria,
        String editorial,
        int stockTotal,
        int stockDisponible
) {
    public static LibroResponse from(Libro l) {
        return new LibroResponse(
                l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(),
                l.getCategoria(), l.getEditorial(),
                l.getStockTotal(), l.getStockDisponible());
    }
}