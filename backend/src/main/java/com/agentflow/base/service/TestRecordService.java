package com.agentflow.base.service;

import com.agentflow.base.dao.TestRecordDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.TestRecord;
import com.agentflow.base.model.dto.TestDtos.TestRequest;
import com.agentflow.base.model.dto.TestDtos.TestResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestRecordService {
    private static final Logger log = LoggerFactory.getLogger(TestRecordService.class);
    private final TestRecordDao dao;
    public TestRecordService(TestRecordDao dao) { this.dao = dao; }
    @Transactional(readOnly = true) public List<TestResponse> findAll() { log.info("查詢 test 資料"); return dao.findAll().stream().map(this::toResponse).toList(); }
    public TestResponse create(TestRequest request) { log.info("新增 test 資料 {}", request.name()); return toResponse(dao.save(new TestRecord(request.name(), request.description(), request.testStatus()))); }
    public TestResponse update(UUID id, TestRequest request) { var record = get(id); record.update(request.name(), request.description(), request.testStatus()); log.info("更新 test 資料 {}", id); return toResponse(record); }
    public void delete(UUID id) { dao.delete(get(id)); log.info("刪除 test 資料 {}", id); }
    private TestRecord get(UUID id) { return dao.findById(id).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到測試資料。")); }
    private TestResponse toResponse(TestRecord row) { return new TestResponse(row.getId(), row.getName(), row.getDescription(), row.getTestStatus()); }
}

