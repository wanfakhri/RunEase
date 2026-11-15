package my.uitm.runease.service;

/**
 * Application-level error carrying the HTTP status the servlet layer should
 * return. It is unchecked so servlet handlers can throw it from route helpers
 * without threading checked signatures through every method.
 */
public class AppException extends RuntimeException {

    private final int status;

    public AppException(String message) {
        this(message, 400);
    }

    public AppException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
