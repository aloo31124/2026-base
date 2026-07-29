package com.agentflow.base.service;

import com.agentflow.base.config.LineOAuthProperties;
import com.agentflow.base.exception.BusinessException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.line-oauth.mock-enabled", havingValue = "true")
public class MockLineOAuthClient implements LineOAuthClient {
    private final LineOAuthProperties properties;

    public MockLineOAuthClient(LineOAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public String buildAuthorizationUrl(String state, String nonce, String codeChallenge) {
        return properties.getMockProviderBaseUrl() + "/api/auth/line/mock/authorize?state=" + encode(state)
            + "&redirect_uri=" + encode(properties.getCallbackUrl());
    }

    @Override
    public Profile exchangeAndVerify(String code, String codeVerifier, String nonce) {
        if (codeVerifier == null || codeVerifier.length() < 43 || nonce == null || nonce.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "LINE 模擬驗證安全參數不完整。");
        }
        if ("provider-error".equals(code)) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "LINE 驗證服務回應失敗，請稍後再試。");
        }
        String suffix = code.replaceAll("[^A-Za-z0-9_-]", "");
        if (suffix.isBlank()) suffix = "default";
        return new Profile("U-MOCK-" + suffix, "LINE 測試使用者", null, null);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
