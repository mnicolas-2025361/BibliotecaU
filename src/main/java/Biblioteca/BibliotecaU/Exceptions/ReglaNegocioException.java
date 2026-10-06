package Biblioteca.BibliotecaU.Exceptions;

/** Base para violaciones de reglas de negocio (se responderán como 409). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}