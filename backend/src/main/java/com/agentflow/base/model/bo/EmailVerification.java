package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "email_verification")
public class EmailVerification extends BaseEntity {
    @Column(name = "email", nullable = false, length = 160)
    private String email;
    @Column(name = "purpose", nullable = false, length = 30)
    private String purpose;
    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;
    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING";
    @Column(name = "result_code", length = 50)
    private String resultCode;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "verified_at")
    private Instant verifiedAt;
    @Column(name = "consumed_at")
    private Instant consumedAt;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "resend_available_at", nullable = false)
    private Instant resendAvailableAt;

    protected EmailVerification() {}
    public EmailVerification(String email, String purpose, String codeHash, Instant expiresAt, Instant resendAvailableAt) {
        this.email = email; this.purpose = purpose; this.codeHash = codeHash;
        this.expiresAt = expiresAt; this.resendAvailableAt = resendAvailableAt;
    }
    public String getEmail() { return email; }
    public String getPurpose() { return purpose; }
    public String getCodeHash() { return codeHash; }
    public String getStatus() { return status; }
    public String getResultCode() { return resultCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getResendAvailableAt() { return resendAvailableAt; }
    public int getAttemptCount() { return attemptCount; }
    public boolean isPending() { return "PENDING".equals(status); }
    public void supersede() { status = "SUPERSEDED"; resultCode = "RESENT"; }
    public void failedAttempt() { attemptCount++; if (attemptCount >= 5) { status = "FAILED"; resultCode = "TOO_MANY_ATTEMPTS"; } }
    public void expire() { status = "EXPIRED"; resultCode = "CODE_EXPIRED"; }
    public void verify() { status = "VERIFIED"; resultCode = "VERIFIED"; verifiedAt = Instant.now(); }
    public void consume(String result) { status = "CONSUMED"; resultCode = result; consumedAt = Instant.now(); }
}
