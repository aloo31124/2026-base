package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.service.SmtpVerificationEmailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email/test-code")
@ConditionalOnProperty(name="app.email.capture-enabled", havingValue="true")
public class TestEmailController {
    private final SmtpVerificationEmailSender sender;
    public TestEmailController(SmtpVerificationEmailSender sender) { this.sender = sender; }
    @GetMapping public ApiResponse<String> code(@RequestParam String email, @RequestParam String purpose) {
        return ApiResponse.ok("測試驗證碼已取得。", sender.capturedCode(email, purpose));
    }
}
