package com.agentflow.base.dao;

import com.agentflow.base.model.bo.EmailVerification;
import com.agentflow.base.model.bo.EmailVerification.Purpose;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationDao extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findFirstByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
        String email,
        Purpose purpose
    );
}
