package com.agentflow.base.dao;

import com.agentflow.base.model.bo.AssignedTask;
import com.agentflow.base.model.bo.AssignedTask.WorkStatus;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.dto.ManagerReportDtos.AssigneeOption;
import com.agentflow.base.model.dto.ManagerReportDtos.CompanyTaskSource;
import com.agentflow.base.model.dto.ManagerReportDtos.ManagerTaskSource;
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

    /**
     * 取得指定公司期間內所有受派任務的最小報表來源。
     *
     * @param companyId 主管所屬公司識別
     * @param fromInclusive 台北起日轉換後的含起點時間
     * @param toExclusive 台北迄日次日轉換後的不含終點時間
     * @return 公司任務時間來源
     */
    @Query("""
        select new com.agentflow.base.model.dto.ManagerReportDtos$CompanyTaskSource(task.assignedAt)
        from AssignedTask task
        join CompanyMembership membership on membership.user = task.assignee
        where membership.company.id = :companyId
          and task.assignedAt >= :fromInclusive
          and task.assignedAt < :toExclusive
        order by task.assignedAt asc
        """)
    List<CompanyTaskSource> findCompanyReportSources(
        @Param("companyId") UUID companyId,
        @Param("fromInclusive") Instant fromInclusive,
        @Param("toExclusive") Instant toExclusive
    );

    /**
     * 取得目前主管在期間內建立且符合可選條件的任務來源。
     *
     * @param creator 目前主管
     * @param fromInclusive 台北起日轉換後的含起點時間
     * @param toExclusive 台北迄日次日轉換後的不含終點時間
     * @param assigneeId 可選執行者識別
     * @param workStatus 可選工作執行狀態
     * @return 主管指派任務來源
     */
    @Query("""
        select new com.agentflow.base.model.dto.ManagerReportDtos$ManagerTaskSource(
            task.assignedAt,
            task.assignee.id,
            task.assignee.fullName,
            task.workStatus
        )
        from AssignedTask task
        where task.creator = :creator
          and task.assignedAt >= :fromInclusive
          and task.assignedAt < :toExclusive
          and (:assigneeId is null or task.assignee.id = :assigneeId)
          and (:workStatus is null or task.workStatus = :workStatus)
        order by task.assignedAt asc
        """)
    List<ManagerTaskSource> findManagerReportSources(
        @Param("creator") UserAccount creator,
        @Param("fromInclusive") Instant fromInclusive,
        @Param("toExclusive") Instant toExclusive,
        @Param("assigneeId") UUID assigneeId,
        @Param("workStatus") WorkStatus workStatus
    );

    /**
     * 取得目前主管曾指派過的去重執行者選項。
     *
     * @param creator 目前主管
     * @return 依姓名排序的執行者選項
     */
    @Query("""
        select distinct new com.agentflow.base.model.dto.ManagerReportDtos$AssigneeOption(
            task.assignee.id,
            task.assignee.fullName
        )
        from AssignedTask task
        where task.creator = :creator
        order by task.assignee.fullName asc
        """)
    List<AssigneeOption> findManagerReportAssignees(@Param("creator") UserAccount creator);
}
