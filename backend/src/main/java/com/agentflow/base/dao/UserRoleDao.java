package com.agentflow.base.dao;

import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UserRoleDao extends JpaRepository<UserRole, UUID> {
    @EntityGraph(attributePaths = "role")
    List<UserRole> findAllByUser(UserAccount user);
    boolean existsByUserAndRole_RoleCode(UserAccount user, String roleCode);
}
