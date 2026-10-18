package in.cper.backend.exceptions;

public class NoStockAvailableException extends RuntimeException {
    public NoStockAvailableException(String message) {
        super(message);
    }
}
