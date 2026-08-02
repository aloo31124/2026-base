package com.agentflow.base.dao;

import com.agentflow.base.model.bo.SessionTimeoutPolicy;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTimeoutPolicyDao extends JpaRepository<SessionTimeoutPolicy, UUID> {
    Optional<SessionTimeoutPolicy> findFirstByOrderByCreatedAtAsc();
}
