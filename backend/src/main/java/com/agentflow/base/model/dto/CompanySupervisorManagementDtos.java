package com.agentflow.base.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class CompanySupervisorManagementDtos {
    /**
     * 禁止建立純工具類別。
     */
    private CompanySupervisorManagementDtos() {
    }

    public record CompanyRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description
    ) {
    }

    public record CompanyResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
    ) {
    }

    public record SupervisorCreateRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 80) String title
    ) {
    }

    public record SupervisorUpdateRequest(
        @NotBlank @Size(max = 80) String title
    ) {
    }

    public record SupervisorResponse(
        UUID id,
        UUID userId,
        String fullName,
        String username,
        String email,
        String title,
        UUID companyId,
        String companyName,
        Instant createdAt,
        Instant updatedAt
    ) {
    }

    public record BindingRequest(
        @NotNull UUID companyId,
        @NotNull UUID supervisorId
    ) {
    }

    public record BindingResponse(
        UUID id,
        UUID companyId,
        String companyName,
        UUID supervisorId,
        UUID userId,
        String supervisorName,
        String supervisorUsername,
        String title,
        Instant createdAt
    ) {
    }

    public record EmployeeBindingRequest(
        @NotNull UUID companyId,
        @NotNull UUID userId
    ) {
    }

    public record EmployeeBindingResponse(
        UUID id,
        UUID companyId,
        String companyName,
        UUID userId,
        String employeeName,
        String employeeUsername,
        String employeeEmail,
        Instant createdAt
    ) {
    }
}
