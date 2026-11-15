package my.uitm.runease.model;

public enum AccountStatus {
    ACTIVE("Active", "success"),
    INACTIVE("Inactive", "secondary"),
    SUSPENDED("Suspended", "danger");

    private final String label;
    private final String badge;

    AccountStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }

    public static AccountStatus parse(String value) {
        for (AccountStatus status : values()) {
            if (status.name().equalsIgnoreCase(String.valueOf(value))) {
                return status;
            }
        }
        return INACTIVE;
    }
}
