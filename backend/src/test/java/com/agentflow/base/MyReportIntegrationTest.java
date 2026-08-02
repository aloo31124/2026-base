package com.agentflow.base;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agentflow.base.dao.AssignedTaskDao;
import com.agentflow.base.dao.RoleDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.model.bo.AssignedTask;
import com.agentflow.base.model.bo.AssignedTask.WorkStatus;
import com.agentflow.base.model.bo.Role;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyReportIntegrationTest {
    private static final String API = "/api/my/reports";
    private static final String PASSWORD = "password123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserAccountDao userDao;
    @Autowired RoleDao roleDao;
    @Autowired UserRoleDao userRoleDao;
    @Autowired AssignedTaskDao taskDao;
    @Autowired PasswordEncoder passwordEncoder;

    /** 讓我的報表 fixture 使用獨立 H2，確保本人任務數可精確斷言。 */
    @DynamicPropertySource
    static void myReportDatabase(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            () -> "jdbc:h2:mem:my-report;MODE=MSSQLServer;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
        );
    }

    /** 驗證 Sheet 第 23 列：員工只能看到自己的任務總覽與本人選項。 */
    @Test
    void employeeCanViewOnlyOwnTaskOverview() throws Exception {
        UserAccount employee = createEmployee("我的報表員工");
        UserAccount other = createEmployee("其他員工");
        createTask(employee, employee, "本人任務", WorkStatus.PENDING);
        createTask(other, other, "其他人任務", WorkStatus.COMPLETED);
        String token = token(employee.getUsername());
        String today = LocalDate.now().toString();

        mvc.perform(get(API + "/filters").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("我的報表篩選選項查詢成功。")))
            .andExpect(jsonPath("$.data.assignees", hasSize(1)))
            .andExpect(jsonPath("$.data.assignees[0].id", is(employee.getId().toString())))
            .andExpect(jsonPath("$.data.assignees[0].name", is(employee.getFullName())))
            .andExpect(jsonPath("$.data.workStatuses", hasSize(3)));

        mvc.perform(get(API + "/report")
                .queryParam("from", today)
                .queryParam("to", today)
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assigneeId", is(employee.getId().toString())))
            .andExpect(jsonPath("$.data.assigneeName", is(employee.getFullName())))
            .andExpect(jsonPath("$.data.totalTasks", is(1)))
            .andExpect(jsonPath("$.data.trendPoints", hasSize(1)))
            .andExpect(jsonPath("$.data.trendPoints[0].taskCount", is(1)));
    }

    /** 驗證 Sheet 第 24–25 列：狀態／日期篩選、連續趨勢與比例皆正確。 */
    @Test
    void employeeCanFilterTrendAndStatusRatio() throws Exception {
        UserAccount employee = createEmployee("狀態報表員工");
        createTask(employee, employee, "待處理任務", WorkStatus.PENDING);
        createTask(employee, employee, "已完成任務一", WorkStatus.COMPLETED);
        createTask(employee, employee, "已完成任務二", WorkStatus.COMPLETED);
        String token = token(employee.getUsername());
        LocalDate today = LocalDate.now();

        mvc.perform(get(API + "/report")
                .queryParam("assigneeId", employee.getId().toString())
                .queryParam("workStatus", "COMPLETED")
                .queryParam("from", today.toString())
                .queryParam("to", today.toString())
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.workStatus", is("COMPLETED")))
            .andExpect(jsonPath("$.data.totalTasks", is(2)))
            .andExpect(jsonPath("$.data.trendPoints[0].taskCount", is(2)))
            .andExpect(jsonPath("$.data.statusBuckets[?(@.status == 'PENDING')].taskCount", hasItem(0)))
            .andExpect(jsonPath("$.data.statusBuckets[?(@.status == 'COMPLETED')].taskCount", hasItem(2)))
            .andExpect(jsonPath("$.data.statusBuckets[?(@.status == 'COMPLETED')].percentage", hasItem(100.0)));

        mvc.perform(get(API + "/report").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.from", is(today.minusYears(1).toString())))
            .andExpect(jsonPath("$.data.to", is(today.toString())))
            .andExpect(jsonPath(
                "$.data.trendPoints",
                hasSize((int) ChronoUnit.DAYS.between(today.minusYears(1), today) + 1)
            ));
    }

    /** 驗證非法條件、跨員工識別與未登入請求都被明確拒絕。 */
    @Test
    void reportRejectsInvalidFiltersAndUnauthenticatedRequest() throws Exception {
        UserAccount employee = createEmployee("防護測試員工");
        UserAccount other = createEmployee("防護其他員工");
        String token = token(employee.getUsername());
        LocalDate today = LocalDate.now();

        mvc.perform(get(API + "/report")
                .queryParam("assigneeId", other.getId().toString())
                .header("Authorization", bearer(token)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("只能查詢自己的任務報表。")));

        mvc.perform(get(API + "/report")
                .queryParam("workStatus", "UNKNOWN")
                .header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("不支援的執行狀態。")));

        mvc.perform(get(API + "/report")
                .queryParam("from", today.toString())
                .queryParam("to", today.minusDays(1).toString())
                .header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("開始日期不得晚於結束日期。")));

        mvc.perform(get(API + "/filters"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success", is(false)));
    }

    /** 建立具 EMPLOYEE 角色且可登入的唯一測試員工。 */
    private UserAccount createEmployee(String fullName) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UserAccount employee = userDao.save(new UserAccount(
            fullName,
            "my.report." + suffix,
            "my.report." + suffix + "@example.com",
            passwordEncoder.encode(PASSWORD),
            "我的報表整合測試"
        ));
        Role employeeRole = roleDao.findByRoleCode("EMPLOYEE")
            .orElseGet(() -> roleDao.save(new Role("EMPLOYEE", "員工")));
        userRoleDao.save(new UserRole(employee, employeeRole));
        return employee;
    }

    /** 建立指定受派人與工作狀態的任務資料。 */
    private void createTask(UserAccount creator, UserAccount assignee, String name, WorkStatus workStatus) {
        AssignedTask task = new AssignedTask(
            name + "-" + UUID.randomUUID().toString().substring(0, 8),
            "我的報表整合測試資料",
            Instant.now().plus(2, ChronoUnit.DAYS),
            creator,
            assignee
        );
        if (workStatus != WorkStatus.PENDING) {
            task.updateProgress(workStatus, "狀態比例測試", workStatus == WorkStatus.COMPLETED ? 100 : 50);
        }
        taskDao.save(task);
    }

    /** 以測試員工登入並取得 JWT。 */
    private String token(String username) throws Exception {
        JsonNode data = mapper.readTree(mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", username, "password", PASSWORD))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }

    /** 建立 Bearer Authorization 值。 */
    private String bearer(String token) {
        return "Bearer " + token;
    }
}
