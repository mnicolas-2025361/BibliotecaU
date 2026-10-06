package Biblioteca.BibliotecaU.Controller;

import Biblioteca.BibliotecaU.dto.LibroRequest;
import Biblioteca.BibliotecaU.dto.LibroResponse;
import Biblioteca.BibliotecaU.dto.PageResponse;
import Biblioteca.BibliotecaU.Service.LibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public PageResponse<LibroResponse> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean soloDisponibles,
            @PageableDefault(size = 20, sort = "titulo") Pageable pageable) {
        return PageResponse.from(
                libroService.listar(titulo, autor, categoria, soloDisponibles, pageable));
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable Long id) {
        return libroService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public LibroResponse crear(@Valid @RequestBody LibroRequest request) {
        return libroService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LibroResponse actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequest request) {
        return libroService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
    }
}