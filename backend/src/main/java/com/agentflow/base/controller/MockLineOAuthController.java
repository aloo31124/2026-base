package com.agentflow.base.controller;

import com.agentflow.base.config.LineOAuthProperties;
import com.agentflow.base.dao.LineOAuthAttemptDao;
import com.agentflow.base.model.dto.ApiResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/line/mock")
@ConditionalOnProperty(name = "app.line-oauth.mock-enabled", havingValue = "true")
public class MockLineOAuthController {
    private final LineOAuthProperties properties;
    private final LineOAuthAttemptDao attemptDao;

    public MockLineOAuthController(LineOAuthProperties properties, LineOAuthAttemptDao attemptDao) {
        this.properties = properties;
        this.attemptDao = attemptDao;
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(
        @RequestParam String state,
        @RequestParam(name = "redirect_uri", required = false) String ignoredRedirectUri
    ) {
        String location = properties.getCallbackUrl() + "?code=browser-line-user&state="
            + URLEncoder.encode(state, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, URI.create(location).toString()).build();
    }

    @GetMapping("/audit")
    public ApiResponse<AuditResponse> audit(@RequestParam String state) {
        var attempt = attemptDao.findByStateHash(sha256Hex(state))
            .orElseThrow(() -> new IllegalArgumentException("找不到測試稽核紀錄。"));
        return ApiResponse.ok("LINE OAuth 測試稽核查詢成功。",
            new AuditResponse(attempt.getStatus().name(), attempt.getResultCode(), attempt.getCompletedAt() != null));
    }

    public record AuditResponse(String status, String resultCode, boolean completed) {}

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
