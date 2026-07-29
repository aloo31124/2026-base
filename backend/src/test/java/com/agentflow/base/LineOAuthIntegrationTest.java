package com.agentflow.base;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agentflow.base.dao.LineOAuthAccountDao;
import com.agentflow.base.dao.LineOAuthAttemptDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.RegistrationRecordDao;
import com.agentflow.base.model.bo.LineOAuthAttempt;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LineOAuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserAccountDao userDao;
    @Autowired LineOAuthAccountDao accountDao;
    @Autowired LineOAuthAttemptDao attemptDao;
    @Autowired RegistrationRecordDao registrationRecordDao;

    @Test
    void firstLoginRegistersAndSecondLoginReusesSameUser() throws Exception {
        long usersBefore = userDao.count();
        long accountsBefore = accountDao.count();
        long successBefore = attemptDao.countByStatus(LineOAuthAttempt.Status.SUCCESS);
        long registrationRecordsBefore = registrationRecordDao.count();

        String firstState = authorizeState();
        JsonNode first = callback("line-junit-user", firstState)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.roles[0]", is("EMPLOYEE")))
            .andReturnBody(mapper);
        String username = first.path("data").path("username").asText();

        String secondState = authorizeState();
        callback("line-junit-user", secondState)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username", is(username)));

        org.assertj.core.api.Assertions.assertThat(userDao.count()).isEqualTo(usersBefore + 1);
        org.assertj.core.api.Assertions.assertThat(accountDao.count()).isEqualTo(accountsBefore + 1);
        org.assertj.core.api.Assertions.assertThat(attemptDao.countByStatus(LineOAuthAttempt.Status.SUCCESS)).isEqualTo(successBefore + 2);
        org.assertj.core.api.Assertions.assertThat(registrationRecordDao.count()).isEqualTo(registrationRecordsBefore + 1);
    }

    @Test
    void providerFailureIsAuditedWithoutSensitiveResponse() throws Exception {
        long failuresBefore = attemptDao.countByResultCode("PROVIDER_ERROR");
        String state = authorizeState();
        callback("provider-error", state)
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("LINE 驗證服務")));
        org.assertj.core.api.Assertions.assertThat(attemptDao.countByResultCode("PROVIDER_ERROR")).isEqualTo(failuresBefore + 1);
    }

    @Test
    void deniedAndReplayedCallbacksAreRejected() throws Exception {
        String deniedState = authorizeState();
        mvc.perform(post("/api/auth/line/callback").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("state", deniedState, "error", "access_denied"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("授權已取消")));

        String successState = authorizeState();
        callback("line-replay-user", successState).andExpect(status().isOk());
        callback("line-replay-user", successState)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", containsString("state 無效或已使用")));
    }

    private String authorizeState() throws Exception {
        String response = mvc.perform(get("/api/auth/line/authorize"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.authorizationUrl", containsString("/api/auth/line/mock/authorize")))
            .andReturn().getResponse().getContentAsString();
        String url = mapper.readTree(response).path("data").path("authorizationUrl").asText();
        return query(URI.create(url)).get("state");
    }

    private Result callback(String code, String state) throws Exception {
        var actions = mvc.perform(post("/api/auth/line/callback").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("code", code, "state", state))));
        return new Result(actions);
    }

    private static Map<String, String> query(URI uri) {
        return Arrays.stream(uri.getRawQuery().split("&"))
            .map(value -> value.split("=", 2))
            .collect(Collectors.toMap(
                part -> URLDecoder.decode(part[0], StandardCharsets.UTF_8),
                part -> URLDecoder.decode(part[1], StandardCharsets.UTF_8)
            ));
    }

    private static final class Result {
        private final org.springframework.test.web.servlet.ResultActions actions;
        private Result(org.springframework.test.web.servlet.ResultActions actions) { this.actions = actions; }
        private Result andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            actions.andExpect(matcher);
            return this;
        }
        private JsonNode andReturnBody(ObjectMapper mapper) throws Exception {
            return mapper.readTree(actions.andReturn().getResponse().getContentAsString());
        }
    }
}
