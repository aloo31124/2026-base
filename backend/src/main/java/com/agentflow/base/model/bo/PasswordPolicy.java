package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_policy")
public class PasswordPolicy extends BaseEntity {
    @Column(name = "policy_key", nullable = false, unique = true, length = 40)
    private String policyKey;
    @Column(name = "minimum_length", nullable = false)
    private int minimumLength;
    @Column(name = "require_english", nullable = false)
    private boolean requireEnglish;
    @Column(name = "require_digit", nullable = false)
    private boolean requireDigit;

    protected PasswordPolicy() {}
    public PasswordPolicy(int minimumLength, boolean requireEnglish, boolean requireDigit) {
        this.policyKey = "DEFAULT";
        update(minimumLength, requireEnglish, requireDigit);
    }
    public int getMinimumLength() { return minimumLength; }
    public boolean isRequireEnglish() { return requireEnglish; }
    public boolean isRequireDigit() { return requireDigit; }
    public void update(int minimumLength, boolean requireEnglish, boolean requireDigit) {
        this.minimumLength = minimumLength;
        this.requireEnglish = requireEnglish;
        this.requireDigit = requireDigit;
    }
}
