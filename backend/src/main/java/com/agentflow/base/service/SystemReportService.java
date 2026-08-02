package com.agentflow.base.service;

import com.agentflow.base.dao.AssignedTaskDao;
import com.agentflow.base.dao.CompanyDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.Company;
import com.agentflow.base.model.dto.SystemReportDtos.CompanyOption;
import com.agentflow.base.model.dto.SystemReportDtos.TaskTrendPoint;
import com.agentflow.base.model.dto.SystemReportDtos.TaskTrendReport;
import com.agentflow.base.model.dto.SystemReportDtos.TaskTrendSource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SystemReportService {
    private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Taipei");
    private static final long MAX_RANGE_DAYS = 366;
    private static final String ALL_COMPANIES = "全部公司";

    private final CompanyDao companyDao;
    private final AssignedTaskDao taskDao;

    /**
     * 注入系統報表所需資料存取元件。
     *
     * @param companyDao 公司資料存取
     * @param taskDao 任務資料存取
     */
    public SystemReportService(CompanyDao companyDao, AssignedTaskDao taskDao) {
        this.companyDao = companyDao;
        this.taskDao = taskDao;
    }

    /**
     * 取得依名稱排序的公司篩選選項。
     *
     * @return 公司選項
     */
    public List<CompanyOption> companies() {
        return companyDao.findAllByOrderByNameAsc().stream()
            .map(company -> new CompanyOption(company.getId(), company.getName()))
            .toList();
    }

    /**
     * 依可選公司及日期範圍建立按日任務趨勢。
     *
     * @param companyId 公司識別；空值代表全部公司
     * @param requestedFrom 使用者指定起日
     * @param requestedTo 使用者指定迄日
     * @return 含零值日期的任務趨勢
     */
    public TaskTrendReport taskTrend(UUID companyId, LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate to = requestedTo == null ? LocalDate.now(REPORT_ZONE) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusYears(1) : requestedFrom;
        validateRange(from, to);

        Company company = companyId == null ? null : companyDao.findById(companyId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到指定公司。"));
        List<TaskTrendSource> sources = taskDao.findTaskTrendSources(
            from.atStartOfDay(REPORT_ZONE).toInstant(),
            to.plusDays(1).atStartOfDay(REPORT_ZONE).toInstant(),
            companyId
        );

        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            counts.put(date, 0L);
        }
        for (TaskTrendSource source : sources) {
            LocalDate date = source.assignedAt().atZone(REPORT_ZONE).toLocalDate();
            counts.computeIfPresent(date, (ignored, count) -> count + 1);
        }

        List<TaskTrendPoint> points = counts.entrySet().stream()
            .map(entry -> new TaskTrendPoint(entry.getKey(), entry.getValue()))
            .toList();
        long companyCount = company == null
            ? sources.stream().map(TaskTrendSource::companyId).distinct().count()
            : 1L;
        return new TaskTrendReport(
            companyId,
            company == null ? ALL_COMPANIES : company.getName(),
            from,
            to,
            sources.size(),
            companyCount,
            points
        );
    }

    /**
     * 驗證日期順序與最多 366 天限制。
     *
     * @param from 含起日
     * @param to 含迄日
     */
    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "開始日期不得晚於結束日期。");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_RANGE_DAYS) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "日期範圍不得超過 366 天。");
        }
    }
}
