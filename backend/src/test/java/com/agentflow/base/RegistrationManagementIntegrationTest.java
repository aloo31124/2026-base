package com.agentflow.base;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agentflow.base.dao.SessionTimeoutPolicyDao;
import com.agentflow.base.model.dto.RegistrationManagementDtos.PasswordPolicyRequest;
import com.agentflow.base.model.dto.RegistrationManagementDtos.SessionTimeoutPolicyRequest;
import com.agentflow.base.security.JwtService;
import com.agentflow.base.service.MailGateway;
import com.agentflow.base.service.RegistrationManagementService;
import com.agentflow.base.service.SessionTimeoutPolicyService;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationManagementIntegrationTest {
    private static final String TEST_CODE = "123456";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired RegistrationManagementService service;
    @Autowired SessionTimeoutPolicyService sessionTimeoutPolicyService;
    @Autowired SessionTimeoutPolicyDao sessionTimeoutPolicyDao;
    @Autowired JwtService jwtService;
    @MockitoBean MailGateway mailGateway;

    /**
     * 每個案例前恢復相容的安全預設政策。
     */
    @BeforeEach
    void resetPolicy() {
        service.updatePolicy(new PasswordPolicyRequest(8, true, true));
        sessionTimeoutPolicyService.updatePolicy(new SessionTimeoutPolicyRequest(120));
    }

    /**
     * 驗證系統管理員可讀寫政策，而不合法長度會被拒絕。
     */
    @Test
    void adminCanReadAndUpdatePasswordPolicy() throws Exception {
        String token = token("admin", "admin123");

        mvc.perform(put("/api/admin/registration-management/policy")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"minLength":12,"requireLetter":true,"requireNumber":false}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.minLength", is(12)))
            .andExpect(jsonPath("$.data.requireLetter", is(true)))
            .andExpect(jsonPath("$.data.requireNumber", is(false)));

        mvc.perform(get("/api/admin/registration-management/policy")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.minLength", is(12)));

        mvc.perform(put("/api/admin/registration-management/policy")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"minLength":7,"requireLetter":true,"requireNumber":true}
                    """))
            .andExpect(status().isBadRequest());
    }

    /**
     * 驗證管理員可更新登出時間，且只有更新後新簽發的 JWT 套用目前分鐘數。
     */
    @Test
    void adminCanUpdateSessionTimeoutAndNewJwtUsesPolicy() throws Exception {
        String adminToken = token("admin", "admin123");
        var oldClaims = jwtService.parse(adminToken);
        var oldExpiration = oldClaims.getExpiration().toInstant();

        mvc.perform(put("/api/admin/registration-management/session-timeout")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"timeoutMinutes":30}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeoutMinutes", is(30)));

        mvc.perform(get("/api/admin/registration-management/session-timeout")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeoutMinutes", is(30)));

        String newToken = token("user", "admin123");
        var newClaims = jwtService.parse(newToken);
        long newLifetimeMinutes = Duration.between(
            newClaims.getIssuedAt().toInstant(),
            newClaims.getExpiration().toInstant()
        ).toMinutes();

        org.assertj.core.api.Assertions.assertThat(newLifetimeMinutes).isEqualTo(30);
        org.assertj.core.api.Assertions.assertThat(jwtService.parse(adminToken).getExpiration().toInstant())
            .isEqualTo(oldExpiration);

        mvc.perform(put("/api/admin/registration-management/session-timeout")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"timeoutMinutes":4}
                    """))
            .andExpect(status().isBadRequest());

        mvc.perform(put("/api/admin/registration-management/session-timeout")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"timeoutMinutes":1441}
                    """))
            .andExpect(status().isBadRequest());
    }

    /**
     * 驗證尚未建立政策時，後台會沿用部署預設 120 分鐘並建立首筆資料。
     */
    @Test
    void missingSessionTimeoutPolicyUsesConfiguredDefault() throws Exception {
        sessionTimeoutPolicyDao.deleteAll();
        String adminToken = token("admin", "admin123");

        mvc.perform(get("/api/admin/registration-management/session-timeout")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeoutMinutes", is(120)));

        org.assertj.core.api.Assertions.assertThat(sessionTimeoutPolicyDao.count()).isEqualTo(1);
    }

    /**
     * 驗證信箱註冊會套用動態政策，成功後留下可查詢紀錄。
     */
    @Test
    void emailRegistrationUsesPolicyAndCreatesAuditRecord() throws Exception {
        String email = "policy." + UUID.randomUUID() + "@example.com";
        String ticket = verificationTicket(email);

        register(email, ticket, "abcdefgh")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("至少一個數字")));

        register(email, ticket, "abcdefg1")
            .andExpect(status().isOk());

        String adminToken = token("admin", "admin123");
        String response = mvc.perform(get("/api/admin/registration-management/registrations")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        boolean found = mapper.readTree(response).path("data").valueStream().anyMatch(row ->
            email.equals(row.path("identifier").asText())
                && "EMAIL".equals(row.path("method").asText())
                && row.path("success").asBoolean()
        );
        org.assertj.core.api.Assertions.assertThat(found).isTrue();
    }

    /**
     * 驗證一般使用者收到指定模組的 403 訊息。
     */
    @Test
    void nonAdminReceivesModuleSpecificForbiddenMessage() throws Exception {
        String token = token("user", "admin123");
        mvc.perform(get("/api/admin/registration-management/policy")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("[註冊登入管理] [api] 無系統管理員權限。")));

        mvc.perform(get("/api/admin/registration-management/session-timeout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("[註冊登入管理] [api] 無系統管理員權限。")));
    }

    /**
     * 寄送並核銷固定測試驗證碼。
     */
    private String verificationTicket(String email) throws Exception {
        mvc.perform(post("/api/auth/email/registration-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of("email", email))))
            .andExpect(status().isOk());
        String body = mvc.perform(post("/api/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of(
                    "email", email,
                    "code", TEST_CODE,
                    "purpose", "REGISTRATION"
                ))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data").path("ticketId").asText();
    }

    /**
     * 呼叫信箱建帳 API。
     */
    private org.springframework.test.web.servlet.ResultActions register(
        String email,
        String ticket,
        String password
    ) throws Exception {
        return mvc.perform(post("/api/auth/email/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(java.util.Map.of(
                "email", email,
                "ticketId", ticket,
                "password", password,
                "confirmPassword", password
            ))));
    }

    /**
     * 取得既有測試帳號 JWT。
     */
    private String token(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of(
                    "username", username,
                    "password", password
                ))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data").path("token").asText();
    }
}
