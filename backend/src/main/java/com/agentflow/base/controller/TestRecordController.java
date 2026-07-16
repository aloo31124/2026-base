package com.agentflow.base.controller;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.model.dto.TestDtos.TestRequest;
import com.agentflow.base.model.dto.TestDtos.TestResponse;
import com.agentflow.base.service.TestRecordService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/testTemp")
public class TestRecordController {
    private final TestRecordService service;
    public TestRecordController(TestRecordService service) { this.service = service; }
    @GetMapping public ApiResponse<List<TestResponse>> findAll() { return ApiResponse.ok("查詢成功。", service.findAll()); }
    @PostMapping public ApiResponse<TestResponse> create(@Valid @RequestBody TestRequest request) { return ApiResponse.ok("新增成功。", service.create(request)); }
    @PutMapping("/{id}") public ApiResponse<TestResponse> update(@PathVariable UUID id, @Valid @RequestBody TestRequest request) { return ApiResponse.ok("更新成功。", service.update(id, request)); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable UUID id) { service.delete(id); return ApiResponse.ok("刪除成功。", null); }
}

