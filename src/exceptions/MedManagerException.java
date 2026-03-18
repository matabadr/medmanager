package exceptions;

public class MedManagerException extends RuntimeException {
    public MedManagerException(String message) {
        super(message);
    }
    public MedManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}