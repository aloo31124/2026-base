package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "email_delivery_log")
public class EmailDeliveryLog extends BaseEntity {
    public enum Purpose {
        ADMIN_TEST,
        REGISTRATION,
        PASSWORD_RESET
    }

    public enum Status {
        SUCCESS,
        FAILED
    }

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Column(name = "masked_recipient", nullable = false, length = 180)
    private String maskedRecipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 32)
    private Purpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private Status status;

    @Column(name = "error_summary", length = 240)
    private String errorSummary;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected EmailDeliveryLog() {
    }

    /**
     * 建立不包含驗證碼與秘密的寄送稽核紀錄。
     */
    public EmailDeliveryLog(
        String email,
        String maskedRecipient,
        Purpose purpose,
        Status status,
        String errorSummary,
        Instant completedAt
    ) {
        this.email = email;
        this.maskedRecipient = maskedRecipient;
        this.purpose = purpose;
        this.status = status;
        this.errorSummary = errorSummary;
        this.completedAt = completedAt;
    }

    public String getEmail() {
        return email;
    }

    public String getMaskedRecipient() {
        return maskedRecipient;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
