package com.agentflow.base.service;

import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailResponse;
import com.agentflow.base.service.MailGateway.MailMessage;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {
    private static final int CODE_UPPER_BOUND = 1_000_000;
    private static final int VISIBLE_LOCAL_PART_LENGTH = 1;
    private static final String SUBJECT = "AgentFlow - 信箱驗證碼";
    private static final String BODY_TEMPLATE = """
        您好：

        您的 AgentFlow 信箱驗證碼是：%s

        驗證碼將於 10 分鐘後失效。若您未進行此操作，請忽略本信件。
        """;

    private final MailGateway mailGateway;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(MailGateway mailGateway) {
        this.mailGateway = mailGateway;
    }

    /**
     * 產生一次性驗證碼並寄送至指定信箱。
     *
     * @param email 已由 API 邊界驗證格式的收件信箱
     * @return 不包含驗證碼的寄送結果
     */
    public SendVerificationMailResponse send(String email) {
        String recipient = email.trim();
        String code = "%06d".formatted(secureRandom.nextInt(CODE_UPPER_BOUND));

        try {
            // 驗證碼只存在於這次呼叫的區域變數及 SMTP 訊息，不保存或回傳至前端。
            mailGateway.send(new MailMessage(recipient, SUBJECT, BODY_TEMPLATE.formatted(code)));
            return new SendVerificationMailResponse(maskEmail(recipient), OffsetDateTime.now());
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "驗證碼信件寄送失敗，請稍後再試。");
        }
    }

    /**
     * 遮罩收件者帳號部分，讓成功回應可辨識目標又不揭露完整信箱。
     *
     * @param email 完整電子郵件地址
     * @return 遮罩後地址
     */
    private String maskEmail(String email) {
        int atIndex = email.lastIndexOf('@');
        String localPart = email.substring(0, atIndex);
        String visiblePart = localPart.substring(0, Math.min(VISIBLE_LOCAL_PART_LENGTH, localPart.length()));
        return visiblePart + "***" + email.substring(atIndex);
    }
}
