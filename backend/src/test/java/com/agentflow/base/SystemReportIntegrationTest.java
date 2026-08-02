package com.agentflow.base;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemReportIntegrationTest {
    private static final String API = "/api/admin/system-reports";
    private static final String ADMIN_API = "/api/admin/company-supervisor-management";
    private static final String TASK_API = "/api/task-assignment";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    /** 驗證系統管理員可查詢全部公司並切換單一公司任務趨勢。 */
    @Test
    void adminCanViewAllCompaniesAndFilterTaskTrend() throws Exception {
        String adminToken = token("admin", "admin123");
        ReportFixture first = createCompanyTask(adminToken, "報表甲公司");
        ReportFixture second = createCompanyTask(adminToken, "報表乙公司");
        String today = LocalDate.now().toString();

        mvc.perform(get(API + "/companies").header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].name", hasItem(first.companyName())))
            .andExpect(jsonPath("$.data[*].name", hasItem(second.companyName())));

        mvc.perform(get(API + "/task-trend")
                .queryParam("from", today)
                .queryParam("to", today)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyId", nullValue()))
            .andExpect(jsonPath("$.data.companyName", is("全部公司")))
            .andExpect(jsonPath("$.data.from", is(today)))
            .andExpect(jsonPath("$.data.to", is(today)))
            .andExpect(jsonPath("$.data.totalTasks", is(2)))
            .andExpect(jsonPath("$.data.companyCount", is(2)))
            .andExpect(jsonPath("$.data.points", hasSize(1)))
            .andExpect(jsonPath("$.data.points[0].taskCount", is(2)));

        mvc.perform(get(API + "/task-trend")
                .queryParam("companyId", first.companyId())
                .queryParam("from", today)
                .queryParam("to", today)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyId", is(first.companyId())))
            .andExpect(jsonPath("$.data.companyName", is(first.companyName())))
            .andExpect(jsonPath("$.data.totalTasks", is(1)))
            .andExpect(jsonPath("$.data.companyCount", is(1)))
            .andExpect(jsonPath("$.data.points[0].taskCount", is(1)));
    }

    /** 驗證預設最近一年、零值補點及日期範圍邊界。 */
    @Test
    void trendDefaultsToOneYearAndValidatesDateRange() throws Exception {
        String adminToken = token("admin", "admin123");
        LocalDate today = LocalDate.now();

        mvc.perform(get(API + "/task-trend").header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.from", is(today.minusYears(1).toString())))
            .andExpect(jsonPath("$.data.to", is(today.toString())))
            .andExpect(jsonPath("$.data.points", hasSize((int) ChronoUnit.DAYS.between(today.minusYears(1), today) + 1)));

        mvc.perform(get(API + "/task-trend")
                .queryParam("from", today.toString())
                .queryParam("to", today.minusDays(1).toString())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("開始日期不得晚於結束日期。")));

        mvc.perform(get(API + "/task-trend")
                .queryParam("companyId", UUID.randomUUID().toString())
                .queryParam("from", today.toString())
                .queryParam("to", today.toString())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("找不到指定公司。")));
    }

    /** 驗證一般使用者無法讀取系統報表且收到模組專屬訊息。 */
    @Test
    void nonAdminCannotAccessSystemReports() throws Exception {
        String userToken = token("user", "admin123");

        mvc.perform(get(API + "/task-trend").header("Authorization", bearer(userToken)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("[系統報表] [api] 無系統管理員權限。")));
    }

    /** 透過既有 API 建立公司、主管、員工與一筆今日任務。 */
    private ReportFixture createCompanyTask(String adminToken, String prefix) throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String companyName = prefix + "-" + suffix;
        JsonNode company = readData(mvc.perform(post(ADMIN_API + "/companies")
            .header("Authorization", bearer(adminToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("name", companyName, "description", "系統報表整合測試"))))
            .andExpect(status().isOk()));
        JsonNode manager = createUser(adminToken, "報表主管", "report.manager." + suffix);
        JsonNode employee = createUser(adminToken, "報表員工", "report.employee." + suffix);
        JsonNode supervisor = readData(mvc.perform(post(ADMIN_API + "/supervisors")
            .header("Authorization", bearer(adminToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("userId", manager.path("id").asText(), "title", "報表主管"))))
            .andExpect(status().isOk()));
        mvc.perform(post(ADMIN_API + "/bindings")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("companyId", company.path("id").asText(), "supervisorId", supervisor.path("id").asText()))))
            .andExpect(status().isOk());

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
        mvc.perform(post(TASK_API + "/tasks")
                .header("Authorization", bearer(managerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "系統報表任務-" + suffix,
                    "content", "趨勢統計資料",
                    "deadline", Instant.now().plus(2, ChronoUnit.DAYS).toString(),
                    "assigneeId", employee.path("id").asText()
                ))))
            .andExpect(status().isOk());
        return new ReportFixture(company.path("id").asText(), companyName);
    }

    /** 建立唯一測試使用者。 */
    private JsonNode createUser(String token, String fullName, String username) throws Exception {
        return readData(mvc.perform(post("/api/admin/users")
            .header("Authorization", bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "fullName", fullName,
                "username", username,
                "email", username + "@example.com",
                "password", "password123"
            ))))
            .andExpect(status().isOk()));
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

    private record ReportFixture(String companyId, String companyName) { }
}
