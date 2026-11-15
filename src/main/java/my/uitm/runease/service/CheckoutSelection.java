package my.uitm.runease.service;

import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventCategory;

public class CheckoutSelection {

    private final Event event;
    private final EventCategory category;
    private final boolean alreadyRegistered;
    private final boolean full;

    public CheckoutSelection(Event event, EventCategory category, boolean alreadyRegistered, boolean full) {
        this.event = event;
        this.category = category;
        this.alreadyRegistered = alreadyRegistered;
        this.full = full;
    }

    public Event getEvent() {
        return event;
    }

    public EventCategory getCategory() {
        return category;
    }

    public boolean isAlreadyRegistered() {
        return alreadyRegistered;
    }

    public boolean isFull() {
        return full;
    }
}
