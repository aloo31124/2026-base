package com.agentflow.base;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agentflow.base.service.MailGateway;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailRegistrationLoginIntegrationTest {
    private static final String TEST_CODE = "123456";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean MailGateway mailGateway;

    /**
     * 驗證首次信箱可完成寄碼、核銷、建帳、自動登入及後續帳密登入。
     */
    @Test
    void firstEmailCanRegisterAndLogin() throws Exception {
        String email = uniqueEmail("register");

        requestCode("/api/auth/email/registration-code", email)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.maskedRecipient").value(org.hamcrest.Matchers.endsWith("@example.com")));

        String ticket = verifyCode(email, "REGISTRATION");
        String registrationBody = register(email, ticket, "password123", "password123")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username", is(email)))
            .andExpect(jsonPath("$.data.roles", hasItem("EMPLOYEE")))
            .andReturn().getResponse().getContentAsString();
        String automaticToken = mapper.readTree(registrationBody).path("data").path("token").asText();
        org.assertj.core.api.Assertions.assertThat(automaticToken).isNotBlank();

        login(email, "password123")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username", is(email)));
    }

    /**
     * 驗證既有信箱不會收到註冊碼，且密碼與確認密碼必須一致。
     */
    @Test
    void duplicateEmailAndMismatchedPasswordAreRejected() throws Exception {
        requestCode("/api/auth/email/registration-code", "admin@agentflow.local")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", is("此信箱已註冊。")));

        String email = uniqueEmail("mismatch");
        requestCode("/api/auth/email/registration-code", email).andExpect(status().isOk());
        String ticket = verifyCode(email, "REGISTRATION");
        register(email, ticket, "password123", "different123")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("密碼與確認密碼不一致。")));
    }

    /**
     * 驗證錯誤驗證碼會被拒絕，正確票券完成使用後不可再次核銷。
     */
    @Test
    void invalidCodeAndUsedTicketAreRejected() throws Exception {
        String email = uniqueEmail("ticket");
        requestCode("/api/auth/email/registration-code", email).andExpect(status().isOk());

        mvc.perform(post("/api/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("""
                    {"email":"%s","code":"000000","purpose":"REGISTRATION"}
                    """.formatted(email))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("驗證碼錯誤。")));

        String ticket = verifyCode(email, "REGISTRATION");
        register(email, ticket, "password123", "password123").andExpect(status().isOk());
        register(email, ticket, "password123", "password123")
            .andExpect(status().isConflict());
    }

    /**
     * 驗證忘記密碼流程會使舊密碼失效，並允許新密碼登入。
     */
    @Test
    void passwordResetReplacesOldPassword() throws Exception {
        String email = uniqueEmail("reset");
        requestCode("/api/auth/email/registration-code", email).andExpect(status().isOk());
        register(email, verifyCode(email, "REGISTRATION"), "password123", "password123")
            .andExpect(status().isOk());

        requestCode("/api/auth/email/password-reset-code", email).andExpect(status().isOk());
        String resetTicket = verifyCode(email, "PASSWORD_RESET");
        mvc.perform(post("/api/auth/email/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("""
                    {"email":"%s","ticketId":"%s","password":"newPassword456","confirmPassword":"newPassword456"}
                    """.formatted(email, resetTicket))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("密碼已更新。")));

        login(email, "password123").andExpect(status().isUnauthorized());
        login(email, "newPassword456").andExpect(status().isOk());
    }

    /**
     * 驗證管理員測試寄信會留下可查詢紀錄，一般使用者仍受權限限制。
     */
    @Test
    void adminCanInspectDeliveryLogs() throws Exception {
        String adminToken = token("admin", "admin123");
        mvc.perform(post("/api/admin/email-verification/send")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"receiver@example.com\"}"))
            .andExpect(status().isOk());

        mvc.perform(get("/api/admin/email-verification/logs")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].maskedRecipient", is("r***@example.com")))
            .andExpect(jsonPath("$.data[0].purpose", is("ADMIN_TEST")))
            .andExpect(jsonPath("$.data[0].status", is("SUCCESS")));

        String userToken = token("user", "admin123");
        mvc.perform(get("/api/admin/email-verification/logs")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    /**
     * 呼叫指定用途的寄碼 API。
     *
     * @param path API 路徑
     * @param email 收件信箱
     * @return 可接續斷言的結果
     */
    private org.springframework.test.web.servlet.ResultActions requestCode(String path, String email) throws Exception {
        return mvc.perform(post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json("{\"email\":\"" + email + "\"}")));
    }

    /**
     * 核銷固定測試驗證碼並回傳一次性票券。
     *
     * @param email 收件信箱
     * @param purpose 驗證用途
     * @return 票券 UUID 字串
     */
    private String verifyCode(String email, String purpose) throws Exception {
        String body = mvc.perform(post("/api/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("""
                    {"email":"%s","code":"%s","purpose":"%s"}
                    """.formatted(email, TEST_CODE, purpose))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data").path("ticketId").asText();
    }

    /**
     * 送出信箱註冊資料。
     */
    private org.springframework.test.web.servlet.ResultActions register(
        String email, String ticket, String password, String confirmPassword
    ) throws Exception {
        return mvc.perform(post("/api/auth/email/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json("""
                {"email":"%s","ticketId":"%s","password":"%s","confirmPassword":"%s"}
                """.formatted(email, ticket, password, confirmPassword))));
    }

    /**
     * 呼叫既有登入 API。
     */
    private org.springframework.test.web.servlet.ResultActions login(String username, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json("""
                {"username":"%s","password":"%s"}
                """.formatted(username, password))));
    }

    /**
     * 取得指定既有帳號的 JWT。
     */
    private String token(String username, String password) throws Exception {
        String body = login(username, password)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("data").path("token").asText();
    }

    /**
     * 產生不與其他測試衝突的信箱。
     */
    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    /**
     * 將測試 JSON 正規化為單行內容。
     */
    private String json(String value) throws Exception {
        JsonNode node = mapper.readTree(value);
        return mapper.writeValueAsString(node);
    }
}
