package Biblioteca.BibliotecaU.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Table(
        name = "libros",
        indexes = {
                @Index(name = "uk_libro_isbn", columnList = "isbn", unique = true),
                @Index(name = "idx_libro_titulo", columnList = "titulo"),
                @Index(name = "idx_libro_autor", columnList = "autor"),
                @Index(name = "idx_libro_categoria", columnList = "categoria"),
                @Index(name = "idx_libro_stock", columnList = "stock_disponible")
        }
)
// Red de seguridad a nivel de BD: ni el código ni un UPDATE manual pueden romper el invariante
@Check(constraints = "stock_disponible >= 0 AND stock_disponible <= stock_total")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bloqueo optimista para modificaciones vía JPA (ediciones del ADMIN)
    @Version
    private Long version;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String isbn;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String autor;

    @Size(max = 80)
    @Column(length = 80)
    private String categoria;

    @Size(max = 120)
    @Column(length = 120)
    private String editorial;

    @Min(0)
    @Column(name = "stock_total", nullable = false)
    private int stockTotal;

    @Min(0)
    @Column(name = "stock_disponible", nullable = false)
    private int stockDisponible;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}