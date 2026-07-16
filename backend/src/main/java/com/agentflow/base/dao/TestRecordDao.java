package com.agentflow.base.dao;

import com.agentflow.base.model.bo.TestRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRecordDao extends JpaRepository<TestRecord, UUID> {}

