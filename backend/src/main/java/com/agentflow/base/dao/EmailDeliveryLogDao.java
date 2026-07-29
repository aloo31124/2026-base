package com.agentflow.base.dao;

import com.agentflow.base.model.bo.EmailDeliveryLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailDeliveryLogDao extends JpaRepository<EmailDeliveryLog, UUID> {
    List<EmailDeliveryLog> findTop20ByOrderByCreatedAtDesc();
}
