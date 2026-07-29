package com.agentflow.base.service;

import com.agentflow.base.dao.EmailDeliveryLogDao;
import com.agentflow.base.model.bo.EmailDeliveryLog;
import com.agentflow.base.model.bo.EmailDeliveryLog.Purpose;
import com.agentflow.base.model.bo.EmailDeliveryLog.Status;
import com.agentflow.base.model.dto.EmailVerificationDtos.DeliveryLogResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmailDeliveryLogService {
    private static final int MAX_ERROR_LENGTH = 240;
    private final EmailDeliveryLogDao deliveryLogDao;

    public EmailDeliveryLogService(EmailDeliveryLogDao deliveryLogDao) {
        this.deliveryLogDao = deliveryLogDao;
    }

    /**
     * 保存寄送成功紀錄。
     */
    public void recordSuccess(String email, String maskedRecipient, Purpose purpose) {
        deliveryLogDao.save(new EmailDeliveryLog(
            email,
            maskedRecipient,
            purpose,
            Status.SUCCESS,
            null,
            Instant.now()
        ));
    }

    /**
     * 保存經過截斷的安全失敗摘要。
     */
    public void recordFailure(String email, String maskedRecipient, Purpose purpose, String message) {
        String safeMessage = message == null ? "寄送失敗。" : message;
        if (safeMessage.length() > MAX_ERROR_LENGTH) {
            safeMessage = safeMessage.substring(0, MAX_ERROR_LENGTH);
        }
        deliveryLogDao.save(new EmailDeliveryLog(
            email,
            maskedRecipient,
            purpose,
            Status.FAILED,
            safeMessage,
            Instant.now()
        ));
    }

    /**
     * 取得最近二十筆寄送紀錄供管理員檢視。
     */
    public List<DeliveryLogResponse> recent() {
        return deliveryLogDao.findTop20ByOrderByCreatedAtDesc().stream()
            .map(log -> new DeliveryLogResponse(
                log.getId(),
                log.getMaskedRecipient(),
                log.getPurpose(),
                log.getStatus(),
                log.getErrorSummary(),
                log.getCompletedAt()
            ))
            .toList();
    }
}
