package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailRequest;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailResponse;
import com.agentflow.base.model.dto.EmailVerificationDtos.DeliveryLogResponse;
import com.agentflow.base.service.EmailDeliveryLogService;
import com.agentflow.base.service.EmailVerificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final EmailDeliveryLogService deliveryLogService;

    public EmailVerificationController(
        EmailVerificationService service,
        EmailDeliveryLogService deliveryLogService
    ) {
        this.service = service;
        this.deliveryLogService = deliveryLogService;
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

    /**
     * 取得最近二十筆寄送成功或失敗紀錄。
     */
    @GetMapping("/logs")
    public ApiResponse<List<DeliveryLogResponse>> logs() {
        return ApiResponse.ok("寄送紀錄查詢成功。", deliveryLogService.recent());
    }
}
