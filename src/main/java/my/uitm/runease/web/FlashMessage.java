package my.uitm.runease.web;

public class FlashMessage {

    private final String type;
    private final String message;

    public FlashMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getIcon() {
        return switch (type) {
            case "success" -> "bi-check-circle-fill";
            case "danger" -> "bi-x-octagon-fill";
            case "warning" -> "bi-exclamation-triangle-fill";
            default -> "bi-info-circle-fill";
        };
    }
}
