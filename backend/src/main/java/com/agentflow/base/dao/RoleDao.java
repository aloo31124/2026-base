package com.agentflow.base.dao;

import com.agentflow.base.model.bo.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleCode(String roleCode);
}

