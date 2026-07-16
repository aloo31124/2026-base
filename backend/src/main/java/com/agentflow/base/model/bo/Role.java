package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role extends BaseEntity {
    @Column(name = "role_code", nullable = false, unique = true, length = 40)
    private String roleCode;
    @Column(name = "display_name", nullable = false, length = 40)
    private String displayName;

    protected Role() {}
    public Role(String roleCode, String displayName) { this.roleCode = roleCode; this.displayName = displayName; }
    public String getRoleCode() { return roleCode; }
    public String getDisplayName() { return displayName; }
}

