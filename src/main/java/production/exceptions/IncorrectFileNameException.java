package production.exceptions;

public class IncorrectFileNameException extends Exception {
    public IncorrectFileNameException(String message) {
        super(message);
    }
    public IncorrectFileNameException() {
        super("No file with such name.");
    }
    public IncorrectFileNameException(String message, Throwable cause){
        super(message, cause);
    }
    public IncorrectFileNameException(Throwable cause){
        super(cause);
    }
}
