package dobby.exceptions;

public class RequestTooBigException extends RuntimeException {
    public RequestTooBigException(String message) {
        super(message);
    }
}
