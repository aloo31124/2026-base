package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_policy")
public class PasswordPolicy extends BaseEntity {
    public static final int DEFAULT_MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72;

    @Column(name = "min_length", nullable = false)
    private int minLength;

    @Column(name = "require_letter", nullable = false)
    private boolean requireLetter;

    @Column(name = "require_number", nullable = false)
    private boolean requireNumber;

    protected PasswordPolicy() {
    }

    public PasswordPolicy(int minLength, boolean requireLetter, boolean requireNumber) {
        update(minLength, requireLetter, requireNumber);
    }

    public int getMinLength() {
        return minLength;
    }

    public boolean isRequireLetter() {
        return requireLetter;
    }

    public boolean isRequireNumber() {
        return requireNumber;
    }

    /**
     * 更新密碼政策欄位；輸入範圍由 Service 統一驗證。
     *
     * @param minLength 最小密碼長度
     * @param requireLetter 是否要求英文字母
     * @param requireNumber 是否要求數字
     */
    public void update(int minLength, boolean requireLetter, boolean requireNumber) {
        this.minLength = minLength;
        this.requireLetter = requireLetter;
        this.requireNumber = requireNumber;
    }
}
