package com.agentflow.base.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {}
    public record UserRequest(@NotBlank String fullName, @NotBlank String username, @Email @NotBlank String email, @Size(min = 8) String password) {}
    public record UserUpdateRequest(@NotBlank String fullName, @Email @NotBlank String email, @Size(min = 8) String password) {}
    public record UserResponse(UUID id, String fullName, String username, String email, String registrationMethod, boolean active, List<String> roles) {}
    public record AssignRoleRequest(@NotBlank String roleCode) {}
}

