package my.uitm.runease.model;

public enum EventType {
    ROAD("Road race"),
    TRAIL("Trail"),
    FUN_RUN("Fun run"),
    VIRTUAL("Virtual");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static EventType parse(String value) {
        for (EventType type : values()) {
            if (type.name().equalsIgnoreCase(String.valueOf(value))) {
                return type;
            }
        }
        return ROAD;
    }
}
