package com.agentflow.base.service;

import com.agentflow.base.dao.AssignedTaskDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.AssignedTask.WorkStatus;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.dto.MyReportDtos.AssigneeOption;
import com.agentflow.base.model.dto.MyReportDtos.MyReport;
import com.agentflow.base.model.dto.MyReportDtos.MyReportFilters;
import com.agentflow.base.model.dto.MyReportDtos.MyTaskSource;
import com.agentflow.base.model.dto.MyReportDtos.StatusBucket;
import com.agentflow.base.model.dto.MyReportDtos.TaskTrendPoint;
import com.agentflow.base.model.dto.MyReportDtos.WorkStatusOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MyReportService {
    private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Taipei");
    private static final long MAX_RANGE_DAYS = 366;
    private static final Map<WorkStatus, String> STATUS_LABELS = Map.of(
        WorkStatus.PENDING, "待處理",
        WorkStatus.IN_PROGRESS, "進行中",
        WorkStatus.COMPLETED, "已完成"
    );

    private final UserAccountDao userDao;
    private final AssignedTaskDao taskDao;

    /**
     * 注入登入員工與本人任務資料存取元件。
     *
     * @param userDao 使用者資料存取
     * @param taskDao 指派任務資料存取
     */
    public MyReportService(UserAccountDao userDao, AssignedTaskDao taskDao) {
        this.userDao = userDao;
        this.taskDao = taskDao;
    }

    /**
     * 取得登入員工本人、工作狀態與預設最近一年日期選項。
     *
     * @param username 登入帳號
     * @return 我的報表篩選選項
     */
    public MyReportFilters filters(String username) {
        UserAccount employee = requireEmployee(username);
        LocalDate today = LocalDate.now(REPORT_ZONE);
        return new MyReportFilters(
            List.of(new AssigneeOption(employee.getId(), employee.getFullName())),
            List.of(
                statusOption(WorkStatus.PENDING),
                statusOption(WorkStatus.IN_PROGRESS),
                statusOption(WorkStatus.COMPLETED)
            ),
            today.minusYears(1),
            today
        );
    }

    /**
     * 建立登入員工自己的任務總覽、每日趨勢與狀態比例。
     *
     * @param username 登入帳號
     * @param requestedAssigneeId 可選執行者識別，只能指定本人
     * @param requestedWorkStatus 可選工作執行狀態
     * @param requestedFrom 可選含起日
     * @param requestedTo 可選含迄日
     * @return 綜合我的報表
     */
    public MyReport report(
        String username,
        UUID requestedAssigneeId,
        String requestedWorkStatus,
        LocalDate requestedFrom,
        LocalDate requestedTo
    ) {
        UserAccount employee = requireEmployee(username);
        validateAssignee(employee, requestedAssigneeId);
        LocalDate to = requestedTo == null ? LocalDate.now(REPORT_ZONE) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusYears(1) : requestedFrom;
        validateRange(from, to);
        WorkStatus workStatus = parseWorkStatus(requestedWorkStatus);

        // DAO 固定以登入員工為受派人；外部 assigneeId 永遠不會進入資料查詢條件。
        List<MyTaskSource> allSources = taskDao.findMyReportSources(
            employee,
            from.atStartOfDay(REPORT_ZONE).toInstant(),
            to.plusDays(1).atStartOfDay(REPORT_ZONE).toInstant()
        );
        List<MyTaskSource> sources = allSources.stream()
            .filter(source -> workStatus == null || effectiveStatus(source) == workStatus)
            .toList();

        return new MyReport(
            from,
            to,
            employee.getId(),
            employee.getFullName(),
            workStatus == null ? null : workStatus.name(),
            sources.size(),
            trendPoints(from, to, sources),
            statusBuckets(sources)
        );
    }

    /** 取得登入帳號對應的有效使用者。 */
    private UserAccount requireEmployee(String username) {
        return userDao.findByUsername(username)
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "找不到登入使用者。"));
    }

    /** 僅允許未指定或指定登入員工本人。 */
    private void validateAssignee(UserAccount employee, UUID requestedAssigneeId) {
        if (requestedAssigneeId != null && !requestedAssigneeId.equals(employee.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能查詢自己的任務報表。");
        }
    }

    /** 將外部狀態字串安全轉為既有工作狀態列舉。 */
    private WorkStatus parseWorkStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return WorkStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支援的執行狀態。");
        }
    }

    /** 驗證日期順序與最多 366 天限制。 */
    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "開始日期不得晚於結束日期。");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_RANGE_DAYS) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "日期範圍不得超過 366 天。");
        }
    }

    /** 將任務來源按台北日期彙總並補齊所有零值日。 */
    private List<TaskTrendPoint> trendPoints(LocalDate from, LocalDate to, List<MyTaskSource> sources) {
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            counts.put(date, 0L);
        }

        // 每個來源只計入其台北本地日期一次，保留固定且可預測的連續時間軸。
        for (MyTaskSource source : sources) {
            LocalDate date = source.assignedAt().atZone(REPORT_ZONE).toLocalDate();
            counts.computeIfPresent(date, (ignored, count) -> count + 1);
        }
        return counts.entrySet().stream()
            .map(entry -> new TaskTrendPoint(entry.getKey(), entry.getValue()))
            .toList();
    }

    /** 將本人任務依三種工作狀態計數並換算為一位小數百分比。 */
    private List<StatusBucket> statusBuckets(List<MyTaskSource> sources) {
        Map<WorkStatus, Long> counts = new EnumMap<>(WorkStatus.class);
        for (WorkStatus status : WorkStatus.values()) {
            counts.put(status, 0L);
        }
        for (MyTaskSource source : sources) {
            WorkStatus status = effectiveStatus(source);
            counts.compute(status, (ignored, count) -> count + 1);
        }

        // 最後一個狀態吸收四捨五入差額，使有資料時比例精確合計 100%。
        long total = sources.size();
        double allocated = 0;
        List<StatusBucket> buckets = new ArrayList<>();
        WorkStatus[] statuses = WorkStatus.values();
        for (int index = 0; index < statuses.length; index++) {
            WorkStatus status = statuses[index];
            double percentage = total == 0
                ? 0
                : index == statuses.length - 1
                    ? roundOneDecimal(100 - allocated)
                    : roundOneDecimal(counts.get(status) * 100.0 / total);
            allocated += percentage;
            buckets.add(new StatusBucket(status.name(), STATUS_LABELS.get(status), counts.get(status), percentage));
        }
        return List.copyOf(buckets);
    }

    /** 讓 legacy 空狀態與 AssignedTask getter 一致視為待處理。 */
    private WorkStatus effectiveStatus(MyTaskSource source) {
        return source.workStatus() == null ? WorkStatus.PENDING : source.workStatus();
    }

    /** 建立單一工作狀態篩選選項。 */
    private WorkStatusOption statusOption(WorkStatus status) {
        return new WorkStatusOption(status.name(), STATUS_LABELS.get(status));
    }

    /** 將百分比四捨五入至一位小數。 */
    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
