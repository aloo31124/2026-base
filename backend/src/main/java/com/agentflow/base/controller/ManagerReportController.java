package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.ManagerReportDtos.ManagerReport;
import com.agentflow.base.model.dto.ManagerReportDtos.ManagerReportFilters;
import com.agentflow.base.service.ManagerReportService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/reports")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerReportController {
    private final ManagerReportService service;

    /**
     * 注入主管報表業務服務。
     *
     * @param service 主管報表服務
     */
    public ManagerReportController(ManagerReportService service) {
        this.service = service;
    }

    /**
     * 取得登入主管的公司、執行者、狀態與預設日期選項。
     *
     * @param principal 登入身分
     * @return 標準篩選選項 response
     */
    @GetMapping("/filters")
    public ApiResponse<ManagerReportFilters> filters(Principal principal) {
        return ApiResponse.ok("主管報表篩選選項查詢成功。", service.filters(principal.getName()));
    }

    /**
     * 取得公司摘要與目前主管自己指派的趨勢及狀態比例。
     *
     * @param principal 登入身分
     * @param assigneeId 可選執行者識別
     * @param workStatus 可選工作執行狀態
     * @param from 可選含起日
     * @param to 可選含迄日
     * @return 標準綜合主管報表 response
     */
    @GetMapping("/report")
    public ApiResponse<ManagerReport> report(
        Principal principal,
        @RequestParam(required = false) UUID assigneeId,
        @RequestParam(required = false) String workStatus,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(
            "主管報表查詢成功。",
            service.report(principal.getName(), assigneeId, workStatus, from, to)
        );
    }
}
