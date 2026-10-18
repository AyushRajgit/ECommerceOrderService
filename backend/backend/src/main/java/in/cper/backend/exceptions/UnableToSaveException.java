package in.cper.backend.exceptions;

public class UnableToSaveException extends RuntimeException {
    public UnableToSaveException(String message) {
        super(message);
    }
}
