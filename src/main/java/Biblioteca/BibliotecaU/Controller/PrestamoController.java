package Biblioteca.BibliotecaU.Controller;

import Biblioteca.BibliotecaU.dto.PageResponse;
import Biblioteca.BibliotecaU.dto.PrestamoRequest;
import Biblioteca.BibliotecaU.dto.PrestamoResponse;
import Biblioteca.BibliotecaU.Service.PrestamoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    /** LECTOR: pide para sí mismo. BIBLIOTECARIO/ADMIN: deben indicar usuarioId. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestamoResponse registrar(@Valid @RequestBody PrestamoRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId;
        if (esPersonal(jwt)) {
            if (request.usuarioId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Debes indicar el usuarioId del lector");
            }
            usuarioId = request.usuarioId();
        } else {
            usuarioId = idDelToken(jwt);
            if (request.usuarioId() != null && !request.usuarioId().equals(usuarioId)) {
                throw new AccessDeniedException("Un lector solo puede pedir préstamos para sí mismo");
            }
        }
        return prestamoService.registrarPrestamo(usuarioId, request.libroId());
    }

    @PutMapping("/{id}/devolucion")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    public PrestamoResponse devolver(@PathVariable Long id) {
        return prestamoService.devolver(id);
    }

    /** Historial del propio lector autenticado. */
    @GetMapping("/mis-prestamos")
    public PageResponse<PrestamoResponse> misPrestamos(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(prestamoService.historialDelLector(idDelToken(jwt), pageable));
    }

    /** Historial de cualquier lector (personal de la biblioteca). */
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    public PageResponse<PrestamoResponse> historialDeUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(prestamoService.historialDelLector(usuarioId, pageable));
    }

    @GetMapping("/atrasados")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    public PageResponse<PrestamoResponse> atrasados(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(prestamoService.atrasados(pageable));
    }

    private boolean esPersonal(Jwt jwt) {
        String rol = jwt.getClaimAsString("rol");
        return "ADMIN".equals(rol) || "BIBLIOTECARIO".equals(rol);
    }

    private Long idDelToken(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}