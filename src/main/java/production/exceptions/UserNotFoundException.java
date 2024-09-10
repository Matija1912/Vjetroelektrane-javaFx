package production.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException() {
        super("Credentials not found.");
    }
    public UserNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
    public UserNotFoundException(Throwable cause){
        super(cause);
    }
}
