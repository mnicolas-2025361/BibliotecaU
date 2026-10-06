package Biblioteca.BibliotecaU.Service;

import Biblioteca.BibliotecaU.dto.PrestamoResponse;
import Biblioteca.BibliotecaU.Entity.EstadoPrestamo;
import Biblioteca.BibliotecaU.Entity.EstadoUsuario;
import Biblioteca.BibliotecaU.Entity.Libro;
import Biblioteca.BibliotecaU.Entity.Prestamo;
import Biblioteca.BibliotecaU.Entity.Usuario;
import Biblioteca.BibliotecaU.Exceptions.LimitePrestamosException;
import Biblioteca.BibliotecaU.Exceptions.RecursoNoEncontradoException;
import Biblioteca.BibliotecaU.Exceptions.ReglaNegocioException;
import Biblioteca.BibliotecaU.Exceptions.StockAgotadoException;
import Biblioteca.BibliotecaU.Exceptions.UsuarioSancionadoException;
import Biblioteca.BibliotecaU.Repository.LibroRepository;
import Biblioteca.BibliotecaU.Repository.PrestamoRepository;
import Biblioteca.BibliotecaU.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    public static final int MAX_PRESTAMOS_ACTIVOS = 3;

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    /**
     * Orden de bloqueos (siempre el mismo, evita deadlocks): usuario -> libro.
     * noRollbackFor: la sanción debe quedar guardada aunque el préstamo se rechace.
     */
    @Transactional(noRollbackFor = UsuarioSancionadoException.class, timeout = 10)
    public PrestamoResponse registrarPrestamo(Long usuarioId, Long libroId) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1) Bloquea al lector: serializa sus propias peticiones simultáneas
        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));

        // 2) Sanción automática por préstamos vencidos sin devolver
        boolean tieneVencidos = prestamoRepository
                .existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(usuarioId, EstadoPrestamo.ACTIVO, ahora);
        if (tieneVencidos) {
            usuario.setEstado(EstadoUsuario.SANCIONADO);
            throw new UsuarioSancionadoException(
                    "Tienes préstamos vencidos sin devolver. Quedas sancionado hasta devolverlos.");
        }
        if (usuario.estaSancionado()) {
            usuario.setEstado(EstadoUsuario.ACTIVO); // ya devolvió lo atrasado
        }

        // 3) Límite de préstamos activos
        long activos = prestamoRepository.countByUsuarioIdAndEstado(usuarioId, EstadoPrestamo.ACTIVO);
        if (activos >= MAX_PRESTAMOS_ACTIVOS) {
            throw new LimitePrestamosException(
                    "Ya tienes " + activos + " préstamos activos (máximo " + MAX_PRESTAMOS_ACTIVOS + ")");
        }

        // 4) Descuento atómico de stock: una sola sentencia, sin carrera posible
        if (libroRepository.descontarStock(libroId) == 0) {
            if (!libroRepository.existsById(libroId)) {
                throw new RecursoNoEncontradoException("Libro no encontrado: " + libroId);
            }
            throw new StockAgotadoException("No hay ejemplares disponibles de este libro");
        }

        // 5) Registrar el préstamo (descontarStock limpió el contexto, por eso se vuelve a cargar)
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + libroId));
        Prestamo prestamo = Prestamo.builder()
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .libro(libro)
                .fechaPrestamo(ahora)
                .fechaVencimiento(ahora.plusDays(Prestamo.DIAS_PRESTAMO))
                .build();

        return PrestamoResponse.from(prestamoRepository.save(prestamo));
    }

    @Transactional(timeout = 10)
    public PrestamoResponse devolver(Long prestamoId) {
        // Bloquea el préstamo: dos devoluciones simultáneas no suman el stock dos veces
        Prestamo prestamo = prestamoRepository.findByIdForUpdate(prestamoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado: " + prestamoId));

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ReglaNegocioException("Este préstamo ya fue devuelto");
        }

        prestamo.marcarDevuelto();
        // Se arma la respuesta antes del UPDATE de stock, porque este limpia el contexto
        PrestamoResponse respuesta = PrestamoResponse.from(prestamo);

        if (libroRepository.incrementarStock(prestamo.getLibro().getId()) == 0) {
            // Revierte todo: el stock ya estaba completo, hay una inconsistencia que investigar
            throw new IllegalStateException("Inconsistencia de stock al devolver el préstamo " + prestamoId);
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public Page<PrestamoResponse> historialDelLector(Long usuarioId, Pageable pageable) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId);
        }
        return prestamoRepository
                .findByUsuarioIdOrderByFechaPrestamoDesc(usuarioId, pageable)
                .map(PrestamoResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PrestamoResponse> atrasados(Pageable pageable) {
        return prestamoRepository
                .findAtrasados(EstadoPrestamo.ACTIVO, LocalDateTime.now(), pageable)
                .map(PrestamoResponse::from);
    }
}