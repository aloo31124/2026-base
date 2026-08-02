package com.agentflow.base.service;

import com.agentflow.base.dao.AssignedTaskDao;
import com.agentflow.base.dao.CompanyMembershipDao;
import com.agentflow.base.dao.SupervisorProfileDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.AssignedTask.WorkStatus;
import com.agentflow.base.model.bo.CompanyMembership;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.dto.ManagerReportDtos.AssigneeOption;
import com.agentflow.base.model.dto.ManagerReportDtos.CompanyTaskSource;
import com.agentflow.base.model.dto.ManagerReportDtos.ManagerReport;
import com.agentflow.base.model.dto.ManagerReportDtos.ManagerReportFilters;
import com.agentflow.base.model.dto.ManagerReportDtos.ManagerTaskSource;
import com.agentflow.base.model.dto.ManagerReportDtos.StatusBucket;
import com.agentflow.base.model.dto.ManagerReportDtos.TaskTrendPoint;
import com.agentflow.base.model.dto.ManagerReportDtos.WorkStatusOption;
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
public class ManagerReportService {
    private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Taipei");
    private static final long MAX_RANGE_DAYS = 366;
    private static final String ALL_ASSIGNEES = "全部執行者";
    private static final Map<WorkStatus, String> STATUS_LABELS = Map.of(
        WorkStatus.PENDING, "待處理",
        WorkStatus.IN_PROGRESS, "進行中",
        WorkStatus.COMPLETED, "已完成"
    );

    private final UserAccountDao userDao;
    private final SupervisorProfileDao supervisorDao;
    private final CompanyMembershipDao membershipDao;
    private final AssignedTaskDao taskDao;

    /**
     * 注入主管報表所需的身分、公司與任務資料存取元件。
     *
     * @param userDao 使用者資料存取
     * @param supervisorDao 主管資料存取
     * @param membershipDao 公司成員資料存取
     * @param taskDao 任務資料存取
     */
    public ManagerReportService(
        UserAccountDao userDao,
        SupervisorProfileDao supervisorDao,
        CompanyMembershipDao membershipDao,
        AssignedTaskDao taskDao
    ) {
        this.userDao = userDao;
        this.supervisorDao = supervisorDao;
        this.membershipDao = membershipDao;
        this.taskDao = taskDao;
    }

    /**
     * 取得目前主管的公司、實際執行者、工作狀態與預設日期選項。
     *
     * @param username 登入帳號
     * @return 主管報表篩選選項
     */
    public ManagerReportFilters filters(String username) {
        UserAccount manager = requireManager(username);
        CompanyMembership membership = requireMembership(manager);
        LocalDate today = LocalDate.now(REPORT_ZONE);

        // 執行者只來自目前主管曾建立的任務，避免列出無關公司人員。
        List<AssigneeOption> assignees = taskDao.findManagerReportAssignees(manager);
        List<WorkStatusOption> workStatuses = List.of(
            statusOption(WorkStatus.PENDING),
            statusOption(WorkStatus.IN_PROGRESS),
            statusOption(WorkStatus.COMPLETED)
        );
        return new ManagerReportFilters(
            membership.getCompany().getName(),
            assignees,
            workStatuses,
            today.minusYears(1),
            today
        );
    }

    /**
     * 建立同公司摘要與目前主管自己指派的趨勢及狀態比例。
     *
     * @param username 登入帳號
     * @param assigneeId 可選執行者識別
     * @param requestedWorkStatus 可選工作執行狀態
     * @param requestedFrom 可選含起日
     * @param requestedTo 可選含迄日
     * @return 綜合主管報表
     */
    public ManagerReport report(
        String username,
        UUID assigneeId,
        String requestedWorkStatus,
        LocalDate requestedFrom,
        LocalDate requestedTo
    ) {
        UserAccount manager = requireManager(username);
        CompanyMembership membership = requireMembership(manager);
        LocalDate to = requestedTo == null ? LocalDate.now(REPORT_ZONE) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusYears(1) : requestedFrom;
        validateRange(from, to);
        WorkStatus workStatus = parseWorkStatus(requestedWorkStatus);

        // 先驗證執行者確實屬於目前主管的歷史指派範圍，避免任意 UUID 探測使用者。
        List<AssigneeOption> assignees = taskDao.findManagerReportAssignees(manager);
        String assigneeName = resolveAssigneeName(assignees, assigneeId);
        var fromInstant = from.atStartOfDay(REPORT_ZONE).toInstant();
        var toExclusive = to.plusDays(1).atStartOfDay(REPORT_ZONE).toInstant();

        // 公司總數只受公司與日期限制，主管圖表則額外套用目前主管、執行者及工作狀態。
        List<CompanyTaskSource> companySources = taskDao.findCompanyReportSources(
            membership.getCompany().getId(),
            fromInstant,
            toExclusive
        );
        List<ManagerTaskSource> managerSources = taskDao.findManagerReportSources(
            manager,
            fromInstant,
            toExclusive,
            assigneeId,
            workStatus
        );

        return new ManagerReport(
            membership.getCompany().getName(),
            from,
            to,
            assigneeId,
            assigneeName,
            workStatus == null ? null : workStatus.name(),
            companySources.size(),
            managerSources.size(),
            trendPoints(from, to, managerSources),
            statusBuckets(managerSources)
        );
    }

    /** 取得登入帳號並驗證具主管資料。 */
    private UserAccount requireManager(String username) {
        UserAccount user = userDao.findByUsername(username)
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "找不到登入使用者。"));
        if (!supervisorDao.existsByUser(user)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "目前使用者不是主管。");
        }
        return user;
    }

    /** 取得主管的必要公司綁定。 */
    private CompanyMembership requireMembership(UserAccount manager) {
        return membershipDao.findByUser(manager)
            .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "請先綁定公司。"));
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

    /** 解析可選執行者顯示名稱，並拒絕不屬於目前主管的識別。 */
    private String resolveAssigneeName(List<AssigneeOption> options, UUID assigneeId) {
        if (assigneeId == null) {
            return ALL_ASSIGNEES;
        }
        return options.stream()
            .filter(option -> option.id().equals(assigneeId))
            .map(AssigneeOption::name)
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到指定執行者。"));
    }

    /** 將任務來源按台北日期彙總並補齊所有零值日。 */
    private List<TaskTrendPoint> trendPoints(
        LocalDate from,
        LocalDate to,
        List<ManagerTaskSource> sources
    ) {
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            counts.put(date, 0L);
        }

        // 每個來源只計入其台北本地日期一次，並保留連續時間軸。
        for (ManagerTaskSource source : sources) {
            LocalDate date = source.assignedAt().atZone(REPORT_ZONE).toLocalDate();
            counts.computeIfPresent(date, (ignored, count) -> count + 1);
        }
        return counts.entrySet().stream()
            .map(entry -> new TaskTrendPoint(entry.getKey(), entry.getValue()))
            .toList();
    }

    /** 將任務依三種工作狀態計數並換算為一位小數百分比。 */
    private List<StatusBucket> statusBuckets(List<ManagerTaskSource> sources) {
        Map<WorkStatus, Long> counts = new EnumMap<>(WorkStatus.class);
        for (WorkStatus status : WorkStatus.values()) {
            counts.put(status, 0L);
        }
        for (ManagerTaskSource source : sources) {
            WorkStatus status = source.workStatus() == null ? WorkStatus.PENDING : source.workStatus();
            counts.compute(status, (ignored, count) -> count + 1);
        }

        // 最後一個狀態吸收四捨五入差額，使有資料時圖例百分比精確合計 100%。
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

    /** 建立單一工作狀態篩選選項。 */
    private WorkStatusOption statusOption(WorkStatus status) {
        return new WorkStatusOption(status.name(), STATUS_LABELS.get(status));
    }

    /** 將百分比四捨五入至一位小數。 */
    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
