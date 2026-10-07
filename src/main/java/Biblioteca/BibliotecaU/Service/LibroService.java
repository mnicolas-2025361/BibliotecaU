package Biblioteca.BibliotecaU.Service;

import Biblioteca.BibliotecaU.dto.LibroRequest;
import Biblioteca.BibliotecaU.dto.LibroResponse;
import Biblioteca.BibliotecaU.Entity.Libro;
import Biblioteca.BibliotecaU.Exceptions.RecursoDuplicadoException;
import Biblioteca.BibliotecaU.Exceptions.RecursoNoEncontradoException;
import Biblioteca.BibliotecaU.Exceptions.ReglaNegocioException;
import Biblioteca.BibliotecaU.Repository.LibroRepository;
import Biblioteca.BibliotecaU.Repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static Biblioteca.BibliotecaU.Repository.LibroSpecifications.*;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    @Transactional(readOnly = true)
    public Page<LibroResponse> listar(String titulo, String autor, String categoria,
                                      Boolean soloDisponibles, Pageable pageable) {
        Specification<Libro> spec = Specification.allOf(
                tituloContiene(titulo),
                autorContiene(autor),
                categoriaIgual(categoria),
                soloDisponibles(soloDisponibles));
        return libroRepository.findAll(spec, pageable).map(LibroResponse::from);
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return LibroResponse.from(buscar(id));
    }

    @Transactional
    public LibroResponse crear(LibroRequest r) {
        String isbn = r.isbn().trim();
        if (r.stockDisponible() > r.stockTotal()) {
            throw new ReglaNegocioException(
                    "El stock disponible no puede ser mayor que el stock total");
        }

        Libro libro = Libro.builder()
                .isbn(isbn)
                .titulo(r.titulo().trim())
                .autor(r.autor().trim())
                .categoria(r.categoria())
                .editorial(r.editorial())
                .stockTotal(r.stockTotal())
                .stockDisponible(r.stockDisponible())
                .build();

        return LibroResponse.from(libroRepository.save(libro));
    }

    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest r) {
        Libro libro = buscar(id);

        String isbn = r.isbn().trim();
        if (!libro.getIsbn().equals(isbn) && libroRepository.existsByIsbn(isbn)) {
            throw new RecursoDuplicadoException("Ya existe un libro con el ISBN " + isbn);
        }

        // El cambio de stock total se refleja en el disponible, sin tocar lo ya prestado
        int prestados = libro.getStockTotal() - libro.getStockDisponible();
        int nuevoDisponible = r.stockTotal() - prestados;

        if (nuevoDisponible < 0) {
            throw new ReglaNegocioException("No puedes dejar el stock total en " + r.stockTotal()
                    + ": hay " + prestados + " ejemplares prestados");
        }

        libro.setIsbn(isbn);
        libro.setTitulo(r.titulo().trim());
        libro.setAutor(r.autor().trim());
        libro.setCategoria(r.categoria());
        libro.setEditorial(r.editorial());
        libro.setStockTotal(r.stockTotal());
        libro.setStockDisponible(nuevoDisponible);

        // @Version detecta si otro proceso modificó el libro al mismo tiempo
        return LibroResponse.from(libro);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro libro = buscar(id);

        if (prestamoRepository.existsByLibroId(id)) {
            throw new ReglaNegocioException("No se puede eliminar un libro con historial de préstamos");
        }

        libroRepository.delete(libro);
    }

    private Libro buscar(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + id));
    }
}