package com.agentflow.base;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class CompanySupervisorManagementIntegrationTest {
    private static final String API = "/api/admin/company-supervisor-management";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    /**
     * 驗證公司可完整增查改刪，且名稱忽略大小寫不得重複。
     */
    @Test
    void adminCanCrudCompanies() throws Exception {
        String token = token("admin", "admin123");
        String unique = "整合公司-" + UUID.randomUUID();
        JsonNode company = createCompany(token, unique, "初始說明");
        String id = company.path("id").asText();

        mvc.perform(get(API + "/companies")
                .queryParam("name", unique.substring(0, 4))
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].name", hasItem(unique)));

        mvc.perform(put(API + "/companies/" + id)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("name", unique + "-更新", "description", "更新說明"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name", is(unique + "-更新")))
            .andExpect(jsonPath("$.data.description", is("更新說明")));

        mvc.perform(post(API + "/companies")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("name", (unique + "-更新").toUpperCase(), "description", ""))))
            .andExpect(status().isConflict());

        mvc.perform(delete(API + "/companies/" + id)
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", nullValue()));
    }

    /**
     * 驗證主管只能由既有啟用使用者建立，並可修改與移除主管身分。
     */
    @Test
    void supervisorMustReferenceRegisteredActiveUser() throws Exception {
        String token = token("admin", "admin123");
        JsonNode user = createUser(token, "主管候選");
        String userId = user.path("id").asText();

        JsonNode supervisor = createSupervisor(token, userId, "部門主管");
        String supervisorId = supervisor.path("id").asText();
        mvc.perform(get("/api/admin/users").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.id == '" + userId + "')].roles", hasItem(hasItem("MANAGER"))));

        mvc.perform(put(API + "/supervisors/" + supervisorId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "營運主管"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title", is("營運主管")));

        mvc.perform(post(API + "/supervisors")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("userId", UUID.randomUUID(), "title", "無效主管"))))
            .andExpect(status().isNotFound());

        mvc.perform(delete(API + "/supervisors/" + supervisorId)
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk());
    }

    /**
     * 驗證一家公司可有多名主管，而同一使用者最多綁定一家公司。
     */
    @Test
    void bindingsAllowManySupervisorsButOnlyOneCompanyPerUser() throws Exception {
        String token = token("admin", "admin123");
        JsonNode firstCompany = createCompany(token, "甲公司-" + UUID.randomUUID(), "");
        JsonNode secondCompany = createCompany(token, "乙公司-" + UUID.randomUUID(), "");
        JsonNode firstSupervisor = createSupervisor(
            token,
            createUser(token, "第一主管").path("id").asText(),
            "第一主管"
        );
        JsonNode secondSupervisor = createSupervisor(
            token,
            createUser(token, "第二主管").path("id").asText(),
            "第二主管"
        );

        JsonNode firstBinding = createBinding(
            token,
            firstCompany.path("id").asText(),
            firstSupervisor.path("id").asText()
        );
        createBinding(token, firstCompany.path("id").asText(), secondSupervisor.path("id").asText());

        mvc.perform(post(API + "/bindings")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "companyId", secondCompany.path("id").asText(),
                    "supervisorId", firstSupervisor.path("id").asText()
                ))))
            .andExpect(status().isConflict());

        mvc.perform(delete(API + "/companies/" + firstCompany.path("id").asText())
                .header("Authorization", bearer(token)))
            .andExpect(status().isConflict());

        mvc.perform(delete(API + "/supervisors/" + firstSupervisor.path("id").asText())
                .header("Authorization", bearer(token)))
            .andExpect(status().isConflict());

        mvc.perform(delete(API + "/bindings/" + firstBinding.path("id").asText())
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk());

        createBinding(token, secondCompany.path("id").asText(), firstSupervisor.path("id").asText());
    }

    /**
     * 驗證綁定可同時依公司名稱及主管姓名或帳號篩選。
     */
    @Test
    void bindingsCanBeSearchedByCompanyAndSupervisor() throws Exception {
        String token = token("admin", "admin123");
        String companyName = "搜尋公司-" + UUID.randomUUID();
        String supervisorName = "搜尋主管";
        JsonNode company = createCompany(token, companyName, "");
        JsonNode supervisor = createSupervisor(
            token,
            createUser(token, supervisorName).path("id").asText(),
            "搜尋職稱"
        );
        createBinding(token, company.path("id").asText(), supervisor.path("id").asText());

        mvc.perform(get(API + "/bindings")
                .queryParam("companyName", companyName.substring(0, 4))
                .queryParam("supervisorName", supervisorName)
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].companyName", is(companyName)))
            .andExpect(jsonPath("$.data[0].supervisorName", is(supervisorName)));
    }

    /**
     * 驗證員工可綁定、依公司與員工查詢、拒絕第二家公司，並在取消後改綁。
     */
    @Test
    void employeeBindingsSupportSearchConflictAndRebinding() throws Exception {
        String token = token("admin", "admin123");
        String employeeName = "搜尋員工";
        JsonNode firstCompany = createCompany(token, "員工甲公司-" + UUID.randomUUID(), "");
        JsonNode secondCompany = createCompany(token, "員工乙公司-" + UUID.randomUUID(), "");
        JsonNode employee = createUser(token, employeeName);

        JsonNode binding = createEmployeeBinding(
            token,
            firstCompany.path("id").asText(),
            employee.path("id").asText()
        );

        mvc.perform(get(API + "/employee-bindings")
                .queryParam("companyName", firstCompany.path("name").asText())
                .queryParam("employeeName", employeeName)
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].companyId", is(firstCompany.path("id").asText())))
            .andExpect(jsonPath("$.data[0].userId", is(employee.path("id").asText())))
            .andExpect(jsonPath("$.data[0].employeeName", is(employeeName)))
            .andExpect(jsonPath("$.data[0].employeeUsername", is(employee.path("username").asText())));

        mvc.perform(post(API + "/employee-bindings")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "companyId", secondCompany.path("id").asText(),
                    "userId", employee.path("id").asText()
                ))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("已綁定公司")));

        mvc.perform(delete(API + "/bindings/" + binding.path("id").asText())
                .header("Authorization", bearer(token)))
            .andExpect(status().isConflict());

        mvc.perform(delete(API + "/employee-bindings/" + binding.path("id").asText())
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", nullValue()));

        createEmployeeBinding(
            token,
            secondCompany.path("id").asText(),
            employee.path("id").asText()
        );
    }

    /**
     * 驗證已建立主管資料的使用者不能再用員工身分綁定公司。
     */
    @Test
    void supervisorCannotBeBoundAsEmployee() throws Exception {
        String token = token("admin", "admin123");
        JsonNode company = createCompany(token, "身分公司-" + UUID.randomUUID(), "");
        JsonNode user = createUser(token, "身分主管");
        createSupervisor(token, user.path("id").asText(), "主管");

        mvc.perform(post(API + "/employee-bindings")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "companyId", company.path("id").asText(),
                    "userId", user.path("id").asText()
                ))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("主管")));
    }

    /**
     * 驗證一般使用者收到公司主管管理專屬 403 訊息。
     */
    @Test
    void nonAdminReceivesModuleSpecificForbiddenMessage() throws Exception {
        String token = token("user", "admin123");
        mvc.perform(get(API + "/companies").header("Authorization", bearer(token)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("[公司主管管理] [api] 無系統管理員權限。")));
    }

    /**
     * 透過管理 API 建立唯一測試使用者。
     */
    private JsonNode createUser(String token, String fullName) throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String body = mvc.perform(post("/api/admin/users")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "fullName", fullName,
                    "username", "candidate." + suffix,
                    "email", "candidate." + suffix + "@example.com",
                    "password", "password123"
                ))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data");
    }

    /**
     * 透過管理 API 建立公司並回傳資料節點。
     */
    private JsonNode createCompany(String token, String name, String description) throws Exception {
        String body = mvc.perform(post(API + "/companies")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("name", name, "description", description))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data");
    }

    /**
     * 透過管理 API 建立主管並回傳資料節點。
     */
    private JsonNode createSupervisor(String token, String userId, String title) throws Exception {
        String body = mvc.perform(post(API + "/supervisors")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("userId", userId, "title", title))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data");
    }

    /**
     * 透過管理 API 建立公司主管綁定並回傳資料節點。
     */
    private JsonNode createBinding(String token, String companyId, String supervisorId) throws Exception {
        String body = mvc.perform(post(API + "/bindings")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("companyId", companyId, "supervisorId", supervisorId))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data");
    }

    /**
     * 透過管理 API 建立公司員工綁定並回傳資料節點。
     */
    private JsonNode createEmployeeBinding(String token, String companyId, String userId) throws Exception {
        String body = mvc.perform(post(API + "/employee-bindings")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("companyId", companyId, "userId", userId))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data");
    }

    /**
     * 取得既有測試帳號 JWT。
     */
    private String token(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", username, "password", password))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data").path("token").asText();
    }

    /**
     * 將物件轉為 JSON 請求內容。
     */
    private String json(Object value) {
        return mapper.writeValueAsString(value);
    }

    /**
     * 建立 Bearer Authorization 值。
     */
    private String bearer(String token) {
        return "Bearer " + token;
    }
}
