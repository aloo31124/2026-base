package com.agentflow.base.service;

import com.agentflow.base.dao.SessionTimeoutPolicyDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.SessionTimeoutPolicy;
import com.agentflow.base.model.dto.RegistrationManagementDtos.SessionTimeoutPolicyRequest;
import com.agentflow.base.model.dto.RegistrationManagementDtos.SessionTimeoutPolicyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionTimeoutPolicyService {
    private final SessionTimeoutPolicyDao policyDao;
    private final int defaultTimeoutMinutes;

    public SessionTimeoutPolicyService(
        SessionTimeoutPolicyDao policyDao,
        @Value("${app.jwt.expiration-minutes}") int defaultTimeoutMinutes
    ) {
        validateConfiguredDefault(defaultTimeoutMinutes);
        this.policyDao = policyDao;
        this.defaultTimeoutMinutes = defaultTimeoutMinutes;
    }

    /**
     * 取得後台顯示的目前政策；初次查詢時將部署預設值落庫。
     *
     * @return 目前登出時間政策
     */
    @Transactional
    public SessionTimeoutPolicyResponse getPolicy() {
        return toResponse(requirePolicy());
    }

    /**
     * 驗證並更新後續新 JWT 使用的登入效期。
     *
     * @param request 管理員提交的分鐘數
     * @return 更新後政策
     */
    @Transactional
    public SessionTimeoutPolicyResponse updatePolicy(SessionTimeoutPolicyRequest request) {
        validateTimeoutMinutes(request.timeoutMinutes());
        SessionTimeoutPolicy policy = requirePolicy();
        policy.update(request.timeoutMinutes());
        return toResponse(policyDao.save(policy));
    }

    /**
     * 提供 JwtService 簽發 token 時使用的即時效期。
     *
     * @return 目前效期分鐘數
     */
    @Transactional(readOnly = true)
    public int currentTimeoutMinutes() {
        return policyDao.findFirstByOrderByCreatedAtAsc()
            .map(SessionTimeoutPolicy::getTimeoutMinutes)
            .orElse(defaultTimeoutMinutes);
    }

    private SessionTimeoutPolicy requirePolicy() {
        return policyDao.findFirstByOrderByCreatedAtAsc()
            .orElseGet(() -> policyDao.save(new SessionTimeoutPolicy(defaultTimeoutMinutes)));
    }

    private void validateTimeoutMinutes(int timeoutMinutes) {
        if (
            timeoutMinutes < SessionTimeoutPolicy.MIN_TIMEOUT_MINUTES
                || timeoutMinutes > SessionTimeoutPolicy.MAX_TIMEOUT_MINUTES
        ) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "登出時間必須介於 5 與 1440 分鐘之間。");
        }
    }

    private void validateConfiguredDefault(int timeoutMinutes) {
        if (
            timeoutMinutes < SessionTimeoutPolicy.MIN_TIMEOUT_MINUTES
                || timeoutMinutes > SessionTimeoutPolicy.MAX_TIMEOUT_MINUTES
        ) {
            throw new IllegalArgumentException("JWT_EXPIRATION_MINUTES 必須介於 5 與 1440 之間。");
        }
    }

    private SessionTimeoutPolicyResponse toResponse(SessionTimeoutPolicy policy) {
        return new SessionTimeoutPolicyResponse(
            policy.getTimeoutMinutes(),
            policy.getUpdatedAt()
        );
    }
}
