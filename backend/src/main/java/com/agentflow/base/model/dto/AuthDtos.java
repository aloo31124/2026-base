package com.agentflow.base.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, String tokenType, String username, String fullName, List<String> roles) {}
}

