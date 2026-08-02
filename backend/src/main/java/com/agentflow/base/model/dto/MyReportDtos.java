package com.agentflow.base.model.dto;

import com.agentflow.base.model.bo.AssignedTask.WorkStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class MyReportDtos {
    /** 禁止建立只提供我的報表資料模型的工具類別。 */
    private MyReportDtos() {
    }

    /** 登入員工本人執行者選項。 */
    public record AssigneeOption(UUID id, String name) {
    }

    /** 工作執行狀態的值與繁體中文標籤。 */
    public record WorkStatusOption(String value, String label) {
    }

    /** 我的報表頁面初始化所需選項與預設日期。 */
    public record MyReportFilters(
        List<AssigneeOption> assignees,
        List<WorkStatusOption> workStatuses,
        LocalDate defaultFrom,
        LocalDate defaultTo
    ) {
    }

    /** 本人任務 DAO 查詢的最小報表來源。 */
    public record MyTaskSource(
        Instant assignedAt,
        UUID assigneeId,
        String assigneeName,
        WorkStatus workStatus
    ) {
    }

    /** 任務趨勢的一個連續日期資料點。 */
    public record TaskTrendPoint(LocalDate date, long taskCount) {
    }

    /** 工作狀態比例的一個圖例資料桶。 */
    public record StatusBucket(String status, String label, long taskCount, double percentage) {
    }

    /** 套用條件、個人總數與兩種圖表資料的綜合回應。 */
    public record MyReport(
        LocalDate from,
        LocalDate to,
        UUID assigneeId,
        String assigneeName,
        String workStatus,
        long totalTasks,
        List<TaskTrendPoint> trendPoints,
        List<StatusBucket> statusBuckets
    ) {
    }
}
