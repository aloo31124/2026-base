package com.agentflow.base.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class TestDtos {
    private TestDtos() {}
    public record TestRequest(@NotBlank String name, @NotBlank String description, @NotBlank String testStatus) {}
    public record TestResponse(UUID id, String name, String description, String testStatus) {}
}

