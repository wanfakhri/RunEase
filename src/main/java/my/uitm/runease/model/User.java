package my.uitm.runease.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import my.uitm.runease.util.Fmt;

public class User implements Serializable {

    private long userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private Role role = Role.PARTICIPANT;
    private AccountStatus status = AccountStatus.ACTIVE;
    private String gender;
    private LocalDate dateOfBirth;
    private String city;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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

    // --- Derived properties used by the JSP views -----------------------
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.toString();
    }

    public String getFirstName() {
        if (fullName == null || fullName.isBlank()) {
            return "Runner";
        }
        return fullName.trim().split("\\s+")[0];
    }

    public String getRoleLabel() {
        return role == null ? "" : role.getLabel();
    }

    public String getRoleKey() {
        return role == null ? "" : role.name();
    }

    public String getStatusLabel() {
        return status == null ? "" : status.getLabel();
    }

    public String getStatusBadge() {
        return status == null ? "secondary" : status.getBadge();
    }

    public String getDateOfBirthFormatted() {
        return Fmt.date(dateOfBirth);
    }

    public String getLastLoginFormatted() {
        return lastLoginAt == null ? "Never" : Fmt.dateTime(lastLoginAt);
    }

    public String getCreatedAtFormatted() {
        return Fmt.dateTime(createdAt);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isOrganizer() {
        return role == Role.ORGANIZER;
    }

    public boolean isParticipant() {
        return role == Role.PARTICIPANT;
    }
}
