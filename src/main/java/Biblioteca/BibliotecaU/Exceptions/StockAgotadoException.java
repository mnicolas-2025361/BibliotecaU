package Biblioteca.BibliotecaU.Exceptions;

public class StockAgotadoException extends ReglaNegocioException {
    public StockAgotadoException(String mensaje) {
        super(mensaje);
    }
}