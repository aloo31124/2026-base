package com.agentflow.base.dao;

import com.agentflow.base.model.bo.LineOAuthAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineOAuthAccountDao extends JpaRepository<LineOAuthAccount, UUID> {
    @EntityGraph(attributePaths = "user")
    Optional<LineOAuthAccount> findByLineUserId(String lineUserId);
}
