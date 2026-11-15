package my.uitm.runease.model;

import java.time.LocalDateTime;

import my.uitm.runease.util.Fmt;

public class AuditLog {

    private long logId;
    private Long userId;
    private String userName;
    private String action;
    private String entityType;
    private String entityId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAtFormatted() {
        return Fmt.dateTime(createdAt);
    }

    public String getActionBadge() {
        if (action == null) {
            return "secondary";
        }
        return switch (action) {
            case "LOGIN" -> "info";
            case "CREATE" -> "success";
            case "UPDATE" -> "warning";
            case "DELETE", "REJECT" -> "danger";
            case "APPROVE", "CHECK_IN" -> "primary";
            default -> "secondary";
        };
    }

    public String getActionLabel() {
        return action == null ? "" : action.replace('_', ' ');
    }

    public String getActorInitials() {
        if (userName == null || userName.isBlank()) {
            return "SY";
        }
        String[] parts = userName.trim().split("\\s+");
        return ("" + parts[0].charAt(0) + (parts.length > 1 ? String.valueOf(parts[1].charAt(0)) : "")).toUpperCase();
    }
}
