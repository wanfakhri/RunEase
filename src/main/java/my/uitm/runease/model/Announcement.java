package my.uitm.runease.model;

import java.time.LocalDateTime;

import my.uitm.runease.util.Fmt;

public class Announcement {

    private long announcementId;
    private long eventId;
    private String eventTitle;
    private String title;
    private String body;
    private String audience = "ALL";
    private long createdBy;
    private String authorName;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;

    public long getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(long announcementId) {
        this.announcementId = announcementId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPublishedAtFormatted() {
        return Fmt.dateTime(publishedAt);
    }

    public String getAudienceLabel() {
        return "CONFIRMED".equals(audience) ? "Confirmed participants" : "Everyone";
    }

    public String getExcerpt() {
        if (body == null) {
            return "";
        }
        return body.length() <= 140 ? body : body.substring(0, 137) + "...";
    }
}
