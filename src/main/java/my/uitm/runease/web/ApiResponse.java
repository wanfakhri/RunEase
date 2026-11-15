package my.uitm.runease.web;

import java.util.Map;

public class ApiResponse {

    private final boolean ok;
    private final String message;
    private final Object data;
    private final Map<String, String> errors;

    private ApiResponse(boolean ok, String message, Object data, Map<String, String> errors) {
        this.ok = ok;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static ApiResponse ok(String message, Object data) {
        return new ApiResponse(true, message, data, null);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null, null);
    }

    public static ApiResponse validation(String message, Map<String, String> errors) {
        return new ApiResponse(false, message, null, errors);
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
