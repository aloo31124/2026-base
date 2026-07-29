package com.agentflow.base.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class EmailAuthDtos {
    private EmailAuthDtos() {}
    public record CodeRequest(@NotBlank @Email String email) {}
    public record CodeResponse(Instant expiresAt, Instant resendAvailableAt) {}
    public record RegistrationRequest(@NotBlank @Size(max=80) String fullName, @NotBlank @Email String email,
        @NotBlank String password, @NotBlank @Size(min=6,max=6) String code) {}
    public record PasswordResetRequest(@NotBlank @Email String email, @NotBlank String newPassword,
        @NotBlank @Size(min=6,max=6) String code) {}
}
