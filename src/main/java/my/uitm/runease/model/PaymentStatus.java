package my.uitm.runease.model;

public enum PaymentStatus {
    PENDING("Pending", "warning"),
    PAID("Paid", "success"),
    REFUNDED("Refunded", "info"),
    FAILED("Failed", "danger");

    private final String label;
    private final String badge;

    PaymentStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }

    public static PaymentStatus parse(String value) {
        for (PaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(String.valueOf(value))) {
                return status;
            }
        }
        return PENDING;
    }
}
