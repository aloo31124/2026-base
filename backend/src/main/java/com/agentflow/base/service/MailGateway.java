package com.agentflow.base.service;

public interface MailGateway {
    /**
     * 將已組合完成的純文字信件交付外部郵件服務。
     *
     * @param message 不可記錄至日誌的信件資料
     */
    void send(MailMessage message);

    record MailMessage(String recipient, String subject, String text) {
    }
}
