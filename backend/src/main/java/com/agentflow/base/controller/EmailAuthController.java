package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.AuthDtos.LoginResponse;
import com.agentflow.base.model.dto.EmailAuthDtos.CodeRequest;
import com.agentflow.base.model.dto.EmailAuthDtos.CodeResponse;
import com.agentflow.base.model.dto.EmailAuthDtos.PasswordResetRequest;
import com.agentflow.base.model.dto.EmailAuthDtos.RegistrationRequest;
import com.agentflow.base.service.EmailAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email")
public class EmailAuthController {
    private final EmailAuthService service;
    public EmailAuthController(EmailAuthService service) { this.service = service; }
    @PostMapping("/registrations/code")
    public ApiResponse<CodeResponse> registrationCode(@Valid @RequestBody CodeRequest request) {
        return ApiResponse.ok("驗證碼已寄出。", service.sendRegistrationCode(request.email()));
    }
    @PostMapping("/registrations")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return ApiResponse.ok("信箱註冊成功。", service.register(request));
    }
    @PostMapping("/password-resets/code")
    public ApiResponse<CodeResponse> resetCode(@Valid @RequestBody CodeRequest request) {
        return ApiResponse.ok("若此信箱已註冊，驗證碼將寄至該信箱。", service.sendResetCode(request.email()));
    }
    @PostMapping("/password-resets")
    public ApiResponse<Void> reset(@Valid @RequestBody PasswordResetRequest request) {
        service.resetPassword(request); return ApiResponse.ok("密碼更新成功。", null);
    }
}
