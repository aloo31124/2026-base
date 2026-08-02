package com.agentflow.base.model.dto;

import com.agentflow.base.model.bo.RegistrationRecord;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class RegistrationManagementDtos {
    private RegistrationManagementDtos() {
    }

    public record PasswordPolicyRequest(
        @Min(8) @Max(72) int minLength,
        @NotNull Boolean requireLetter,
        @NotNull Boolean requireNumber
    ) {
    }

    public record PasswordPolicyResponse(
        int minLength,
        boolean requireLetter,
        boolean requireNumber,
        Instant updatedAt
    ) {
    }

    public record SessionTimeoutPolicyRequest(
        @Min(5) @Max(1440) int timeoutMinutes
    ) {
    }

    public record SessionTimeoutPolicyResponse(
        int timeoutMinutes,
        Instant updatedAt
    ) {
    }

    public record RegistrationRecordResponse(
        UUID id,
        RegistrationRecord.Method method,
        String identifier,
        boolean success,
        Instant completedAt
    ) {
    }
}
