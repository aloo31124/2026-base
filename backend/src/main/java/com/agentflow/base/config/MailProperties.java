package com.agentflow.base.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
    String sender,
    boolean mockEnabled,
    String testCode
) {
}
