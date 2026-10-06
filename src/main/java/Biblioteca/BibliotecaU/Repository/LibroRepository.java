package Biblioteca.BibliotecaU.Repository;

import Biblioteca.BibliotecaU.entity.Libro;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long>, JpaSpecificationExecutor<Libro> {

    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    /**
     * Opción A (pesimista): bloquea el libro. Útil si necesitas leer y decidir con lógica compleja.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT l FROM Libro l WHERE l.id = :id")
    Optional<Libro> findByIdForUpdate(@Param("id") Long id);

    /**
     * Opción B (RECOMENDADA para el préstamo): descuento atómico en una sola sentencia.
     * Devuelve 1 si había stock y se descontó, 0 si no había (stock agotado o libro inexistente).
     * Como @Modifying con JPQL no pasa por Hibernate, incrementamos la versión a mano.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Libro l
           SET l.stockDisponible = l.stockDisponible - 1,
               l.version = l.version + 1
         WHERE l.id = :id AND l.stockDisponible > 0
        """)
    int descontarStock(@Param("id") Long id);

    /**
     * Devolución: suma 1 sin pasarse del stock total.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Libro l
           SET l.stockDisponible = l.stockDisponible + 1,
               l.version = l.version + 1
         WHERE l.id = :id AND l.stockDisponible < l.stockTotal
        """)
    int incrementarStock(@Param("id") Long id);
}