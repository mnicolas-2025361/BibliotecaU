package Biblioteca.BibliotecaU.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "prestamos",
        indexes = {
                @Index(name = "idx_prestamo_usuario", columnList = "usuario_id"),
                @Index(name = "idx_prestamo_libro", columnList = "libro_id"),
                // Conteo de activos por lector y detección de sanción
                @Index(name = "idx_prestamo_usuario_estado", columnList = "usuario_id, estado"),
                // Reporte de atrasados
                @Index(name = "idx_prestamo_estado_vencimiento", columnList = "estado, fecha_vencimiento")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestamo {

    public static final int DIAS_PRESTAMO = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "fecha_prestamo", nullable = false, updatable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_vencimiento", nullable = false, updatable = false)
    private LocalDateTime fechaVencimiento;

    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @PrePersist
    void alCrear() {
        if (fechaPrestamo == null) fechaPrestamo = LocalDateTime.now();
        if (fechaVencimiento == null) fechaVencimiento = fechaPrestamo.plusDays(DIAS_PRESTAMO);
    }

    public boolean estaAtrasado() {
        return estado == EstadoPrestamo.ACTIVO && fechaVencimiento.isBefore(LocalDateTime.now());
    }

    public void marcarDevuelto() {
        this.estado = EstadoPrestamo.DEVUELTO;
        this.fechaDevolucion = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestamo other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}