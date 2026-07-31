package com.agentflow.base;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskAssignmentIntegrationTest {
    private static final String API = "/api/task-assignment";
    private static final String ADMIN_API = "/api/admin/company-supervisor-management";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    /**
     * 驗證公司綁定、主管員工綁定、任務 CRUD、查詢、退回與撤回完整流程。
     */
    @Test
    void managerCanAssignSearchReturnUpdateAndWithdrawTask() throws Exception {
        String adminToken = token("admin", "admin123");
        JsonNode company = createCompany(adminToken);
        JsonNode manager = createUser(adminToken, "整合主管");
        JsonNode employee = createUser(adminToken, "整合員工");
        JsonNode supervisor = createSupervisor(adminToken, manager.path("id").asText());
        createSupervisorCompanyBinding(
            adminToken,
            company.path("id").asText(),
            supervisor.path("id").asText()
        );

        String managerToken = token(manager.path("username").asText(), "password123");
        String employeeToken = token(employee.path("username").asText(), "password123");
        mvc.perform(post(API + "/company-bindings")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("companyName", company.path("name").asText()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyId", is(company.path("id").asText())));

        JsonNode employeeBinding = readData(mvc.perform(post(API + "/employee-bindings")
                .header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("employeeId", employee.path("id").asText()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.employeeEmail", is(employee.path("email").asText()))));

        mvc.perform(get(API + "/employees")
                .queryParam("email", employee.path("email").asText().toUpperCase())
                .header("Authorization", bearer(managerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].bindingId", is(employeeBinding.path("id").asText())));

        Instant deadline = Instant.now().plus(2, ChronoUnit.DAYS);
        JsonNode task = readData(mvc.perform(post(API + "/tasks")
                .header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "整合任務",
                    "content", "驗證完整流程",
                    "deadline", deadline.toString(),
                    "assigneeId", employee.path("id").asText()
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status", is("ASSIGNED"))));

        mvc.perform(get(API + "/tasks")
                .queryParam("name", "整合")
                .queryParam("assignee", employee.path("username").asText())
                .queryParam("sortBy", "deadline")
                .queryParam("direction", "asc")
                .header("Authorization", bearer(managerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].id", is(task.path("id").asText())));

        mvc.perform(get(API + "/inbox").header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name", is("整合任務")));

        mvc.perform(post(API + "/tasks/" + task.path("id").asText() + "/return")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("reason", "資訊不足"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status", is("RETURNED")))
            .andExpect(jsonPath("$.data.returnReason", is("資訊不足")));

        mvc.perform(put(API + "/tasks/" + task.path("id").asText())
                .header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "整合任務-補充",
                    "content", "已補充資訊",
                    "deadline", deadline.plus(1, ChronoUnit.DAYS).toString(),
                    "assigneeId", employee.path("id").asText()
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status", is("ASSIGNED")))
            .andExpect(jsonPath("$.data.returnReason").doesNotExist());

        mvc.perform(post(API + "/tasks/" + task.path("id").asText() + "/withdraw")
                .header("Authorization", bearer(managerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status", is("WITHDRAWN")));

        mvc.perform(delete(API + "/employee-bindings/" + employeeBinding.path("id").asText())
                .header("Authorization", bearer(managerToken)))
            .andExpect(status().isOk());
    }

    /**
     * 驗證主管不可指派跨公司使用者，非主管不可呼叫管理端點。
     */
    @Test
    void assignmentRejectsCrossCompanyAndNonManager() throws Exception {
        String employeeToken = token("user", "admin123");
        mvc.perform(get(API + "/tasks").header("Authorization", bearer(employeeToken)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("[任務指派] [api] 無主管權限。")));
    }

    /** 驗證我的任務進度、附件、提交審核與延期申請。 */
    @Test
    void employeeCanUpdateProgressAttachSubmitAndRequestExtension() throws Exception {
        String adminToken = token("admin", "admin123");
        JsonNode company = createCompany(adminToken);
        JsonNode manager = createUser(adminToken, "我的任務主管");
        JsonNode employee = createUser(adminToken, "我的任務員工");
        JsonNode supervisor = createSupervisor(adminToken, manager.path("id").asText());
        createSupervisorCompanyBinding(adminToken, company.path("id").asText(), supervisor.path("id").asText());
        String managerToken = token(manager.path("username").asText(), "password123");
        String employeeToken = token(employee.path("username").asText(), "password123");

        mvc.perform(post(API + "/company-bindings").header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("companyName", company.path("name").asText()))))
            .andExpect(status().isOk());
        mvc.perform(post(API + "/employee-bindings").header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("employeeId", employee.path("id").asText()))))
            .andExpect(status().isOk());

        JsonNode task = readData(mvc.perform(post(API + "/tasks").header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                    "name", "我的任務整合測試", "content", "原始工作內容",
                    "deadline", Instant.now().plus(2, ChronoUnit.DAYS).toString(),
                    "assigneeId", employee.path("id").asText()))))
            .andExpect(status().isOk()));
        String taskPath = API + "/tasks/" + task.path("id").asText();

        mvc.perform(get(API + "/inbox").queryParam("name", "整合").queryParam("sortBy", "deadline")
                .queryParam("direction", "asc").header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].workStatus", is("PENDING")))
            .andExpect(jsonPath("$.data[0].progressPercent", is(10)));

        mvc.perform(put(taskPath + "/progress").header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                    "workStatus", "COMPLETED", "progressContent", "已完成核心工作", "progressPercent", 80))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.workStatus", is("COMPLETED")))
            .andExpect(jsonPath("$.data.progressPercent", is(80)));

        mvc.perform(post(taskPath + "/attachments").header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                    "fileName", "evidence.txt", "contentType", "text/plain", "base64Content", "b2s="))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.fileName", is("evidence.txt")))
            .andExpect(jsonPath("$.data.fileSize", is(2)));

        mvc.perform(post(taskPath + "/submit").header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.submittedAt").isNotEmpty())
            .andExpect(jsonPath("$.data.progressPercent", is(80)));

        JsonNode extensionTask = readData(mvc.perform(post(API + "/tasks").header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                    "name", "延期任務", "content", "需要延期",
                    "deadline", Instant.now().plus(3, ChronoUnit.DAYS).toString(),
                    "assigneeId", employee.path("id").asText()))))
            .andExpect(status().isOk()));
        mvc.perform(post(API + "/tasks/" + extensionTask.path("id").asText() + "/extension-requests")
                .header("Authorization", bearer(employeeToken)).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("reason", "等待外部資料"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.extensionRequestedAt").isNotEmpty())
            .andExpect(jsonPath("$.data.extensionReason", is("等待外部資料")));
    }

    /** 透過管理 API 建立唯一公司。 */
    private JsonNode createCompany(String token) throws Exception {
        String name = "任務公司-" + UUID.randomUUID();
        return readData(mvc.perform(post(ADMIN_API + "/companies")
            .header("Authorization", bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("name", name, "description", "任務整合測試"))))
            .andExpect(status().isOk()));
    }

    /** 透過管理 API 建立唯一使用者。 */
    private JsonNode createUser(String token, String fullName) throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return readData(mvc.perform(post("/api/admin/users")
            .header("Authorization", bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "fullName", fullName,
                "username", "task." + suffix,
                "email", "task." + suffix + "@example.com",
                "password", "password123"
            ))))
            .andExpect(status().isOk()));
    }

    /** 將既有使用者建立為主管。 */
    private JsonNode createSupervisor(String token, String userId) throws Exception {
        return readData(mvc.perform(post(ADMIN_API + "/supervisors")
            .header("Authorization", bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("userId", userId, "title", "任務主管"))))
            .andExpect(status().isOk()));
    }

    /** 建立主管公司綁定。 */
    private void createSupervisorCompanyBinding(String token, String companyId, String supervisorId) throws Exception {
        mvc.perform(post(ADMIN_API + "/bindings")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("companyId", companyId, "supervisorId", supervisorId))))
            .andExpect(status().isOk());
    }

    /** 登入並取得 JWT。 */
    private String token(String username, String password) throws Exception {
        return readData(mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("username", username, "password", password))))
            .andExpect(status().isOk())).path("token").asText();
    }

    /** 讀取標準回應的 data 節點。 */
    private JsonNode readData(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        return mapper.readTree(result.andReturn().getResponse().getContentAsString()).path("data");
    }

    /** 序列化 JSON。 */
    private String json(Object value) {
        return mapper.writeValueAsString(value);
    }

    /** 建立 Bearer 值。 */
    private String bearer(String token) {
        return "Bearer " + token;
    }
}
