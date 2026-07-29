package com.agentflow.base.service;

import com.agentflow.base.dao.PasswordPolicyDao;
import com.agentflow.base.dao.RegistrationRecordDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.PasswordPolicy;
import com.agentflow.base.model.bo.RegistrationRecord;
import com.agentflow.base.model.bo.RegistrationRecord.Method;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.dto.RegistrationManagementDtos.PasswordPolicyRequest;
import com.agentflow.base.model.dto.RegistrationManagementDtos.PasswordPolicyResponse;
import com.agentflow.base.model.dto.RegistrationManagementDtos.RegistrationRecordResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationManagementService {
    private static final int REGISTRATION_RECORD_LIMIT = 100;
    private final PasswordPolicyDao passwordPolicyDao;
    private final RegistrationRecordDao registrationRecordDao;

    public RegistrationManagementService(
        PasswordPolicyDao passwordPolicyDao,
        RegistrationRecordDao registrationRecordDao
    ) {
        this.passwordPolicyDao = passwordPolicyDao;
        this.registrationRecordDao = registrationRecordDao;
    }

    /**
     * 取得目前密碼政策；初次使用時建立安全預設值。
     *
     * @return 目前密碼政策
     */
    @Transactional
    public PasswordPolicyResponse getPolicy() {
        return toResponse(requirePolicy());
    }

    /**
     * 驗證並更新系統密碼政策。
     *
     * @param request 管理員提交的政策
     * @return 更新後政策
     */
    @Transactional
    public PasswordPolicyResponse updatePolicy(PasswordPolicyRequest request) {
        validateLength(request.minLength());
        PasswordPolicy policy = requirePolicy();
        policy.update(request.minLength(), request.requireLetter(), request.requireNumber());
        return toResponse(passwordPolicyDao.save(policy));
    }

    /**
     * 依目前政策驗證註冊或重設用的新密碼。
     *
     * @param password 待驗證的明文密碼
     */
    @Transactional(readOnly = true)
    public void validatePassword(String password) {
        PasswordPolicy policy = passwordPolicyDao.findFirstByOrderByCreatedAtAsc()
            .orElseGet(() -> new PasswordPolicy(PasswordPolicy.DEFAULT_MIN_LENGTH, true, true));
        List<String> missingRules = new ArrayList<>();

        // 一次收集全部缺少條件，讓使用者不必逐次嘗試。
        if (password.length() < policy.getMinLength()) {
            missingRules.add("至少 " + policy.getMinLength() + " 位");
        }
        if (policy.isRequireLetter() && password.chars().noneMatch(value -> isAsciiLetter((char) value))) {
            missingRules.add("至少一個英文字母");
        }
        if (policy.isRequireNumber() && password.chars().noneMatch(value -> value >= '0' && value <= '9')) {
            missingRules.add("至少一個數字");
        }
        if (!missingRules.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密碼不符合政策：" + String.join("、", missingRules) + "。");
        }
    }

    /**
     * 寫入首次成功建帳紀錄；同一使用者不重複寫入。
     *
     * @param user 新建立的使用者
     * @param method 註冊方式
     */
    @Transactional
    public void recordSuccess(UserAccount user, Method method) {
        if (registrationRecordDao.existsByUser(user)) {
            return;
        }
        registrationRecordDao.save(new RegistrationRecord(user, method, user.getEmail(), Instant.now()));
    }

    /**
     * 取得最近一百筆註冊紀錄。
     *
     * @return 依完成時間倒序的紀錄
     */
    @Transactional(readOnly = true)
    public List<RegistrationRecordResponse> findRecentRegistrations() {
        return registrationRecordDao.findAllByOrderByCompletedAtDesc(PageRequest.of(0, REGISTRATION_RECORD_LIMIT))
            .stream()
            .map(record -> new RegistrationRecordResponse(
                record.getId(),
                record.getMethod(),
                record.getIdentifier(),
                record.isSuccess(),
                record.getCompletedAt()
            ))
            .toList();
    }

    /**
     * 取得或建立唯一政策資料。
     */
    private PasswordPolicy requirePolicy() {
        return passwordPolicyDao.findFirstByOrderByCreatedAtAsc()
            .orElseGet(() -> passwordPolicyDao.save(
                new PasswordPolicy(PasswordPolicy.DEFAULT_MIN_LENGTH, true, true)
            ));
    }

    /**
     * 驗證政策長度邊界。
     */
    private void validateLength(int minLength) {
        if (minLength < PasswordPolicy.DEFAULT_MIN_LENGTH || minLength > PasswordPolicy.MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密碼最小長度必須介於 8 與 72 之間。");
        }
    }

    /**
     * 判斷字元是否為 ASCII 英文字母。
     */
    private boolean isAsciiLetter(char value) {
        return (value >= 'A' && value <= 'Z') || (value >= 'a' && value <= 'z');
    }

    /**
     * 將 BO 轉為 API response。
     */
    private PasswordPolicyResponse toResponse(PasswordPolicy policy) {
        return new PasswordPolicyResponse(
            policy.getMinLength(),
            policy.isRequireLetter(),
            policy.isRequireNumber(),
            policy.getUpdatedAt()
        );
    }
}
