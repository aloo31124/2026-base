package com.agentflow.base.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

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
}
