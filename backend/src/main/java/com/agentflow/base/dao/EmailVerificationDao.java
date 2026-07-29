package com.agentflow.base.dao;

import com.agentflow.base.model.bo.EmailVerification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationDao extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findFirstByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
    List<EmailVerification> findAllByEmailAndPurposeAndStatus(String email, String purpose, String status);
}
