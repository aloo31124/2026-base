package com.agentflow.base.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class LineOAuthDtos {
    private LineOAuthDtos() {}

    public record AuthorizeResponse(String authorizationUrl, Instant expiresAt) {}

    public record CallbackRequest(
        String code,
        @NotBlank(message = "state 不可空白") String state,
        String error,
        String errorDescription
    ) {}
}
