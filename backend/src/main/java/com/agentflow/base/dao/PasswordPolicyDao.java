package com.agentflow.base.dao;

import com.agentflow.base.model.bo.PasswordPolicy;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordPolicyDao extends JpaRepository<PasswordPolicy, UUID> {
    Optional<PasswordPolicy> findFirstByOrderByCreatedAtAsc();
}
