package com.agentflow.base;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class EmailAuthIntegrationTest {
    @Autowired MockMvc mvc;

    @Test void emailRegistrationAndLoginWorks() throws Exception {
        String email = "new.member@agentflow.local";
        sendCode("/api/auth/email/registrations/code", email);
        String code = code(email, "REGISTER");
        mvc.perform(post("/api/auth/email/registrations").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"新會員\",\"email\":\""+email+"\",\"password\":\"Member123\",\"code\":\""+code+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.username", is(email)))
            .andExpect(jsonPath("$.data.roles[0]", is("EMPLOYEE")));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"NEW.MEMBER@AGENTFLOW.LOCAL\",\"password\":\"Member123\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success", is(true)));
        mvc.perform(post("/api/auth/email/registrations").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"重複\",\"email\":\""+email+"\",\"password\":\"Member123\",\"code\":\""+code+"\"}"))
            .andExpect(status().isConflict());
    }

    @Test void wrongCodeAndWeakPasswordAreRejected() throws Exception {
        String email = "validation@agentflow.local";
        sendCode("/api/auth/email/registrations/code", email);
        mvc.perform(post("/api/auth/email/registrations").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"測試\",\"email\":\""+email+"\",\"password\":\"abcdefgh\",\"code\":\"000000\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is("密碼必須包含數字。")));
        mvc.perform(post("/api/auth/email/registrations").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"測試\",\"email\":\""+email+"\",\"password\":\"Password123\",\"code\":\"000000\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is("驗證碼錯誤。")));
    }

    @Test void forgotPasswordResetsCredentialAndUnknownEmailDoesNotLeak() throws Exception {
        String email = "reset.member@agentflow.local";
        sendCode("/api/auth/email/registrations/code", email);
        String registrationCode = code(email, "REGISTER");
        mvc.perform(post("/api/auth/email/registrations").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"重設會員\",\"email\":\""+email+"\",\"password\":\"Before123\",\"code\":\""+registrationCode+"\"}"))
            .andExpect(status().isOk());
        sendCode("/api/auth/email/password-resets/code", email);
        String resetCode = code(email, "RESET_PASSWORD");
        mvc.perform(post("/api/auth/email/password-resets").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\""+email+"\",\"newPassword\":\"After456\",\"code\":\""+resetCode+"\"}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\""+email+"\",\"password\":\"After456\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/email/password-resets/code").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"missing@agentflow.local\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.message", is("若此信箱已註冊，驗證碼將寄至該信箱。")));
    }

    private void sendCode(String path, String email) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\""+email+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success", is(true)));
    }
    private String code(String email, String purpose) throws Exception {
        String body = mvc.perform(get("/api/auth/email/test-code").param("email", email).param("purpose", purpose))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new tools.jackson.databind.ObjectMapper().readTree(body).path("data").asText();
    }
}
