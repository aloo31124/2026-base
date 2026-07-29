package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailRequest;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailResponse;
import com.agentflow.base.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/email-verification")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class EmailVerificationController {
    private final EmailVerificationService service;

    public EmailVerificationController(EmailVerificationService service) {
        this.service = service;
    }

    /**
     * 接收管理員的測試收件地址並寄送驗證碼。
     *
     * @param request 經格式驗證的收件信箱
     * @return 遮罩收件者與寄送完成時間
     */
    @PostMapping("/send")
    public ApiResponse<SendVerificationMailResponse> send(@Valid @RequestBody SendVerificationMailRequest request) {
        return ApiResponse.ok("驗證碼信件已寄送。", service.send(request.email()));
    }
}
