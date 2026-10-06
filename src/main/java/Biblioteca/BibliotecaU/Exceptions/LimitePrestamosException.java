package Biblioteca.BibliotecaU.Exceptions;

public class LimitePrestamosException extends ReglaNegocioException {
    public LimitePrestamosException(String mensaje) {
        super(mensaje);
    }
}