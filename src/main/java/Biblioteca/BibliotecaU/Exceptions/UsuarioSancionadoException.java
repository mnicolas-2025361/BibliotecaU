package Biblioteca.BibliotecaU.Exceptions;

public class UsuarioSancionadoException extends ReglaNegocioException {
    public UsuarioSancionadoException(String mensaje) {
        super(mensaje);
    }
}