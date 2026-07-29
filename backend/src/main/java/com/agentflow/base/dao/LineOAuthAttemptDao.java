package com.agentflow.base.dao;

import com.agentflow.base.model.bo.LineOAuthAttempt;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineOAuthAttemptDao extends JpaRepository<LineOAuthAttempt, UUID> {
    Optional<LineOAuthAttempt> findByStateHash(String stateHash);
    long countByStatus(LineOAuthAttempt.Status status);
    long countByResultCode(String resultCode);
}
