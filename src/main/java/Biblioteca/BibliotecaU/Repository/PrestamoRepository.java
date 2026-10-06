package Biblioteca.BibliotecaU.Repository;

import Biblioteca.BibliotecaU.Entity.EstadoPrestamo;
import Biblioteca.BibliotecaU.Entity.Prestamo;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /** Límite de 3 préstamos activos (usa idx_prestamo_usuario_estado). */
    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    /** Sanción: ¿tiene préstamos activos ya vencidos? */
    boolean existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(
            Long usuarioId, EstadoPrestamo estado, LocalDateTime ahora);

    /** Historial del lector con el libro cargado en el mismo query (sin N+1). */
    @EntityGraph(attributePaths = "libro")
    Page<Prestamo> findByUsuarioIdOrderByFechaPrestamoDesc(Long usuarioId, Pageable pageable);

    /** Préstamos atrasados (usa idx_prestamo_estado_vencimiento). */
    @EntityGraph(attributePaths = {"libro", "usuario"})
    @Query("""
        SELECT p FROM Prestamo p
         WHERE p.estado = :estado AND p.fechaVencimiento < :ahora
         ORDER BY p.fechaVencimiento ASC
        """)
    Page<Prestamo> findAtrasados(@Param("estado") EstadoPrestamo estado,
                                 @Param("ahora") LocalDateTime ahora,
                                 Pageable pageable);

    /**
     * Bloquea el préstamo al devolver, para que dos devoluciones simultáneas
     * no sumen el stock dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT p FROM Prestamo p WHERE p.id = :id")
    Optional<Prestamo> findByIdForUpdate(@Param("id") Long id);

    boolean existsByLibroId(Long libroId);
}