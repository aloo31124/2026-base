package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.UserDtos.AssignRoleRequest;
import com.agentflow.base.model.dto.UserDtos.UserRequest;
import com.agentflow.base.model.dto.UserDtos.UserResponse;
import com.agentflow.base.model.dto.UserDtos.UserUpdateRequest;
import com.agentflow.base.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }
    @GetMapping public ApiResponse<List<UserResponse>> findAll() { return ApiResponse.ok("查詢成功。", service.findAll()); }
    @PostMapping public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request) { return ApiResponse.ok("使用者建立成功。", service.create(request)); }
    @PutMapping("/{id}") public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) { return ApiResponse.ok("使用者更新成功。", service.update(id, request)); }
    @PatchMapping("/{id}/disable") public ApiResponse<UserResponse> disable(@PathVariable UUID id) { return ApiResponse.ok("使用者已停用。", service.disable(id)); }
    @PostMapping("/{id}/roles") public ApiResponse<UserResponse> assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) { return ApiResponse.ok("角色授予成功。", service.assignRole(id, request.roleCode())); }
}

