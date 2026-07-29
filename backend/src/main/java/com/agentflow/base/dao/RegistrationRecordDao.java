package com.agentflow.base.dao;

import com.agentflow.base.model.bo.RegistrationRecord;
import com.agentflow.base.model.bo.UserAccount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRecordDao extends JpaRepository<RegistrationRecord, UUID> {
    boolean existsByUser(UserAccount user);

    @EntityGraph(attributePaths = "user")
    List<RegistrationRecord> findAllByOrderByCompletedAtDesc(Pageable pageable);
}
