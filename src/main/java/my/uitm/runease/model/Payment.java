package my.uitm.runease.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import my.uitm.runease.util.Fmt;

public class Payment {

    private long paymentId;
    private long registrationId;
    private String referenceNo;
    private String method;
    private BigDecimal amount;
    private PaymentStatus status = PaymentStatus.PENDING;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(long registrationId) {
        this.registrationId = registrationId;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatusLabel() {
        return status.getLabel();
    }

    public String getStatusBadge() {
        return status.getBadge();
    }

    public String getAmountFormatted() {
        return Fmt.money(amount);
    }

    public String getMethodLabel() {
        if ("FPX".equals(method)) {
            return "FPX Online Banking";
        }
        if ("CARD".equals(method)) {
            return "Credit / Debit Card";
        }
        if ("EWALLET".equals(method)) {
            return "E-Wallet";
        }
        return method;
    }

    public String getPaidAtFormatted() {
        return paidAt == null ? "-" : Fmt.dateTime(paidAt);
    }
}
