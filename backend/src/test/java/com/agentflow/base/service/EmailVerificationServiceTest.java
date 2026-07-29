package com.agentflow.base.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.config.MailProperties;
import com.agentflow.base.dao.EmailVerificationDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.service.MailGateway.MailMessage;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

class EmailVerificationServiceTest {
    private static final Pattern SIX_DIGIT_CODE = Pattern.compile("\\b\\d{6}\\b");

    @Test
    void sendsSixDigitVerificationCodeWithoutReturningIt() {
        MailGateway gateway = mock(MailGateway.class);
        EmailVerificationService service = service(gateway);

        var response = service.send("admin@example.com");

        ArgumentCaptor<MailMessage> messageCaptor = ArgumentCaptor.forClass(MailMessage.class);
        verify(gateway).send(messageCaptor.capture());
        MailMessage message = messageCaptor.getValue();
        assertThat(message.recipient()).isEqualTo("admin@example.com");
        assertThat(message.subject()).contains("AgentFlow", "信箱驗證碼");
        assertThat(message.text()).contains("10 分鐘");
        assertThat(SIX_DIGIT_CODE.matcher(message.text()).find()).isTrue();
        assertThat(response.maskedRecipient()).isEqualTo("a***@example.com");
        assertThat(response.toString()).doesNotContain(message.text());
    }

    @Test
    void convertsUnexpectedSmtpFailureToSafeBusinessError() {
        MailGateway gateway = mock(MailGateway.class);
        doThrow(new IllegalStateException("smtp secret detail")).when(gateway).send(any());
        EmailVerificationService service = service(gateway);

        assertThatThrownBy(() -> service.send("admin@example.com"))
            .isInstanceOfSatisfying(BusinessException.class, exception -> {
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                assertThat(exception.getMessage()).isEqualTo("驗證碼信件寄送失敗，請稍後再試。");
                assertThat(exception.getMessage()).doesNotContain("secret");
            });
    }

    @Test
    void preservesConfigurationBusinessError() {
        MailGateway gateway = mock(MailGateway.class);
        BusinessException notConfigured = new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Gmail SMTP 尚未設定。");
        doThrow(notConfigured).when(gateway).send(any());
        EmailVerificationService service = service(gateway);

        assertThatThrownBy(() -> service.send("admin@example.com")).isSameAs(notConfigured);
    }

    /**
     * 建立只測試寄信行為的服務與替身相依。
     */
    private EmailVerificationService service(MailGateway gateway) {
        return new EmailVerificationService(
            gateway,
            new MailProperties("", false, null),
            mock(org.springframework.security.crypto.password.PasswordEncoder.class),
            mock(EmailVerificationDao.class),
            mock(UserAccountDao.class),
            mock(EmailDeliveryLogService.class)
        );
    }
}
