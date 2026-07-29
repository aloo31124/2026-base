package com.agentflow.base.service;

import com.agentflow.base.config.MailProperties;
import com.agentflow.base.dao.EmailVerificationDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.EmailDeliveryLog;
import com.agentflow.base.model.bo.EmailVerification;
import com.agentflow.base.model.bo.EmailVerification.Purpose;
import com.agentflow.base.model.dto.EmailVerificationDtos.SendVerificationMailResponse;
import com.agentflow.base.model.dto.EmailVerificationDtos.VerificationTicketResponse;
import com.agentflow.base.service.MailGateway.MailMessage;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailVerificationService {
    private static final int CODE_UPPER_BOUND = 1_000_000;
    private static final int VISIBLE_LOCAL_PART_LENGTH = 1;
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final String SUBJECT = "AgentFlow - 信箱驗證碼";
    private static final String BODY_TEMPLATE = """
        您好：

        您的 AgentFlow 信箱驗證碼是：%s

        驗證碼將於 10 分鐘後失效。若您未進行此操作，請忽略本信件。
        """;

    private final MailGateway mailGateway;
    private final MailProperties mailProperties;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationDao verificationDao;
    private final UserAccountDao userAccountDao;
    private final EmailDeliveryLogService deliveryLogService;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
        MailGateway mailGateway,
        MailProperties mailProperties,
        PasswordEncoder passwordEncoder,
        EmailVerificationDao verificationDao,
        UserAccountDao userAccountDao,
        EmailDeliveryLogService deliveryLogService
    ) {
        this.mailGateway = mailGateway;
        this.mailProperties = mailProperties;
        this.passwordEncoder = passwordEncoder;
        this.verificationDao = verificationDao;
        this.userAccountDao = userAccountDao;
        this.deliveryLogService = deliveryLogService;
    }

    /**
     * 保留既有管理員測試寄信行為，並新增資料庫寄送紀錄。
     */
    public SendVerificationMailResponse send(String email) {
        return deliver(normalizeEmail(email), EmailDeliveryLog.Purpose.ADMIN_TEST, false, null);
    }

    /**
     * 為首次註冊信箱建立並寄送驗證碼。
     */
    public SendVerificationMailResponse requestRegistrationCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userAccountDao.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException(HttpStatus.CONFLICT, "此信箱已註冊。");
        }
        return deliver(normalizedEmail, EmailDeliveryLog.Purpose.REGISTRATION, true, Purpose.REGISTRATION);
    }

    /**
     * 為既有信箱帳號建立密碼重設驗證碼。
     */
    public SendVerificationMailResponse requestPasswordResetCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (!userAccountDao.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "查無此信箱帳號。");
        }
        return deliver(normalizedEmail, EmailDeliveryLog.Purpose.PASSWORD_RESET, true, Purpose.PASSWORD_RESET);
    }

    /**
     * 核銷最新有效驗證碼並回傳一次性票券。
     */
    public VerificationTicketResponse verify(String email, String code, Purpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        EmailVerification verification = verificationDao
            .findFirstByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(normalizedEmail, purpose)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "找不到有效的信箱驗證流程。"));

        try {
            verification.verify(code, passwordEncoder);
            verificationDao.save(verification);
        } catch (BusinessException exception) {
            // 失敗次數須在拋出錯誤前獨立保存，避免錯碼限制失效。
            verificationDao.save(verification);
            throw exception;
        }
        return new VerificationTicketResponse(verification.getId(), purpose, verification.getExpiresAt());
    }

    /**
     * 依票券、信箱與用途載入已完成驗證的流程。
     */
    public EmailVerification requireUsableTicket(UUID ticketId, String email, Purpose purpose) {
        EmailVerification verification = verificationDao.findById(ticketId)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "驗證票券不存在。"));
        verification.ensureUsableTicket(normalizeEmail(email), purpose);
        return verification;
    }

    /**
     * 保存票券已使用狀態。
     */
    public void consume(EmailVerification verification) {
        verification.consume();
        verificationDao.save(verification);
    }

    /**
     * 正規化信箱供唯一性與驗證流程比對。
     */
    public String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 寄送信件、保存安全寄送紀錄，並視用途建立驗證流程。
     */
    private SendVerificationMailResponse deliver(
        String email,
        EmailDeliveryLog.Purpose logPurpose,
        boolean persistVerification,
        Purpose verificationPurpose
    ) {
        String code = verificationCode();
        String maskedRecipient = maskEmail(email);

        try {
            mailGateway.send(new MailMessage(email, SUBJECT, BODY_TEMPLATE.formatted(code)));
            if (persistVerification) {
                verificationDao.save(new EmailVerification(
                    email,
                    verificationPurpose,
                    passwordEncoder.encode(code),
                    Instant.now().plus(CODE_TTL)
                ));
            }
            deliveryLogService.recordSuccess(email, maskedRecipient, logPurpose);
            return new SendVerificationMailResponse(maskedRecipient, OffsetDateTime.now());
        } catch (BusinessException exception) {
            deliveryLogService.recordFailure(email, maskedRecipient, logPurpose, exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            String safeMessage = "驗證碼信件寄送失敗，請稍後再試。";
            deliveryLogService.recordFailure(email, maskedRecipient, logPurpose, safeMessage);
            throw new BusinessException(HttpStatus.BAD_GATEWAY, safeMessage);
        }
    }

    /**
     * 在測試 profile 使用固定碼，正式環境使用安全亂數。
     */
    private String verificationCode() {
        if (StringUtils.hasText(mailProperties.testCode())
            && mailProperties.testCode().matches("\\d{6}")) {
            return mailProperties.testCode();
        }
        return "%06d".formatted(secureRandom.nextInt(CODE_UPPER_BOUND));
    }

    /**
     * 遮罩收件者帳號部分，避免 API 與管理頁揭露完整信箱。
     */
    private String maskEmail(String email) {
        int atIndex = email.lastIndexOf('@');
        String localPart = email.substring(0, atIndex);
        String visiblePart = localPart.substring(0, Math.min(VISIBLE_LOCAL_PART_LENGTH, localPart.length()));
        return visiblePart + "***" + email.substring(atIndex);
    }
}
