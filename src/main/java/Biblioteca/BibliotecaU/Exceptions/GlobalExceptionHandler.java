package Biblioteca.BibliotecaU.Exceptions;

import Biblioteca.BibliotecaU.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- Negocio ----------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> duplicado(RecursoDuplicadoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(UsuarioSancionadoException.class)
    public ResponseEntity<ApiError> sancionado(UsuarioSancionadoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    /** Cubre StockAgotadoException, LimitePrestamosException y reglas genéricas. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // ---------- Concurrencia (clave en la prueba de estrés) ----------

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> conflictoVersion(ObjectOptimisticLockingFailureException ex,
                                                     HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT,
                "El recurso fue modificado por otro proceso. Vuelve a consultarlo e intenta de nuevo.", req);
    }

    /** Timeout de bloqueo, transacción vencida o pool de conexiones agotado: el cliente puede reintentar. */
    @ExceptionHandler({PessimisticLockingFailureException.class,
            TransactionTimedOutException.class,
            CannotCreateTransactionException.class})
    public ResponseEntity<ApiError> sobrecarga(Exception ex, HttpServletRequest req) {
        log.warn("Sobrecarga o bloqueo en {}: {}", req.getRequestURI(), ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "1")
                .body(cuerpo(HttpStatus.SERVICE_UNAVAILABLE,
                        "El sistema está con alta demanda. Intenta de nuevo en un momento.", req, Map.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violación de integridad en {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT,
                "La operación viola una restricción de datos (valor duplicado o registro referenciado).", req);
    }

    // ---------- Validación / petición mal formada ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.merge(e.getField(), e.getDefaultMessage(), (a, b) -> a + "; " + b));
        return ResponseEntity.badRequest()
                .body(cuerpo(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", req, errores));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o está mal formado", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + ex.getName(), req);
    }

    // ---------- Seguridad ----------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta operación", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> noAutenticado(AuthenticationException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", req);
    }

    // ---------- Respaldo ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generico(Exception ex, HttpServletRequest req) {
        // Excepciones propias de Spring MVC (405, 404 de ruta, 415, ResponseStatusException...)
        if (ex instanceof org.springframework.web.ErrorResponse er) {
            HttpStatus status = HttpStatus.resolve(er.getStatusCode().value());
            if (status != null) {
                String detalle = er.getBody().getDetail();
                return respuesta(status, detalle != null ? detalle : status.getReasonPhrase(), req);
            }
        }
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
    }

    // ---------- Utilidades ----------

    private ResponseEntity<ApiError> respuesta(HttpStatus status, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(status).body(cuerpo(status, mensaje, req, Map.of()));
    }

    private ApiError cuerpo(HttpStatus status, String mensaje, HttpServletRequest req, Map<String, String> errores) {
        return new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, req.getRequestURI(), errores);
    }
}