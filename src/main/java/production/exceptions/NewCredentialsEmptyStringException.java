package production.exceptions;

public class NewCredentialsEmptyStringException extends RuntimeException {
    public NewCredentialsEmptyStringException(String message) {
        super(message);
    }
    public NewCredentialsEmptyStringException() {
        super("New credentials are an empty string.");
    }
    public NewCredentialsEmptyStringException(String message, Throwable cause){
        super(message, cause);
    }
    public NewCredentialsEmptyStringException(Throwable cause){
        super(cause);
    }
}
