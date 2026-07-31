package com.agentflow.base.dao;

import com.agentflow.base.model.bo.AssignedTask;
import com.agentflow.base.model.bo.UserAccount;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignedTaskDao extends JpaRepository<AssignedTask, UUID> {
    @EntityGraph(attributePaths = {"creator", "assignee"})
    @Query("""
        select task from AssignedTask task
        where task.creator = :creator
          and (:name = '' or lower(task.name) like concat('%', :name, '%'))
          and (:assignee = '' or lower(task.assignee.username) like concat('%', :assignee, '%'))
          and (:assignedFrom is null or task.assignedAt >= :assignedFrom)
          and (:assignedTo is null or task.assignedAt <= :assignedTo)
          and (:deadlineFrom is null or task.deadline >= :deadlineFrom)
          and (:deadlineTo is null or task.deadline <= :deadlineTo)
        """)
    List<AssignedTask> searchCreatedTasks(
        @Param("creator") UserAccount creator,
        @Param("name") String name,
        @Param("assignee") String assignee,
        @Param("assignedFrom") Instant assignedFrom,
        @Param("assignedTo") Instant assignedTo,
        @Param("deadlineFrom") Instant deadlineFrom,
        @Param("deadlineTo") Instant deadlineTo,
        Sort sort
    );

    @EntityGraph(attributePaths = {"creator", "assignee"})
    List<AssignedTask> findAllByAssigneeOrderByAssignedAtDesc(UserAccount assignee);
}
