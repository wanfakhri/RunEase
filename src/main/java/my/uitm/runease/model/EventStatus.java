package my.uitm.runease.model;

public enum EventStatus {
    DRAFT("Draft", "secondary"),
    PENDING("Pending review", "warning"),
    APPROVED("Approved", "success"),
    REJECTED("Rejected", "danger"),
    COMPLETED("Completed", "info"),
    CANCELLED("Cancelled", "dark");

    private final String label;
    private final String badge;

    EventStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }

    public boolean isPubliclyVisible() {
        return this == APPROVED || this == COMPLETED;
    }

    public static EventStatus parse(String value) {
        for (EventStatus status : values()) {
            if (status.name().equalsIgnoreCase(String.valueOf(value))) {
                return status;
            }
        }
        return DRAFT;
    }
}
