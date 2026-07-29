package com.agentflow.base.service;

import com.agentflow.base.config.MailProperties;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.service.MailGateway.MailMessage;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SmtpMailGateway implements MailGateway {
    private final JavaMailSender mailSender;
    private final MailProperties properties;

    public SmtpMailGateway(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    /**
     * 使用 Spring Mail 將純文字信件交付 Gmail SMTP。
     *
     * @param message 收件者、主旨與純文字內容
     */
    @Override
    public void send(MailMessage message) {
        if (!StringUtils.hasText(properties.sender())) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Gmail SMTP 尚未設定。");
        }

        // 只在傳輸物件中放入信件內容，不將驗證碼或完整收件者寫入日誌。
        SimpleMailMessage smtpMessage = new SimpleMailMessage();
        smtpMessage.setFrom(properties.sender());
        smtpMessage.setTo(message.recipient());
        smtpMessage.setSubject(message.subject());
        smtpMessage.setText(message.text());
        mailSender.send(smtpMessage);
    }
}
