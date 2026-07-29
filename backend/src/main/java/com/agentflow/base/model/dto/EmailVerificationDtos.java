package com.agentflow.base.model.dto;

import com.agentflow.base.model.bo.EmailDeliveryLog;
import com.agentflow.base.model.bo.EmailVerification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class EmailVerificationDtos {
    private EmailVerificationDtos() {
    }

    public record SendVerificationMailRequest(
        @NotBlank
        @Email
        @Size(max = 160)
        String email
    ) {
    }

    public record SendVerificationMailResponse(
        String maskedRecipient,
        OffsetDateTime sentAt
    ) {
    }

    public record VerifyCodeRequest(
        @NotBlank
        @Email
        @Size(max = 160)
        String email,
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "必須為 6 位數字")
        String code,
        @NotNull
        EmailVerification.Purpose purpose
    ) {
    }

    public record VerificationTicketResponse(
        UUID ticketId,
        EmailVerification.Purpose purpose,
        Instant expiresAt
    ) {
    }

    public record CompleteCredentialRequest(
        @NotBlank
        @Email
        @Size(max = 160)
        String email,
        @NotNull
        UUID ticketId,
        @NotBlank
        @Size(min = 8, max = 72)
        String password,
        @NotBlank
        @Size(min = 8, max = 72)
        String confirmPassword
    ) {
    }

    public record DeliveryLogResponse(
        UUID id,
        String maskedRecipient,
        EmailDeliveryLog.Purpose purpose,
        EmailDeliveryLog.Status status,
        String errorSummary,
        Instant completedAt
    ) {
    }
}
