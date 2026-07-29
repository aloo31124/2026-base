package com.agentflow.base.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpVerificationEmailSender implements VerificationEmailSender {
    private final JavaMailSender mailSender;
    private final boolean captureEnabled;
    private final String from;
    private final Map<String, String> capturedCodes = new ConcurrentHashMap<>();
    public SmtpVerificationEmailSender(JavaMailSender mailSender,
        @Value("${app.email.capture-enabled:false}") boolean captureEnabled,
        @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender; this.captureEnabled = captureEnabled; this.from = from;
    }
    @Override public void send(String email, String purpose, String code) {
        if (captureEnabled) { capturedCodes.put(key(email, purpose), code); return; }
        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) message.setFrom(from);
        message.setTo(email);
        message.setSubject("AgentFlow 信箱驗證碼");
        message.setText("您的驗證碼是 " + code + "，10 分鐘內有效。若非本人操作請忽略此信。");
        mailSender.send(message);
    }
    public String capturedCode(String email, String purpose) { return capturedCodes.get(key(email, purpose)); }
    private String key(String email, String purpose) { return email.trim().toLowerCase() + ":" + purpose; }
}
