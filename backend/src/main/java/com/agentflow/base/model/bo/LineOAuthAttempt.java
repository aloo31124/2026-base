package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "line_oauth_attempt")
public class LineOAuthAttempt extends BaseEntity {
    public enum Status { PENDING, SUCCESS, DENIED, FAILED }

    @Column(name = "state_hash", nullable = false, unique = true, length = 64)
    private String stateHash;

    @Column(name = "code_verifier", length = 128)
    private String codeVerifier;

    @Column(name = "nonce", length = 128)
    private String nonce;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private Status status;

    @Column(name = "result_code", length = 64)
    private String resultCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    protected LineOAuthAttempt() {}

    public LineOAuthAttempt(String stateHash, String codeVerifier, String nonce, Instant expiresAt) {
        this.stateHash = stateHash;
        this.codeVerifier = codeVerifier;
        this.nonce = nonce;
        this.expiresAt = expiresAt;
        this.status = Status.PENDING;
    }

    public static LineOAuthAttempt unknownStateFailure(String stateHash, Instant now) {
        LineOAuthAttempt attempt = new LineOAuthAttempt(stateHash, null, null, now);
        attempt.complete(Status.FAILED, "STATE_NOT_FOUND", null, now);
        return attempt;
    }

    public String getStateHash() { return stateHash; }
    public String getCodeVerifier() { return codeVerifier; }
    public String getNonce() { return nonce; }
    public Status getStatus() { return status; }
    public String getResultCode() { return resultCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCompletedAt() { return completedAt; }
    public UserAccount getUser() { return user; }

    public boolean isExpired(Instant now) { return !expiresAt.isAfter(now); }
    public boolean isPending() { return status == Status.PENDING; }

    public void succeed(UserAccount user, Instant now) {
        complete(Status.SUCCESS, "SUCCESS", user, now);
    }

    public void deny(String resultCode, Instant now) {
        complete(Status.DENIED, resultCode, null, now);
    }

    public void fail(String resultCode, Instant now) {
        complete(Status.FAILED, resultCode, null, now);
    }

    private void complete(Status terminalStatus, String resultCode, UserAccount user, Instant now) {
        this.status = terminalStatus;
        this.resultCode = resultCode;
        this.user = user;
        this.completedAt = now;
        this.codeVerifier = null;
        this.nonce = null;
    }
}
