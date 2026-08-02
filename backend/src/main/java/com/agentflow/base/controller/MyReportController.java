package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.MyReportDtos.MyReport;
import com.agentflow.base.model.dto.MyReportDtos.MyReportFilters;
import com.agentflow.base.service.MyReportService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my/reports")
public class MyReportController {
    private final MyReportService service;

    /**
     * 注入我的報表業務服務。
     *
     * @param service 我的報表服務
     */
    public MyReportController(MyReportService service) {
        this.service = service;
    }

    /**
     * 取得登入員工本人、狀態與預設日期選項。
     *
     * @param principal 登入身分
     * @return 標準篩選選項 response
     */
    @GetMapping("/filters")
    public ApiResponse<MyReportFilters> filters(Principal principal) {
        return ApiResponse.ok("我的報表篩選選項查詢成功。", service.filters(principal.getName()));
    }

    /**
     * 取得登入員工自己的任務總覽、趨勢與狀態比例。
     *
     * @param principal 登入身分
     * @param assigneeId 可選執行者識別，只能指定本人
     * @param workStatus 可選工作執行狀態
     * @param from 可選含起日
     * @param to 可選含迄日
     * @return 標準綜合我的報表 response
     */
    @GetMapping("/report")
    public ApiResponse<MyReport> report(
        Principal principal,
        @RequestParam(required = false) UUID assigneeId,
        @RequestParam(required = false) String workStatus,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(
            "我的報表查詢成功。",
            service.report(principal.getName(), assigneeId, workStatus, from, to)
        );
    }
}
