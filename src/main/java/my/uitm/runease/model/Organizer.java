package my.uitm.runease.model;

import java.time.LocalDateTime;

import my.uitm.runease.util.Fmt;

public class Organizer {

    private long organizerId;
    private long userId;
    private String organizationName;
    private String registrationNo;
    private String contactPerson;
    private String website;
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    private LocalDateTime verifiedAt;
    private Long verifiedBy;
    private LocalDateTime createdAt;

    // joined / derived
    private String userEmail;
    private String userPhone;
    private String userName;
    private int eventCount;

    public long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(long organizerId) {
        this.organizerId = organizerId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public String getStatusLabel() {
        return verificationStatus.getLabel();
    }

    public String getStatusBadge() {
        return verificationStatus.getBadge();
    }

    public String getVerifiedAtFormatted() {
        return verifiedAt == null ? "Not verified" : Fmt.dateTime(verifiedAt);
    }

    public String getInitials() {
        if (organizationName == null || organizationName.isBlank()) {
            return "?";
        }
        String[] parts = organizationName.trim().split("\\s+");
        return ("" + parts[0].charAt(0) + (parts.length > 1 ? String.valueOf(parts[1].charAt(0)) : "")).toUpperCase();
    }
}
