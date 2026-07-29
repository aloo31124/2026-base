package com.agentflow.base.service;

import com.agentflow.base.dao.RoleDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.EmailVerification;
import com.agentflow.base.model.bo.EmailVerification.Purpose;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import com.agentflow.base.model.dto.AuthDtos.LoginResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailRegistrationService {
    private static final String DEFAULT_ROLE = "EMPLOYEE";
    private final UserAccountDao userAccountDao;
    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService verificationService;
    private final AuthService authService;

    public EmailRegistrationService(
        UserAccountDao userAccountDao,
        UserRoleDao userRoleDao,
        RoleDao roleDao,
        PasswordEncoder passwordEncoder,
        EmailVerificationService verificationService,
        AuthService authService
    ) {
        this.userAccountDao = userAccountDao;
        this.userRoleDao = userRoleDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.authService = authService;
    }

    /**
     * 使用一次性註冊票券建立一般使用者並回傳登入工作階段。
     */
    @Transactional
    public LoginResponse register(
        String email,
        UUID ticketId,
        String password,
        String confirmPassword
    ) {
        String normalizedEmail = verificationService.normalizeEmail(email);
        validatePasswords(password, confirmPassword);
        if (userAccountDao.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException(HttpStatus.CONFLICT, "此信箱已註冊。");
        }

        // 先驗證票券，再於同一交易建立帳號、角色並將票券標為已使用。
        EmailVerification verification = verificationService.requireUsableTicket(
            ticketId,
            normalizedEmail,
            Purpose.REGISTRATION
        );
        String localPart = normalizedEmail.substring(0, normalizedEmail.indexOf('@'));
        UserAccount account = userAccountDao.save(new UserAccount(
            localPart,
            normalizedEmail,
            normalizedEmail,
            passwordEncoder.encode(password),
            "信箱註冊"
        ));
        var employeeRole = roleDao.findByRoleCode(DEFAULT_ROLE)
            .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "預設使用者角色尚未建立。"));
        userRoleDao.save(new UserRole(account, employeeRole));
        verificationService.consume(verification);
        return authService.createSession(account);
    }

    /**
     * 使用一次性重設票券更新密碼雜湊。
     */
    @Transactional
    public void resetPassword(
        String email,
        UUID ticketId,
        String password,
        String confirmPassword
    ) {
        String normalizedEmail = verificationService.normalizeEmail(email);
        validatePasswords(password, confirmPassword);
        UserAccount account = userAccountDao.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "查無此信箱帳號。"));
        EmailVerification verification = verificationService.requireUsableTicket(
            ticketId,
            normalizedEmail,
            Purpose.PASSWORD_RESET
        );

        // 密碼與票券狀態在同一交易更新，避免只完成其中一項。
        account.updatePassword(passwordEncoder.encode(password));
        userAccountDao.save(account);
        verificationService.consume(verification);
    }

    /**
     * 驗證兩次密碼輸入一致，長度由 DTO 邊界另行檢查。
     */
    private void validatePasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密碼與確認密碼不一致。");
        }
    }
}
