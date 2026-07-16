package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class UserAccount extends BaseEntity {
    @Column(name = "full_name", nullable = false, length = 80)
    private String fullName;
    @Column(name = "username", nullable = false, unique = true, length = 60)
    private String username;
    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "registration_method", nullable = false, length = 40)
    private String registrationMethod;
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected UserAccount() {}
    public UserAccount(String fullName, String username, String email, String passwordHash, String registrationMethod) {
        this.fullName = fullName; this.username = username; this.email = email;
        this.passwordHash = passwordHash; this.registrationMethod = registrationMethod;
    }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRegistrationMethod() { return registrationMethod; }
    public boolean isActive() { return active; }
    public void update(String fullName, String email) { this.fullName = fullName; this.email = email; }
    public void updatePassword(String hash) { this.passwordHash = hash; }
    public void disable() { this.active = false; }
}

