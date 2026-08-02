package com.agentflow.base;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagerReportIntegrationTest {
    private static final String API = "/api/manager/reports";
    private static final String ADMIN_API = "/api/admin/company-supervisor-management";
    private static final String TASK_API = "/api/task-assignment";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    /** 讓主管報表 fixture 使用獨立 H2 資料庫，避免污染跨公司系統報表測試。 */
    @DynamicPropertySource
    static void managerReportDatabase(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            () -> "jdbc:h2:mem:manager-report;MODE=MSSQLServer;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
        );
    }

    /** 驗證公司總覽涵蓋同公司任務，而圖表只統計目前主管建立的任務。 */
    @Test
    void managerCanViewCompanyOverviewAndOwnAssignedReports() throws Exception {
        ManagerFixture fixture = createManagerFixture("主管報表公司");
        JsonNode employeeTask = createTask(
            fixture.managerToken(), fixture.employeeId(), "主管建立員工任務"
        );
        createTask(fixture.managerToken(), fixture.managerId(), "主管建立自己的任務");
        updateProgress(fixture.employeeToken(), employeeTask.path("id").asText(), "COMPLETED", 100);

        ManagerAccount peer = createPeerManager(fixture.adminToken(), fixture.companyId(), "同公司另一主管");
        createTask(peer.token(), peer.id(), "另一主管建立的任務");
        String today = LocalDate.now().toString();

        mvc.perform(get(API + "/filters").header("Authorization", bearer(fixture.managerToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyName", is(fixture.companyName())))
            .andExpect(jsonPath("$.data.assignees", hasSize(2)))
            .andExpect(jsonPath("$.data.assignees[*].id", hasItem(fixture.employeeId())))
            .andExpect(jsonPath("$.data.assignees[*].id", hasItem(fixture.managerId())))
            .andExpect(jsonPath("$.data.workStatuses", hasSize(3)));

        mvc.perform(get(API + "/report")
                .queryParam("from", today)
                .queryParam("to", today)
                .header("Authorization", bearer(fixture.managerToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyName", is(fixture.companyName())))
            .andExpect(jsonPath("$.data.companyTotalTasks", is(3)))
            .andExpect(jsonPath("$.data.managerTotalTasks", is(2)))
            .andExpect(jsonPath("$.data.trendPoints", hasSize(1)))
            .andExpect(jsonPath("$.data.trendPoints[0].taskCount", is(2)))
            .andExpect(jsonPath("$.data.statusBuckets[?(@.status == 'PENDING')].taskCount", hasItem(1)))
            .andExpect(jsonPath("$.data.statusBuckets[?(@.status == 'COMPLETED')].taskCount", hasItem(1)));
    }

    /** 驗證執行者、工作狀態、預設一年與日期範圍條件。 */
    @Test
    void managerCanFilterAssigneeStatusAndDateRange() throws Exception {
        ManagerFixture fixture = createManagerFixture("主管篩選公司");
        JsonNode task = createTask(fixture.managerToken(), fixture.employeeId(), "已完成篩選任務");
        updateProgress(fixture.employeeToken(), task.path("id").asText(), "COMPLETED", 100);
        LocalDate today = LocalDate.now();

        mvc.perform(get(API + "/report")
                .queryParam("assigneeId", fixture.employeeId())
                .queryParam("workStatus", "COMPLETED")
                .queryParam("from", today.toString())
                .queryParam("to", today.toString())
                .header("Authorization", bearer(fixture.managerToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assigneeId", is(fixture.employeeId())))
            .andExpect(jsonPath("$.data.assigneeName", is("主管報表員工")))
            .andExpect(jsonPath("$.data.workStatus", is("COMPLETED")))
            .andExpect(jsonPath("$.data.managerTotalTasks", is(1)))
            .andExpect(jsonPath("$.data.statusBuckets[?(@.status == 'PENDING')].taskCount", hasItem(0)))
            .andExpect(jsonPath("$.data.statusBuckets[?(@.status == 'COMPLETED')].percentage", hasItem(100.0)));

        mvc.perform(get(API + "/report").header("Authorization", bearer(fixture.managerToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.from", is(today.minusYears(1).toString())))
            .andExpect(jsonPath("$.data.to", is(today.toString())))
            .andExpect(jsonPath(
                "$.data.trendPoints",
                hasSize((int) ChronoUnit.DAYS.between(today.minusYears(1), today) + 1)
            ));

        mvc.perform(get(API + "/report")
                .queryParam("from", today.toString())
                .queryParam("to", today.minusDays(1).toString())
                .header("Authorization", bearer(fixture.managerToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("開始日期不得晚於結束日期。")));

        mvc.perform(get(API + "/report")
                .queryParam("workStatus", "UNKNOWN")
                .header("Authorization", bearer(fixture.managerToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("不支援的執行狀態。")));

        mvc.perform(get(API + "/report")
                .queryParam("assigneeId", UUID.randomUUID().toString())
                .header("Authorization", bearer(fixture.managerToken())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("找不到指定執行者。")));
    }

    /** 驗證非主管被拒絕且未綁公司主管收到明確錯誤。 */
    @Test
    void reportRejectsNonManagerAndManagerWithoutCompany() throws Exception {
        String employeeToken = token("user", "admin123");

        mvc.perform(get(API + "/filters").header("Authorization", bearer(employeeToken)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("[主管報表] [api] 無主管權限。")));

        String adminToken = token("admin", "admin123");
        JsonNode manager = createUser(adminToken, "未綁公司主管", "unbound.manager." + suffix());
        createSupervisor(adminToken, manager.path("id").asText(), "未綁公司主管");
        String managerToken = token(manager.path("username").asText(), "password123");

        mvc.perform(get(API + "/filters").header("Authorization", bearer(managerToken)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", is("請先綁定公司。")));
    }

    /** 建立具有公司、主管角色、員工綁定與登入 token 的測試情境。 */
    private ManagerFixture createManagerFixture(String companyPrefix) throws Exception {
        String adminToken = token("admin", "admin123");
        String suffix = suffix();
        String companyName = companyPrefix + "-" + suffix;
        JsonNode company = readData(mvc.perform(post(ADMIN_API + "/companies")
            .header("Authorization", bearer(adminToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("name", companyName, "description", "主管報表整合測試"))))
            .andExpect(status().isOk()));
        JsonNode manager = createUser(adminToken, "主管報表主管", "manager.report." + suffix);
        JsonNode employee = createUser(adminToken, "主管報表員工", "employee.report." + suffix);
        JsonNode supervisor = createSupervisor(adminToken, manager.path("id").asText(), "主管報表主管");
        bindSupervisor(adminToken, company.path("id").asText(), supervisor.path("id").asText());

        String managerToken = token(manager.path("username").asText(), "password123");
        String employeeToken = token(employee.path("username").asText(), "password123");
        mvc.perform(post(TASK_API + "/company-bindings")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("companyName", companyName))))
            .andExpect(status().isOk());
        mvc.perform(post(TASK_API + "/employee-bindings")
                .header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("employeeId", employee.path("id").asText()))))
            .andExpect(status().isOk());

        return new ManagerFixture(
            adminToken,
            managerToken,
            employeeToken,
            company.path("id").asText(),
            companyName,
            manager.path("id").asText(),
            employee.path("id").asText()
        );
    }

    /** 在既有公司建立另一位主管供公司總覽隔離測試。 */
    private ManagerAccount createPeerManager(String adminToken, String companyId, String name) throws Exception {
        String suffix = suffix();
        JsonNode manager = createUser(adminToken, name, "peer.manager." + suffix);
        JsonNode supervisor = createSupervisor(adminToken, manager.path("id").asText(), name);
        bindSupervisor(adminToken, companyId, supervisor.path("id").asText());
        return new ManagerAccount(
            manager.path("id").asText(),
            token(manager.path("username").asText(), "password123")
        );
    }

    /** 建立主管任務並回傳標準 response 的 data。 */
    private JsonNode createTask(String managerToken, String assigneeId, String name) throws Exception {
        return readData(mvc.perform(post(TASK_API + "/tasks")
            .header("Authorization", bearer(managerToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "name", name + "-" + suffix(),
                "content", "主管報表統計資料",
                "deadline", Instant.now().plus(2, ChronoUnit.DAYS).toString(),
                "assigneeId", assigneeId
            ))))
            .andExpect(status().isOk()));
    }

    /** 更新受派任務的工作狀態與進度。 */
    private void updateProgress(String employeeToken, String taskId, String workStatus, int progress) throws Exception {
        mvc.perform(put(TASK_API + "/tasks/" + taskId + "/progress")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "workStatus", workStatus,
                    "progressContent", "主管報表狀態測試",
                    "progressPercent", progress
                ))))
            .andExpect(status().isOk());
    }

    /** 建立唯一測試使用者。 */
    private JsonNode createUser(String adminToken, String fullName, String username) throws Exception {
        return readData(mvc.perform(post("/api/admin/users")
            .header("Authorization", bearer(adminToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "fullName", fullName,
                "username", username,
                "email", username + "@example.com",
                "password", "password123"
            ))))
            .andExpect(status().isOk()));
    }

    /** 授予使用者主管資料。 */
    private JsonNode createSupervisor(String adminToken, String userId, String title) throws Exception {
        return readData(mvc.perform(post(ADMIN_API + "/supervisors")
            .header("Authorization", bearer(adminToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("userId", userId, "title", title))))
            .andExpect(status().isOk()));
    }

    /** 綁定主管至指定公司。 */
    private void bindSupervisor(String adminToken, String companyId, String supervisorId) throws Exception {
        mvc.perform(post(ADMIN_API + "/bindings")
                .header("Authorization", bearer(adminToken))
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

    /** 讀取標準 response 的 data 節點。 */
    private JsonNode readData(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        return mapper.readTree(result.andReturn().getResponse().getContentAsString()).path("data");
    }

    /** 序列化 JSON 請求內容。 */
    private String json(Object value) {
        return mapper.writeValueAsString(value);
    }

    /** 建立 Bearer Authorization 值。 */
    private String bearer(String token) {
        return "Bearer " + token;
    }

    /** 產生可避免測試資料碰撞的短識別。 */
    private String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private record ManagerFixture(
        String adminToken,
        String managerToken,
        String employeeToken,
        String companyId,
        String companyName,
        String managerId,
        String employeeId
    ) { }

    private record ManagerAccount(String id, String token) { }
}
