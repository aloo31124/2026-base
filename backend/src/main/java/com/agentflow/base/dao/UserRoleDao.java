package com.agentflow.base.dao;

import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UserRoleDao extends JpaRepository<UserRole, UUID> {
    @EntityGraph(attributePaths = "role")
    List<UserRole> findAllByUser(UserAccount user);
    boolean existsByUserAndRole_RoleCode(UserAccount user, String roleCode);

    /**
     * 查找使用者的指定角色關聯，供主管身分移除時同步清理。
     *
     * @param user 使用者
     * @param roleCode 角色代碼
     * @return 使用者角色關聯
     */
    Optional<UserRole> findByUserAndRole_RoleCode(UserAccount user, String roleCode);
}
