package com.agentflow.base.dao;

import com.agentflow.base.model.bo.AssignedTask;
import com.agentflow.base.model.bo.TaskAttachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAttachmentDao extends JpaRepository<TaskAttachment, UUID> {
    List<TaskAttachment> findAllByTaskOrderByCreatedAtAsc(AssignedTask task);
}
