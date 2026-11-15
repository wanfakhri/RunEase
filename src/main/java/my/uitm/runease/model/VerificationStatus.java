package my.uitm.runease.model;

public enum VerificationStatus {
    PENDING("Pending", "warning"),
    VERIFIED("Verified", "success"),
    REJECTED("Rejected", "danger");

    private final String label;
    private final String badge;

    VerificationStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }

    public static VerificationStatus parse(String value) {
        for (VerificationStatus status : values()) {
            if (status.name().equalsIgnoreCase(String.valueOf(value))) {
                return status;
            }
        }
        return PENDING;
    }
}
