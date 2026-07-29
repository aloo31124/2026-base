package com.agentflow.base.service;

import com.agentflow.base.dao.PasswordPolicyDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.PasswordPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordPolicyService {
    private final PasswordPolicyDao dao;
    public PasswordPolicyService(PasswordPolicyDao dao) { this.dao = dao; }

    @Transactional
    public PasswordPolicy current() {
        return dao.findByPolicyKey("DEFAULT").orElseGet(() -> dao.save(new PasswordPolicy(8, true, true)));
    }
    public void validate(String password) {
        PasswordPolicy policy = current();
        if (password == null || password.length() < policy.getMinimumLength() || password.length() > 128)
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密碼長度須為 " + policy.getMinimumLength() + " 至 128 字元。");
        if (policy.isRequireEnglish() && !password.matches(".*[A-Za-z].*"))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密碼必須包含英文字母。");
        if (policy.isRequireDigit() && !password.matches(".*[0-9].*"))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密碼必須包含數字。");
    }
}
