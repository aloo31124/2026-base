package com.agentflow.base.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class SystemReportDtos {
    /** 禁止建立只提供報表資料模型的工具類別。 */
    private SystemReportDtos() {
    }

    /** 系統報表公司篩選選項。 */
    public record CompanyOption(UUID id, String name) {
    }

    /** DAO 回傳給 Service 彙總的最小任務來源資料。 */
    public record TaskTrendSource(Instant assignedAt, UUID companyId) {
    }

    /** 任務趨勢的一個連續日期資料點。 */
    public record TaskTrendPoint(LocalDate date, long taskCount) {
    }

    /** 任務趨勢篩選條件、摘要與折線圖資料。 */
    public record TaskTrendReport(
        UUID companyId,
        String companyName,
        LocalDate from,
        LocalDate to,
        long totalTasks,
        long companyCount,
        List<TaskTrendPoint> points
    ) {
    }
}
