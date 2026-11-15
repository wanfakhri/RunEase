package my.uitm.runease.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import my.uitm.runease.util.Fmt;

public class Registration {

    private long registrationId;
    private String registrationCode;
    private long userId;
    private String participantName;
    private String participantEmail;
    private String participantPhone;
    private long categoryId;
    private String categoryName;
    private double distanceKm;
    private long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private String venue;
    private String eventCity;
    private long organizerId;
    private String bibNumber;
    private String jerseySize;
    private String emergencyName;
    private String emergencyPhone;
    private String medicalNotes;
    private RegistrationStatus status = RegistrationStatus.PENDING_PAYMENT;
    private BigDecimal totalAmount;
    private LocalDateTime registeredAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime cancelledAt;

    private Payment payment;
    private Result result;

    public long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(long registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public String getParticipantPhone() {
        return participantPhone;
    }

    public void setParticipantPhone(String participantPhone) {
        this.participantPhone = participantPhone;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
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

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getEventCity() {
        return eventCity;
    }

    public void setEventCity(String eventCity) {
        this.eventCity = eventCity;
    }

    public long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(long organizerId) {
        this.organizerId = organizerId;
    }

    public String getBibNumber() {
        return bibNumber;
    }

    public void setBibNumber(String bibNumber) {
        this.bibNumber = bibNumber;
    }

    public String getJerseySize() {
        return jerseySize;
    }

    public void setJerseySize(String jerseySize) {
        this.jerseySize = jerseySize;
    }

    public String getEmergencyName() {
        return emergencyName;
    }

    public void setEmergencyName(String emergencyName) {
        this.emergencyName = emergencyName;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    // --- Derived properties ---------------------------------------------
    public String getStatusLabel() {
        return status.getLabel();
    }

    public String getStatusBadge() {
        return status.getBadge();
    }

    public String getStatusKey() {
        return status.name();
    }

    public String getParticipantInitials() {
        if (participantName == null || participantName.isBlank()) {
            return "?";
        }
        String[] parts = participantName.trim().split("\\s+");
        return ("" + parts[0].charAt(0) + (parts.length > 1 ? String.valueOf(parts[1].charAt(0)) : "")).toUpperCase();
    }

    public String getAmountFormatted() {
        return Fmt.money(totalAmount);
    }

    public String getEventDateFormatted() {
        return Fmt.date(eventDate);
    }

    public String getRegisteredAtFormatted() {
        return Fmt.dateTime(registeredAt);
    }

    public String getCheckedInAtFormatted() {
        return checkedInAt == null ? "-" : Fmt.dateTime(checkedInAt);
    }

    public String getBibDisplay() {
        return bibNumber == null || bibNumber.isBlank() ? "Not assigned" : bibNumber;
    }

    public boolean isConfirmed() {
        return status.isPaid();
    }

    public boolean isCancellable() {
        return (status == RegistrationStatus.CONFIRMED || status == RegistrationStatus.PENDING_PAYMENT)
                && eventDate != null && !LocalDate.now().isAfter(eventDate);
    }

    public long getDaysToEvent() {
        return eventDate == null ? 0 : ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
    }

    public String getDistanceLabel() {
        if (distanceKm == Math.floor(distanceKm)) {
            return (int) distanceKm + "KM";
        }
        return String.format("%.1fKM", distanceKm);
    }
}
