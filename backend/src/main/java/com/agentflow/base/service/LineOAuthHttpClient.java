package com.agentflow.base.service;

import com.agentflow.base.config.LineOAuthProperties;
import com.agentflow.base.exception.BusinessException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "app.line-oauth.mock-enabled", havingValue = "false", matchIfMissing = true)
public class LineOAuthHttpClient implements LineOAuthClient {
    private static final URI TOKEN_URI = URI.create("https://api.line.me/oauth2/v2.1/token");
    private static final URI VERIFY_ID_TOKEN_URI = URI.create("https://api.line.me/oauth2/v2.1/verify");
    private final LineOAuthProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public LineOAuthHttpClient(LineOAuthProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    }

    @Override
    public String buildAuthorizationUrl(String state, String nonce, String codeChallenge) {
        validateConfiguration();
        Map<String, String> query = new LinkedHashMap<>();
        query.put("response_type", "code");
        query.put("client_id", properties.getChannelId());
        query.put("redirect_uri", properties.getCallbackUrl());
        query.put("state", state);
        query.put("scope", "profile openid email");
        query.put("nonce", nonce);
        query.put("code_challenge", codeChallenge);
        query.put("code_challenge_method", "S256");
        return "https://access.line.me/oauth2/v2.1/authorize?" + form(query);
    }

    @Override
    public Profile exchangeAndVerify(String code, String codeVerifier, String nonce) {
        validateConfiguration();
        Map<String, String> tokenForm = new LinkedHashMap<>();
        tokenForm.put("grant_type", "authorization_code");
        tokenForm.put("code", code);
        tokenForm.put("redirect_uri", properties.getCallbackUrl());
        tokenForm.put("client_id", properties.getChannelId());
        tokenForm.put("client_secret", properties.getChannelSecret());
        tokenForm.put("code_verifier", codeVerifier);
        JsonNode token = postForm(TOKEN_URI, tokenForm);
        String idToken = requiredText(token, "id_token");

        Map<String, String> verifyForm = new LinkedHashMap<>();
        verifyForm.put("id_token", idToken);
        verifyForm.put("client_id", properties.getChannelId());
        verifyForm.put("nonce", nonce);
        JsonNode verified = postForm(VERIFY_ID_TOKEN_URI, verifyForm);
        return new Profile(
            requiredText(verified, "sub"),
            requiredText(verified, "name"),
            optionalText(verified, "picture"),
            optionalText(verified, "email")
        );
    }

    private JsonNode postForm(URI uri, Map<String, String> values) {
        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(12))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form(values)))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "LINE 驗證服務回應失敗，請稍後再試。");
            }
            return mapper.readTree(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "LINE 驗證流程已中斷，請重新登入。");
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof BusinessException business) throw business;
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "LINE 驗證服務暫時無法使用。");
        }
    }

    private String requiredText(JsonNode node, String field) {
        String value = optionalText(node, field);
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "LINE 驗證資料不完整，請重新登入。");
        }
        return value;
    }

    private String optionalText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private void validateConfiguration() {
        if (properties.getChannelId().isBlank() || properties.getChannelSecret().isBlank() || properties.getCallbackUrl().isBlank()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "LINE Login 尚未完成服務設定。");
        }
    }

    private static String form(Map<String, String> values) {
        return values.entrySet().stream()
            .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
            .reduce((left, right) -> left + "&" + right)
            .orElse("");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
