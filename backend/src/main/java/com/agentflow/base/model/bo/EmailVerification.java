package com.agentflow.base.model.bo;

import com.agentflow.base.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "email_verification")
public class EmailVerification extends BaseEntity {
    public enum Purpose {
        REGISTRATION,
        PASSWORD_RESET
    }

    public enum Status {
        PENDING,
        VERIFIED,
        COMPLETED,
        INVALIDATED
    }

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 32)
    private Purpose purpose;

    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "used_at")
    private Instant usedAt;

    protected EmailVerification() {
    }

    /**
     * 建立尚未核銷的信箱驗證流程。
     */
    public EmailVerification(String email, Purpose purpose, String codeHash, Instant expiresAt) {
        this.email = email;
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.sentAt = Instant.now();
        this.status = Status.PENDING;
    }

    /**
     * 核對驗證碼並在成功時標記驗證時間。
     *
     * @param code 使用者輸入的六位數驗證碼
     * @param encoder 驗證雜湊的密碼編碼器
     */
    public void verify(String code, PasswordEncoder encoder) {
        Instant now = Instant.now();
        ensureAvailable(now);
        if (!encoder.matches(code, codeHash)) {
            failedAttempts += 1;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                status = Status.INVALIDATED;
            }
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                failedAttempts >= MAX_FAILED_ATTEMPTS ? "驗證碼錯誤次數已達上限。" : "驗證碼錯誤。"
            );
        }
        verifiedAt = now;
        status = Status.VERIFIED;
    }

    /**
     * 確認票券可供指定業務使用。
     *
     * @param expectedEmail 預期信箱
     * @param expectedPurpose 預期用途
     */
    public void ensureUsableTicket(String expectedEmail, Purpose expectedPurpose) {
        Instant now = Instant.now();
        if (!email.equals(expectedEmail) || purpose != expectedPurpose) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "驗證票券與信箱或用途不符。");
        }
        ensureAvailable(now);
        if (verifiedAt == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "信箱尚未完成驗證。");
        }
    }

    /**
     * 將已驗證票券標記為使用完成。
     */
    public void consume() {
        if (usedAt != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "驗證票券已使用。");
        }
        usedAt = Instant.now();
        status = Status.COMPLETED;
    }

    /**
     * 檢查驗證流程是否仍可核銷。
     */
    private void ensureAvailable(Instant now) {
        if (usedAt != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "驗證票券已使用。");
        }
        if (!expiresAt.isAfter(now)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "驗證碼或票券已失效。");
        }
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "驗證碼錯誤次數已達上限。");
        }
    }

    public String getEmail() {
        return email;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }
}
