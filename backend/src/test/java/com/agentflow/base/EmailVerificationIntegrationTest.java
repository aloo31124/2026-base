package com.agentflow.base;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agentflow.base.service.MailGateway;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailVerificationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean MailGateway mailGateway;

    @Test
    void adminCanSendVerificationMail() throws Exception {
        String token = login("admin", "admin123");

        mvc.perform(post("/api/admin/email-verification/send")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"receiver@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", is("驗證碼信件已寄送。")))
            .andExpect(jsonPath("$.data.maskedRecipient", is("r***@example.com")))
            .andExpect(jsonPath("$.data.sentAt").exists());

        verify(mailGateway).send(org.mockito.ArgumentMatchers.argThat(message ->
            message.recipient().equals("receiver@example.com")
                && message.subject().contains("信箱驗證碼")
                && message.text().matches("(?s).*\\b\\d{6}\\b.*")));
    }

    @Test
    void invalidEmailIsRejectedBeforeSending() throws Exception {
        String token = login("admin", "admin123");

        mvc.perform(post("/api/admin/email-verification/send")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    void nonAdminCannotSendVerificationMail() throws Exception {
        String token = login("user", "admin123");

        mvc.perform(post("/api/admin/email-verification/send")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"receiver@example.com\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", is("[使用者角色] [api] 無系統管理員權限。")));
    }

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode json = mapper.readTree(body);
        return json.path("data").path("token").asText();
    }
}
