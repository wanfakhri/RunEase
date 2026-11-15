package my.uitm.runease.model;

import java.time.LocalDateTime;

import my.uitm.runease.util.Fmt;

public class Result {

    private long resultId;
    private long registrationId;
    private int finishTimeSeconds;
    private Integer overallPosition;
    private Integer categoryPosition;
    private Integer paceSeconds;
    private Long recordedBy;
    private LocalDateTime recordedAt;

    public long getResultId() {
        return resultId;
    }

    public void setResultId(long resultId) {
        this.resultId = resultId;
    }

    public long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(long registrationId) {
        this.registrationId = registrationId;
    }

    public int getFinishTimeSeconds() {
        return finishTimeSeconds;
    }

    public void setFinishTimeSeconds(int finishTimeSeconds) {
        this.finishTimeSeconds = finishTimeSeconds;
    }

    public Integer getOverallPosition() {
        return overallPosition;
    }

    public void setOverallPosition(Integer overallPosition) {
        this.overallPosition = overallPosition;
    }

    public Integer getCategoryPosition() {
        return categoryPosition;
    }

    public void setCategoryPosition(Integer categoryPosition) {
        this.categoryPosition = categoryPosition;
    }

    public Integer getPaceSeconds() {
        return paceSeconds;
    }

    public void setPaceSeconds(Integer paceSeconds) {
        this.paceSeconds = paceSeconds;
    }

    public Long getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(Long recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getFinishTimeFormatted() {
        return Fmt.duration(finishTimeSeconds);
    }

    public String getPaceFormatted() {
        return Fmt.pace(paceSeconds);
    }

    public String getOverallPositionLabel() {
        return overallPosition == null ? "-" : "#" + overallPosition;
    }

    public String getCategoryPositionLabel() {
        return categoryPosition == null ? "-" : "#" + categoryPosition;
    }

    public String getRecordedAtFormatted() {
        return Fmt.dateTime(recordedAt);
    }
}
