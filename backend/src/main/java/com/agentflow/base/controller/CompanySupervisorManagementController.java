package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.BindingRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.BindingResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.CompanyRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.CompanyResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.EmployeeBindingRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.EmployeeBindingResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.SupervisorCreateRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.SupervisorResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.SupervisorUpdateRequest;
import com.agentflow.base.service.CompanySupervisorManagementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/company-supervisor-management")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class CompanySupervisorManagementController {
    private final CompanySupervisorManagementService service;

    /**
     * 注入公司主管管理業務服務。
     */
    public CompanySupervisorManagementController(CompanySupervisorManagementService service) {
        this.service = service;
    }

    /**
     * 查詢公司列表。
     */
    @GetMapping("/companies")
    public ApiResponse<List<CompanyResponse>> findCompanies(
        @RequestParam(defaultValue = "") String name
    ) {
        return ApiResponse.ok("公司查詢成功。", service.findCompanies(name));
    }

    /**
     * 新增公司。
     */
    @PostMapping("/companies")
    public ApiResponse<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        return ApiResponse.ok("公司建立成功。", service.createCompany(request));
    }

    /**
     * 修改公司。
     */
    @PutMapping("/companies/{id}")
    public ApiResponse<CompanyResponse> updateCompany(
        @PathVariable UUID id,
        @Valid @RequestBody CompanyRequest request
    ) {
        return ApiResponse.ok("公司更新成功。", service.updateCompany(id, request));
    }

    /**
     * 刪除公司。
     */
    @DeleteMapping("/companies/{id}")
    public ApiResponse<Void> deleteCompany(@PathVariable UUID id) {
        service.deleteCompany(id);
        return ApiResponse.ok("公司刪除成功。", null);
    }

    /**
     * 查詢主管列表。
     */
    @GetMapping("/supervisors")
    public ApiResponse<List<SupervisorResponse>> findSupervisors(
        @RequestParam(defaultValue = "") String keyword
    ) {
        return ApiResponse.ok("主管查詢成功。", service.findSupervisors(keyword));
    }

    /**
     * 由既有使用者建立主管。
     */
    @PostMapping("/supervisors")
    public ApiResponse<SupervisorResponse> createSupervisor(
        @Valid @RequestBody SupervisorCreateRequest request
    ) {
        return ApiResponse.ok("主管建立成功。", service.createSupervisor(request));
    }

    /**
     * 修改主管職稱。
     */
    @PutMapping("/supervisors/{id}")
    public ApiResponse<SupervisorResponse> updateSupervisor(
        @PathVariable UUID id,
        @Valid @RequestBody SupervisorUpdateRequest request
    ) {
        return ApiResponse.ok("主管更新成功。", service.updateSupervisor(id, request));
    }

    /**
     * 刪除主管身分。
     */
    @DeleteMapping("/supervisors/{id}")
    public ApiResponse<Void> deleteSupervisor(@PathVariable UUID id) {
        service.deleteSupervisor(id);
        return ApiResponse.ok("主管刪除成功。", null);
    }

    /**
     * 查詢公司主管綁定。
     */
    @GetMapping("/bindings")
    public ApiResponse<List<BindingResponse>> findBindings(
        @RequestParam(defaultValue = "") String companyName,
        @RequestParam(defaultValue = "") String supervisorName
    ) {
        return ApiResponse.ok("公司主管綁定查詢成功。", service.findBindings(companyName, supervisorName));
    }

    /**
     * 建立公司主管綁定。
     */
    @PostMapping("/bindings")
    public ApiResponse<BindingResponse> createBinding(@Valid @RequestBody BindingRequest request) {
        return ApiResponse.ok("公司主管綁定成功。", service.createBinding(request));
    }

    /**
     * 取消公司主管綁定。
     */
    @DeleteMapping("/bindings/{id}")
    public ApiResponse<Void> deleteBinding(@PathVariable UUID id) {
        service.deleteBinding(id);
        return ApiResponse.ok("公司主管綁定已取消。", null);
    }

    /**
     * 查詢公司員工綁定。
     */
    @GetMapping("/employee-bindings")
    public ApiResponse<List<EmployeeBindingResponse>> findEmployeeBindings(
        @RequestParam(defaultValue = "") String companyName,
        @RequestParam(defaultValue = "") String employeeName
    ) {
        return ApiResponse.ok(
            "公司員工綁定查詢成功。",
            service.findEmployeeBindings(companyName, employeeName)
        );
    }

    /**
     * 建立公司員工綁定。
     */
    @PostMapping("/employee-bindings")
    public ApiResponse<EmployeeBindingResponse> createEmployeeBinding(
        @Valid @RequestBody EmployeeBindingRequest request
    ) {
        return ApiResponse.ok("公司員工綁定成功。", service.createEmployeeBinding(request));
    }

    /**
     * 取消公司員工綁定。
     */
    @DeleteMapping("/employee-bindings/{id}")
    public ApiResponse<Void> deleteEmployeeBinding(@PathVariable UUID id) {
        service.deleteEmployeeBinding(id);
        return ApiResponse.ok("公司員工綁定已取消。", null);
    }
}
