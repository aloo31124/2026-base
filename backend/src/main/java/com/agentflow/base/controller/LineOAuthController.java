package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.AuthDtos.LoginResponse;
import com.agentflow.base.model.dto.LineOAuthDtos.AuthorizeResponse;
import com.agentflow.base.model.dto.LineOAuthDtos.CallbackRequest;
import com.agentflow.base.service.LineOAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/line")
public class LineOAuthController {
    private final LineOAuthService service;

    public LineOAuthController(LineOAuthService service) {
        this.service = service;
    }

    @GetMapping("/authorize")
    public ApiResponse<AuthorizeResponse> authorize() {
        return ApiResponse.ok("LINE 授權網址建立成功。", service.authorize());
    }

    @PostMapping("/callback")
    public ApiResponse<LoginResponse> callback(@Valid @RequestBody CallbackRequest request) {
        return ApiResponse.ok("LINE 登入成功。", service.callback(request));
    }
}
