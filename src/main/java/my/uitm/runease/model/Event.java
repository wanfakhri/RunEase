package my.uitm.runease.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import my.uitm.runease.util.Fmt;

public class Event {

    private long eventId;
    private long organizerId;
    private String organizerName;
    private String title;
    private String slug;
    private String description;
    private EventType eventType = EventType.ROAD;
    private String venue;
    private String city;
    private String state;
    private LocalDate eventDate;
    private LocalTime flagOffTime;
    private LocalDate registrationOpen;
    private LocalDate registrationClose;
    private EventStatus status = EventStatus.DRAFT;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private List<EventCategory> categories = new ArrayList<>();
    private int registeredCount;
    private int paidCount;
    private int totalQuota;
    private BigDecimal minPrice;
    private BigDecimal revenue;

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(long organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getFlagOffTime() {
        return flagOffTime;
    }

    public void setFlagOffTime(LocalTime flagOffTime) {
        this.flagOffTime = flagOffTime;
    }

    public LocalDate getRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(LocalDate registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public LocalDate getRegistrationClose() {
        return registrationClose;
    }

    public void setRegistrationClose(LocalDate registrationClose) {
        this.registrationClose = registrationClose;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<EventCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<EventCategory> categories) {
        this.categories = categories;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(int paidCount) {
        this.paidCount = paidCount;
    }

    public int getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(int totalQuota) {
        this.totalQuota = totalQuota;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    // --- Derived properties used by the JSP views -----------------------
    public String getStatusLabel() {
        return status.getLabel();
    }

    public String getStatusBadge() {
        return status.getBadge();
    }

    public String getTypeLabel() {
        return eventType.getLabel();
    }

    public String getEventDateFormatted() {
        return Fmt.dateLong(eventDate);
    }

    public String getShortDateFormatted() {
        return Fmt.date(eventDate);
    }

    public String getDateDay() {
        return eventDate == null ? "--" : String.format("%02d", eventDate.getDayOfMonth());
    }

    public String getDateMonth() {
        return Fmt.month(eventDate);
    }

    public String getFlagOffFormatted() {
        return Fmt.time(flagOffTime);
    }

    public String getRegistrationWindow() {
        if (registrationOpen == null || registrationClose == null) {
            return "-";
        }
        return Fmt.date(registrationOpen) + " - " + Fmt.date(registrationClose);
    }

    public String getLocation() {
        if (city == null || city.isBlank()) {
            return state == null ? "" : state;
        }
        return state == null || state.isBlank() ? city : city + ", " + state;
    }

    public long getDaysToEvent() {
        return eventDate == null ? 0 : ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
    }

    public String getCountdownLabel() {
        long days = getDaysToEvent();
        if (days < 0) {
            return "Completed";
        }
        if (days == 0) {
            return "Today";
        }
        return days + (days == 1 ? " day to go" : " days to go");
    }

    public boolean isRegistrationOpen() {
        LocalDate today = LocalDate.now();
        return status == EventStatus.APPROVED
                && registrationOpen != null && registrationClose != null
                && !today.isBefore(registrationOpen) && !today.isAfter(registrationClose);
    }

    public boolean isRegistrationClosed() {
        return registrationClose != null && LocalDate.now().isAfter(registrationClose);
    }

    public String getRegistrationStateLabel() {
        LocalDate today = LocalDate.now();
        if (status != EventStatus.APPROVED) {
            return status.getLabel();
        }
        if (registrationOpen != null && today.isBefore(registrationOpen)) {
            return "Opens " + Fmt.date(registrationOpen);
        }
        if (isRegistrationClosed()) {
            return "Registration closed";
        }
        long days = ChronoUnit.DAYS.between(today, registrationClose);
        if (days <= 7) {
            return "Closing soon";
        }
        return "Registration open";
    }

    public String getRegistrationStateBadge() {
        if (status != EventStatus.APPROVED) {
            return status.getBadge();
        }
        if (registrationOpen != null && LocalDate.now().isBefore(registrationOpen)) {
            return "secondary";
        }
        if (isRegistrationClosed()) {
            return "dark";
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), registrationClose) <= 7 ? "warning" : "success";
    }

    public String getCoverClass() {
        return "cover-" + (int) (eventId % 6);
    }

    public String getDistanceSummary() {
        if (categories == null || categories.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (EventCategory category : categories) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(category.getDistanceLabel());
        }
        return sb.toString();
    }

    public String getMinPriceFormatted() {
        if (minPrice == null) {
            return "-";
        }
        return "From " + Fmt.money(minPrice);
    }

    public int getSlotsLeft() {
        return Math.max(totalQuota - paidCount, 0);
    }

    public int getFillPercent() {
        if (totalQuota <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round(paidCount * 100.0 / totalQuota));
    }

    public String getRevenueFormatted() {
        return Fmt.money(revenue == null ? BigDecimal.ZERO : revenue);
    }
}
