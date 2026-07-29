package com.agentflow.base.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.line-oauth")
public class LineOAuthProperties {
    private String channelId = "";
    private String channelSecret = "";
    private String callbackUrl = "http://localhost:5173/api/auth/line/callback";
    private String mockProviderBaseUrl = "http://localhost:8080";
    private long attemptTtlMinutes = 10;

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getChannelSecret() { return channelSecret; }
    public void setChannelSecret(String channelSecret) { this.channelSecret = channelSecret; }
    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
    public String getMockProviderBaseUrl() { return mockProviderBaseUrl; }
    public void setMockProviderBaseUrl(String mockProviderBaseUrl) { this.mockProviderBaseUrl = mockProviderBaseUrl; }
    public long getAttemptTtlMinutes() { return attemptTtlMinutes; }
    public void setAttemptTtlMinutes(long attemptTtlMinutes) { this.attemptTtlMinutes = attemptTtlMinutes; }
}
