package com.agentflow.base.dao;

import com.agentflow.base.model.bo.AssignedTask;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.dto.SystemReportDtos.TaskTrendSource;
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

    @EntityGraph(attributePaths = {"creator", "assignee"})
    @Query("""
        select task from AssignedTask task
        where task.assignee = :assignee
          and (:name = '' or lower(task.name) like concat('%', :name, '%'))
          and (:assignedFrom is null or task.assignedAt >= :assignedFrom)
          and (:assignedTo is null or task.assignedAt <= :assignedTo)
          and (:deadlineFrom is null or task.deadline >= :deadlineFrom)
          and (:deadlineTo is null or task.deadline <= :deadlineTo)
        """)
    List<AssignedTask> searchReceivedTasks(
        @Param("assignee") UserAccount assignee, @Param("name") String name,
        @Param("assignedFrom") Instant assignedFrom, @Param("assignedTo") Instant assignedTo,
        @Param("deadlineFrom") Instant deadlineFrom, @Param("deadlineTo") Instant deadlineTo, Sort sort
    );

    /**
     * 依期間與可選公司取得系統報表任務來源資料。
     *
     * @param fromInclusive 台北起日轉換後的含起點時間
     * @param toExclusive 台北迄日次日轉換後的不含終點時間
     * @param companyId 公司識別；空值代表全部公司
     * @return 任務時間與受派人公司識別
     */
    @Query("""
        select new com.agentflow.base.model.dto.SystemReportDtos$TaskTrendSource(
            task.assignedAt,
            membership.company.id
        )
        from AssignedTask task
        join CompanyMembership membership on membership.user = task.assignee
        where task.assignedAt >= :fromInclusive
          and task.assignedAt < :toExclusive
          and (:companyId is null or membership.company.id = :companyId)
        order by task.assignedAt asc
        """)
    List<TaskTrendSource> findTaskTrendSources(
        @Param("fromInclusive") Instant fromInclusive,
        @Param("toExclusive") Instant toExclusive,
        @Param("companyId") UUID companyId
    );
}
