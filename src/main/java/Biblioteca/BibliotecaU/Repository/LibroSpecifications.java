package Biblioteca.BibliotecaU.Repository;

import Biblioteca.BibliotecaU.Entity.Libro;
import org.springframework.data.jpa.domain.Specification;

public final class LibroSpecifications {

    private LibroSpecifications() {}

    public static Specification<Libro> tituloContiene(String titulo) {
        return (root, query, cb) -> titulo == null || titulo.isBlank()
                ? null
                : cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%");
    }

    public static Specification<Libro> autorContiene(String autor) {
        return (root, query, cb) -> autor == null || autor.isBlank()
                ? null
                : cb.like(cb.lower(root.get("autor")), "%" + autor.toLowerCase() + "%");
    }

    public static Specification<Libro> categoriaIgual(String categoria) {
        return (root, query, cb) -> categoria == null || categoria.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("categoria")), categoria.toLowerCase());
    }

    public static Specification<Libro> soloDisponibles(Boolean disponibles) {
        return (root, query, cb) -> Boolean.TRUE.equals(disponibles)
                ? cb.greaterThan(root.get("stockDisponible"), 0)
                : null;
    }
}