package my.uitm.runease.model;

public enum RegistrationStatus {
    PENDING_PAYMENT("Pending payment", "warning"),
    CONFIRMED("Confirmed", "success"),
    CANCELLED("Cancelled", "secondary"),
    CHECKED_IN("Checked in", "info"),
    FINISHED("Finished", "primary"),
    DISQUALIFIED("Disqualified", "danger");

    private final String label;
    private final String badge;

    RegistrationStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }

    public boolean isActive() {
        return this != CANCELLED && this != DISQUALIFIED;
    }

    public boolean isPaid() {
        return this == CONFIRMED || this == CHECKED_IN || this == FINISHED;
    }

    public static RegistrationStatus parse(String value) {
        for (RegistrationStatus status : values()) {
            if (status.name().equalsIgnoreCase(String.valueOf(value))) {
                return status;
            }
        }
        return PENDING_PAYMENT;
    }
}
