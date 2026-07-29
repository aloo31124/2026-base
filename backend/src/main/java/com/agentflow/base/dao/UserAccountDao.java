package com.agentflow.base.dao;

import com.agentflow.base.model.bo.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountDao extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
