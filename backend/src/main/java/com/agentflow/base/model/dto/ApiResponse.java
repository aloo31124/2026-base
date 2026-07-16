package com.agentflow.base.model.dto;

import java.time.OffsetDateTime;

public record ApiResponse<T>(boolean success, String message, T data, OffsetDateTime timestamp) {
    public static <T> ApiResponse<T> ok(String message, T data) { return new ApiResponse<>(true, message, data, OffsetDateTime.now()); }
    public static ApiResponse<Void> error(String message) { return new ApiResponse<>(false, message, null, OffsetDateTime.now()); }
}

