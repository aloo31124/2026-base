package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.AuthDtos.LoginResponse;
import com.agentflow.base.model.dto.EmailVerificationDtos.CompleteCredentialRequest;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailRequest;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailResponse;
import com.agentflow.base.model.dto.EmailVerificationDtos.VerificationTicketResponse;
import com.agentflow.base.model.dto.EmailVerificationDtos.VerifyCodeRequest;
import com.agentflow.base.service.EmailRegistrationService;
import com.agentflow.base.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email")
public class EmailAuthController {
    private final EmailVerificationService verificationService;
    private final EmailRegistrationService registrationService;

    public EmailAuthController(
        EmailVerificationService verificationService,
        EmailRegistrationService registrationService
    ) {
        this.verificationService = verificationService;
        this.registrationService = registrationService;
    }

    /**
     * 寄送首次註冊驗證碼。
     */
    @PostMapping("/registration-code")
    public ApiResponse<SendVerificationMailResponse> registrationCode(
        @Valid @RequestBody SendVerificationMailRequest request
    ) {
        return ApiResponse.ok(
            "註冊驗證碼已寄送。",
            verificationService.requestRegistrationCode(request.email())
        );
    }

    /**
     * 寄送忘記密碼驗證碼。
     */
    @PostMapping("/password-reset-code")
    public ApiResponse<SendVerificationMailResponse> passwordResetCode(
        @Valid @RequestBody SendVerificationMailRequest request
    ) {
        return ApiResponse.ok(
            "密碼重設驗證碼已寄送。",
            verificationService.requestPasswordResetCode(request.email())
        );
    }

    /**
     * 核銷驗證碼並回傳一次性票券。
     */
    @PostMapping("/verify")
    public ApiResponse<VerificationTicketResponse> verify(
        @Valid @RequestBody VerifyCodeRequest request
    ) {
        return ApiResponse.ok(
            "信箱驗證成功。",
            verificationService.verify(request.email(), request.code(), request.purpose())
        );
    }

    /**
     * 完成信箱註冊並直接回傳登入工作階段。
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(
        @Valid @RequestBody CompleteCredentialRequest request
    ) {
        return ApiResponse.ok(
            "信箱註冊成功。",
            registrationService.register(
                request.email(),
                request.ticketId(),
                request.password(),
                request.confirmPassword()
            )
        );
    }

    /**
     * 以已驗證票券更新信箱帳號密碼。
     */
    @PostMapping("/password-reset")
    public ApiResponse<Void> passwordReset(
        @Valid @RequestBody CompleteCredentialRequest request
    ) {
        registrationService.resetPassword(
            request.email(),
            request.ticketId(),
            request.password(),
            request.confirmPassword()
        );
        return ApiResponse.ok("密碼已更新。", null);
    }
}
