package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "registration_record")
public class RegistrationRecord extends BaseEntity {
    public enum Method {
        EMAIL,
        LINE
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 16)
    private Method method;

    @Column(name = "identifier", nullable = false, length = 160)
    private String identifier;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected RegistrationRecord() {
    }

    public RegistrationRecord(UserAccount user, Method method, String identifier, Instant completedAt) {
        this.user = user;
        this.method = method;
        this.identifier = identifier;
        this.success = true;
        this.completedAt = completedAt;
    }

    public UserAccount getUser() {
        return user;
    }

    public Method getMethod() {
        return method;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isSuccess() {
        return success;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
