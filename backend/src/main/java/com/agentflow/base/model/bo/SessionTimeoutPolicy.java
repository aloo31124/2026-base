package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_timeout_policy")
public class SessionTimeoutPolicy extends BaseEntity {
    public static final int MIN_TIMEOUT_MINUTES = 5;
    public static final int MAX_TIMEOUT_MINUTES = 1440;

    @Column(name = "timeout_minutes", nullable = false)
    private int timeoutMinutes;

    protected SessionTimeoutPolicy() {
    }

    public SessionTimeoutPolicy(int timeoutMinutes) {
        update(timeoutMinutes);
    }

    public int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    /**
     * 更新登入效期；合法範圍由 Service 統一驗證。
     *
     * @param timeoutMinutes JWT 登入效期分鐘數
     */
    public void update(int timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }
}
