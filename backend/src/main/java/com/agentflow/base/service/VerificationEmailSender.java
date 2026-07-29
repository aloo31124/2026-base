package com.agentflow.base.service;

public interface VerificationEmailSender {
    void send(String email, String purpose, String code);
}
