package com.agentflow.base.service;

import com.agentflow.base.dao.EmailVerificationDao;
import com.agentflow.base.dao.RoleDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.EmailVerification;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import com.agentflow.base.model.dto.AuthDtos.LoginResponse;
import com.agentflow.base.model.dto.EmailAuthDtos.CodeResponse;
import com.agentflow.base.model.dto.EmailAuthDtos.PasswordResetRequest;
import com.agentflow.base.model.dto.EmailAuthDtos.RegistrationRequest;
import com.agentflow.base.security.JwtService;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailAuthService {
    private static final Logger log = LoggerFactory.getLogger(EmailAuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final String REGISTER = "REGISTER";
    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    private final EmailVerificationDao verificationDao; private final UserAccountDao userDao;
    private final RoleDao roleDao; private final UserRoleDao userRoleDao; private final PasswordEncoder encoder;
    private final PasswordPolicyService policyService; private final VerificationEmailSender emailSender; private final JwtService jwtService;
    public EmailAuthService(EmailVerificationDao verificationDao, UserAccountDao userDao, RoleDao roleDao,
        UserRoleDao userRoleDao, PasswordEncoder encoder, PasswordPolicyService policyService,
        VerificationEmailSender emailSender, JwtService jwtService) {
        this.verificationDao=verificationDao; this.userDao=userDao; this.roleDao=roleDao; this.userRoleDao=userRoleDao;
        this.encoder=encoder; this.policyService=policyService; this.emailSender=emailSender; this.jwtService=jwtService;
    }

    @Transactional
    public CodeResponse sendRegistrationCode(String rawEmail) {
        String email = normalize(rawEmail);
        if (userDao.existsByEmail(email)) throw new BusinessException(HttpStatus.CONFLICT, "此信箱已註冊。");
        return issue(email, REGISTER);
    }

    @Transactional
    public CodeResponse sendResetCode(String rawEmail) {
        String email = normalize(rawEmail);
        if (userDao.findByEmailIgnoreCase(email).isEmpty()) {
            Instant now = Instant.now();
            return new CodeResponse(now.plus(10, ChronoUnit.MINUTES), now.plus(60, ChronoUnit.SECONDS));
        }
        return issue(email, RESET_PASSWORD);
    }

    private CodeResponse issue(String email, String purpose) {
        Instant now = Instant.now();
        var latest = verificationDao.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);
        if (latest.isPresent() && latest.get().isPending() && now.isBefore(latest.get().getResendAvailableAt()))
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "驗證碼寄送過於頻繁，請稍後再試。");
        verificationDao.findAllByEmailAndPurposeAndStatus(email, purpose, "PENDING").forEach(EmailVerification::supersede);
        String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        Instant expiresAt = now.plus(10, ChronoUnit.MINUTES);
        Instant resendAt = now.plus(60, ChronoUnit.SECONDS);
        verificationDao.save(new EmailVerification(email, purpose, encoder.encode(code), expiresAt, resendAt));
        emailSender.send(email, purpose, code);
        log.info("已寄送 {} 信箱驗證碼至遮罩地址 {}", purpose, mask(email));
        return new CodeResponse(expiresAt, resendAt);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse register(RegistrationRequest request) {
        String email = normalize(request.email());
        if (userDao.existsByEmail(email)) throw new BusinessException(HttpStatus.CONFLICT, "此信箱已註冊。");
        policyService.validate(request.password());
        EmailVerification verification = verify(email, REGISTER, request.code());
        UserAccount user = userDao.save(new UserAccount(request.fullName().trim(), email, email, encoder.encode(request.password()), "信箱註冊"));
        var role = roleDao.findByRoleCode("EMPLOYEE").orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "系統缺少 EMPLOYEE 角色設定。"));
        userRoleDao.save(new UserRole(user, role));
        verification.consume("REGISTRATION_SUCCESS");
        List<String> roles = List.of("EMPLOYEE");
        log.info("信箱首次註冊成功：{}", mask(email));
        return new LoginResponse(jwtService.create(email, roles), "Bearer", email, user.getFullName(), roles);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void resetPassword(PasswordResetRequest request) {
        String email = normalize(request.email());
        policyService.validate(request.newPassword());
        EmailVerification verification = verify(email, RESET_PASSWORD, request.code());
        UserAccount user = userDao.findByEmailIgnoreCase(email).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "驗證碼無效或已失效。"));
        user.updatePassword(encoder.encode(request.newPassword()));
        verification.consume("PASSWORD_RESET_SUCCESS");
        log.info("信箱密碼重設成功：{}", mask(email));
    }

    private EmailVerification verify(String email, String purpose, String code) {
        EmailVerification row = verificationDao.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "驗證碼無效或已失效。"));
        if (!row.isPending()) throw new BusinessException(HttpStatus.BAD_REQUEST, "驗證碼無效或已失效。");
        if (Instant.now().isAfter(row.getExpiresAt())) { row.expire(); throw new BusinessException(HttpStatus.BAD_REQUEST, "驗證碼已過期，請重新取得。"); }
        if (!encoder.matches(code, row.getCodeHash())) { row.failedAttempt(); throw new BusinessException(HttpStatus.BAD_REQUEST, "驗證碼錯誤。"); }
        row.verify(); return row;
    }
    private static String normalize(String email) { return email.trim().toLowerCase(); }
    private static String mask(String email) { int at=email.indexOf('@'); return at<2?"***"+email.substring(Math.max(0,at)):email.charAt(0)+"***"+email.substring(at); }
}
