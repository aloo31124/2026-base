package com.agentflow.base.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TaskAssignmentDtos {
    private TaskAssignmentDtos() {
    }

    public record ContextResponse(UUID userId, String username, List<String> roles, UUID companyId, String companyName) {}
    public record CompanyBindingRequest(@NotBlank @Size(max = 120) String companyName) {}
    public record EmployeeResponse(UUID userId, String fullName, String username, String email, UUID bindingId) {}
    public record EmployeeBindingRequest(@NotNull UUID employeeId) {}
    public record EmployeeBindingResponse(UUID id, UUID supervisorId, String supervisorName, UUID employeeId, String employeeName, String employeeEmail) {}
    public record AssigneeResponse(UUID userId, String fullName, String username, String type) {}
    public record TaskRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 4000) String content,
        @NotNull @Future Instant deadline,
        @NotNull UUID assigneeId
    ) {}
    public record ReturnRequest(@NotBlank @Size(max = 500) String reason) {}
    public record TaskResponse(
        UUID id,
        String name,
        String content,
        Instant deadline,
        UUID creatorId,
        String creatorName,
        UUID assigneeId,
        String assigneeName,
        String assigneeUsername,
        Instant assignedAt,
        String status,
        String returnReason,
        Instant returnedAt,
        Instant createdAt,
        Instant updatedAt
    ) {}
}
