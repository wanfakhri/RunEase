package my.uitm.runease.model;

import java.math.BigDecimal;
import java.time.LocalTime;

import my.uitm.runease.util.Fmt;

public class EventCategory {

    private long categoryId;
    private long eventId;
    private String eventTitle;
    private String name;
    private double distanceKm;
    private BigDecimal price;
    private int quota;
    private LocalTime startTime;
    private int registeredCount;
    private int paidCount;

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
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

    // --- Derived properties ---------------------------------------------
    public String getPriceFormatted() {
        return Fmt.money(price);
    }

    public String getDistanceLabel() {
        if (distanceKm == Math.floor(distanceKm)) {
            return (int) distanceKm + "KM";
        }
        return String.format("%.1fKM", distanceKm);
    }

    public String getStartTimeFormatted() {
        return Fmt.time(startTime);
    }

    public int getSlotsLeft() {
        return Math.max(quota - registeredCount, 0);
    }

    public int getFillPercent() {
        if (quota <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round(registeredCount * 100.0 / quota));
    }

    public boolean isFull() {
        return registeredCount >= quota;
    }

    public String getQuotaLabel() {
        return Fmt.number(registeredCount) + " / " + Fmt.number(quota);
    }
}
