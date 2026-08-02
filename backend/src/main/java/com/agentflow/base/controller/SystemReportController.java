package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.SystemReportDtos.CompanyOption;
import com.agentflow.base.model.dto.SystemReportDtos.TaskTrendReport;
import com.agentflow.base.service.SystemReportService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system-reports")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class SystemReportController {
    private final SystemReportService service;

    /**
     * 注入系統報表業務服務。
     *
     * @param service 系統報表服務
     */
    public SystemReportController(SystemReportService service) {
        this.service = service;
    }

    /**
     * 取得公司篩選選項。
     *
     * @return 標準公司選項 response
     */
    @GetMapping("/companies")
    public ApiResponse<List<CompanyOption>> companies() {
        return ApiResponse.ok("公司選項查詢成功。", service.companies());
    }

    /**
     * 取得公司任務量按日趨勢。
     *
     * @param companyId 可選公司識別
     * @param from 可選含起日
     * @param to 可選含迄日
     * @return 標準任務趨勢 response
     */
    @GetMapping("/task-trend")
    public ApiResponse<TaskTrendReport> taskTrend(
        @RequestParam(required = false) UUID companyId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok("任務趨勢查詢成功。", service.taskTrend(companyId, from, to));
    }
}
