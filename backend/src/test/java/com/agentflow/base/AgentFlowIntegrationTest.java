package com.agentflow.base;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgentFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void loginAndAdminUserFlow() throws Exception {
        String token = login("admin", "admin123");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success", is(true))).andExpect(jsonPath("$.data", hasSize(4)));
        mvc.perform(post("/api/admin/users").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"測試成員\",\"username\":\"integration.user\",\"email\":\"integration@agentflow.local\",\"password\":\"password123\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.registrationMethod", is("管理員新增"))).andExpect(jsonPath("$.data.roles[0]", is("EMPLOYEE")));
    }

    @Test
    void nonAdminReceivesExplicitPermissionMessage() throws Exception {
        String token = login("user", "admin123");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.message", is("[使用者角色] [api] 無系統管理員權限。")));
    }

    @Test
    void testTableCrudWorksEndToEnd() throws Exception {
        String token = login("admin", "admin123");
        String created = mvc.perform(post("/api/test/testTemp").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"連線測試\",\"description\":\"確認前後端與資料庫\",\"testStatus\":\"READY\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.testStatus", is("READY"))).andReturn().getResponse().getContentAsString();
        String id = mapper.readTree(created).path("data").path("id").asText();
        mvc.perform(put("/api/test/testTemp/" + id).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"連線測試完成\",\"description\":\"CRUD 正常\",\"testStatus\":\"DONE\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.testStatus", is("DONE")));
        mvc.perform(delete("/api/test/testTemp/" + id).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success", is(true))).andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(body); return json.path("data").path("token").asText();
    }
}
