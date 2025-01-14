package production.exceptions;

public class DatabaseConnectionException extends Exception {
    public DatabaseConnectionException(String message) {
        super(message);
    }
    public DatabaseConnectionException() {
        super("Incorrect database credentials");
    }
    public DatabaseConnectionException(String message, Throwable cause){
        super(message, cause);
    }
    public DatabaseConnectionException(Throwable cause){
        super(cause);
    }
}
