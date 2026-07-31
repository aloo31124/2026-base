package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.AssigneeResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.AttachmentRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.AttachmentResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.CompanyBindingRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.ContextResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.EmployeeBindingRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.EmployeeBindingResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.EmployeeResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.ExtensionRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.ProgressRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.ReturnRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.TaskRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.TaskResponse;
import com.agentflow.base.service.TaskAssignmentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
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
@RequestMapping("/api/task-assignment")
public class TaskAssignmentController {
    private final TaskAssignmentService service;

    /** 注入任務指派業務服務。 */
    public TaskAssignmentController(TaskAssignmentService service) {
        this.service = service;
    }

    /** 取得登入者情境。 */
    @GetMapping("/context")
    public ApiResponse<ContextResponse> context(Principal principal) {
        return ApiResponse.ok("任務指派情境查詢成功。", service.context(principal.getName()));
    }

    /** 依公司名稱綁定登入者。 */
    @PostMapping("/company-bindings")
    public ApiResponse<ContextResponse> bindCompany(
        Principal principal,
        @Valid @RequestBody CompanyBindingRequest request
    ) {
        return ApiResponse.ok("公司綁定成功。", service.bindCurrentUserCompany(principal.getName(), request));
    }

    /** 依信箱搜尋同公司員工。 */
    @GetMapping("/employees")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<EmployeeResponse>> employees(
        Principal principal,
        @RequestParam(defaultValue = "") String email
    ) {
        return ApiResponse.ok("員工搜尋成功。", service.findEmployees(principal.getName(), email));
    }

    /** 查詢目前主管的員工綁定。 */
    @GetMapping("/employee-bindings")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<EmployeeBindingResponse>> employeeBindings(Principal principal) {
        return ApiResponse.ok("主管員工綁定查詢成功。", service.findEmployeeBindings(principal.getName()));
    }

    /** 建立主管員工綁定。 */
    @PostMapping("/employee-bindings")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<EmployeeBindingResponse> bindEmployee(
        Principal principal,
        @Valid @RequestBody EmployeeBindingRequest request
    ) {
        return ApiResponse.ok("主管員工綁定成功。", service.bindEmployee(principal.getName(), request));
    }

    /** 取消主管員工綁定。 */
    @DeleteMapping("/employee-bindings/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Void> unbindEmployee(Principal principal, @PathVariable UUID id) {
        service.unbindEmployee(principal.getName(), id);
        return ApiResponse.ok("主管員工綁定已取消。", null);
    }

    /** 查詢可指派對象。 */
    @GetMapping("/assignees")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<AssigneeResponse>> assignees(Principal principal) {
        return ApiResponse.ok("受派人查詢成功。", service.findAssignees(principal.getName()));
    }

    /** 查詢目前主管建立的任務。 */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<TaskResponse>> tasks(
        Principal principal,
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "") String assignee,
        @RequestParam(required = false) Instant assignedFrom,
        @RequestParam(required = false) Instant assignedTo,
        @RequestParam(required = false) Instant deadlineFrom,
        @RequestParam(required = false) Instant deadlineTo,
        @RequestParam(defaultValue = "assignedAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return ApiResponse.ok("任務查詢成功。", service.findTasks(
            principal.getName(), name, assignee, assignedFrom, assignedTo,
            deadlineFrom, deadlineTo, sortBy, direction
        ));
    }

    /** 建立任務。 */
    @PostMapping("/tasks")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<TaskResponse> createTask(Principal principal, @Valid @RequestBody TaskRequest request) {
        return ApiResponse.ok("任務指派成功。", service.createTask(principal.getName(), request));
    }

    /** 修改任務。 */
    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<TaskResponse> updateTask(
        Principal principal,
        @PathVariable UUID id,
        @Valid @RequestBody TaskRequest request
    ) {
        return ApiResponse.ok("任務更新成功。", service.updateTask(principal.getName(), id, request));
    }

    /** 刪除任務。 */
    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Void> deleteTask(Principal principal, @PathVariable UUID id) {
        service.deleteTask(principal.getName(), id);
        return ApiResponse.ok("任務刪除成功。", null);
    }

    /** 撤回任務。 */
    @PostMapping("/tasks/{id}/withdraw")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<TaskResponse> withdrawTask(Principal principal, @PathVariable UUID id) {
        return ApiResponse.ok("任務已撤回。", service.withdrawTask(principal.getName(), id));
    }

    /** 查詢登入者收件匣。 */
    @GetMapping("/inbox")
    public ApiResponse<List<TaskResponse>> inbox(
        Principal principal,
        @RequestParam(defaultValue = "") String name,
        @RequestParam(required = false) Instant assignedFrom,
        @RequestParam(required = false) Instant assignedTo,
        @RequestParam(required = false) Instant deadlineFrom,
        @RequestParam(required = false) Instant deadlineTo,
        @RequestParam(defaultValue = "assignedAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return ApiResponse.ok("我的任務查詢成功。", service.findInbox(
            principal.getName(), name, assignedFrom, assignedTo, deadlineFrom, deadlineTo, sortBy, direction
        ));
    }

    /** 查詢登入者收到的單筆任務。 */
    @GetMapping("/inbox/{id}")
    public ApiResponse<TaskResponse> inboxTask(Principal principal, @PathVariable UUID id) {
        return ApiResponse.ok("我的任務明細查詢成功。", service.inboxTask(principal.getName(), id));
    }

    /** 更新任務工作進度。 */
    @PutMapping("/tasks/{id}/progress")
    public ApiResponse<TaskResponse> updateProgress(Principal principal, @PathVariable UUID id, @Valid @RequestBody ProgressRequest request) {
        return ApiResponse.ok("工作進度更新成功。", service.updateProgress(principal.getName(), id, request));
    }

    /** 新增任務附件。 */
    @PostMapping("/tasks/{id}/attachments")
    public ApiResponse<AttachmentResponse> addAttachment(Principal principal, @PathVariable UUID id, @Valid @RequestBody AttachmentRequest request) {
        return ApiResponse.ok("任務附件上傳成功。", service.addAttachment(principal.getName(), id, request));
    }

    /** 提交任務供原指派者審核。 */
    @PostMapping("/tasks/{id}/submit")
    public ApiResponse<TaskResponse> submit(Principal principal, @PathVariable UUID id) {
        return ApiResponse.ok("任務已提交審核。", service.submitForReview(principal.getName(), id));
    }

    /** 申請任務延期。 */
    @PostMapping("/tasks/{id}/extension-requests")
    public ApiResponse<TaskResponse> requestExtension(Principal principal, @PathVariable UUID id, @Valid @RequestBody ExtensionRequest request) {
        return ApiResponse.ok("延期申請已送出。", service.requestExtension(principal.getName(), id, request));
    }

    /** 退回任務。 */
    @PostMapping("/tasks/{id}/return")
    public ApiResponse<TaskResponse> returnTask(
        Principal principal,
        @PathVariable UUID id,
        @Valid @RequestBody ReturnRequest request
    ) {
        return ApiResponse.ok("任務已退回。", service.returnTask(principal.getName(), id, request));
    }
}
