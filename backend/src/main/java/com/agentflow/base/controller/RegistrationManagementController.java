package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.RegistrationManagementDtos.PasswordPolicyRequest;
import com.agentflow.base.model.dto.RegistrationManagementDtos.PasswordPolicyResponse;
import com.agentflow.base.model.dto.RegistrationManagementDtos.RegistrationRecordResponse;
import com.agentflow.base.model.dto.RegistrationManagementDtos.SessionTimeoutPolicyRequest;
import com.agentflow.base.model.dto.RegistrationManagementDtos.SessionTimeoutPolicyResponse;
import com.agentflow.base.service.RegistrationManagementService;
import com.agentflow.base.service.SessionTimeoutPolicyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/registration-management")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class RegistrationManagementController {
    private final RegistrationManagementService service;
    private final SessionTimeoutPolicyService sessionTimeoutPolicyService;

    public RegistrationManagementController(
        RegistrationManagementService service,
        SessionTimeoutPolicyService sessionTimeoutPolicyService
    ) {
        this.service = service;
        this.sessionTimeoutPolicyService = sessionTimeoutPolicyService;
    }

    /**
     * 取得目前密碼政策。
     */
    @GetMapping("/policy")
    public ApiResponse<PasswordPolicyResponse> getPolicy() {
        return ApiResponse.ok("密碼政策查詢成功。", service.getPolicy());
    }

    /**
     * 更新目前密碼政策。
     */
    @PutMapping("/policy")
    public ApiResponse<PasswordPolicyResponse> updatePolicy(
        @Valid @RequestBody PasswordPolicyRequest request
    ) {
        return ApiResponse.ok("密碼政策更新成功。", service.updatePolicy(request));
    }

    /**
     * 取得後續新登入使用的 JWT 效期。
     */
    @GetMapping("/session-timeout")
    public ApiResponse<SessionTimeoutPolicyResponse> getSessionTimeoutPolicy() {
        return ApiResponse.ok("登出時間設定查詢成功。", sessionTimeoutPolicyService.getPolicy());
    }

    /**
     * 更新後續新登入使用的 JWT 效期。
     */
    @PutMapping("/session-timeout")
    public ApiResponse<SessionTimeoutPolicyResponse> updateSessionTimeoutPolicy(
        @Valid @RequestBody SessionTimeoutPolicyRequest request
    ) {
        return ApiResponse.ok("登出時間設定更新成功。", sessionTimeoutPolicyService.updatePolicy(request));
    }

    /**
     * 取得最近註冊紀錄。
     */
    @GetMapping("/registrations")
    public ApiResponse<List<RegistrationRecordResponse>> registrations() {
        return ApiResponse.ok("註冊紀錄查詢成功。", service.findRecentRegistrations());
    }
}
