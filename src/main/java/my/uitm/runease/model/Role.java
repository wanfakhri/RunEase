package my.uitm.runease.model;

public enum Role {
    ADMIN("Administrator", "/admin"),
    ORGANIZER("Organizer", "/organizer"),
    PARTICIPANT("Participant", "/dashboard");

    private final String label;
    private final String home;

    Role(String label, String home) {
        this.label = label;
        this.home = home;
    }

    public String getLabel() {
        return label;
    }

    public String getHome() {
        return home;
    }

    public static Role parse(String value) {
        if (value == null) {
            return null;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        return null;
    }
}
