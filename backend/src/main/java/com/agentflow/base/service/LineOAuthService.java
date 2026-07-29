package com.agentflow.base.service;

import com.agentflow.base.config.LineOAuthProperties;
import com.agentflow.base.dao.LineOAuthAccountDao;
import com.agentflow.base.dao.LineOAuthAttemptDao;
import com.agentflow.base.dao.RoleDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.LineOAuthAccount;
import com.agentflow.base.model.bo.LineOAuthAttempt;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import com.agentflow.base.model.dto.AuthDtos.LoginResponse;
import com.agentflow.base.model.dto.LineOAuthDtos.AuthorizeResponse;
import com.agentflow.base.model.dto.LineOAuthDtos.CallbackRequest;
import com.agentflow.base.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineOAuthService {
    private static final Logger log = LoggerFactory.getLogger(LineOAuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final LineOAuthClient client;
    private final LineOAuthProperties properties;
    private final LineOAuthAttemptDao attemptDao;
    private final LineOAuthAccountDao accountDao;
    private final UserAccountDao userDao;
    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LineOAuthService(
        LineOAuthClient client,
        LineOAuthProperties properties,
        LineOAuthAttemptDao attemptDao,
        LineOAuthAccountDao accountDao,
        UserAccountDao userDao,
        UserRoleDao userRoleDao,
        RoleDao roleDao,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.client = client;
        this.properties = properties;
        this.attemptDao = attemptDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthorizeResponse authorize() {
        String state = randomUrlToken(32);
        String nonce = randomUrlToken(32);
        String codeVerifier = randomUrlToken(64);
        String codeChallenge = base64Url(sha256Bytes(codeVerifier));
        Instant expiresAt = Instant.now().plus(properties.getAttemptTtlMinutes(), ChronoUnit.MINUTES);
        String authorizationUrl = client.buildAuthorizationUrl(state, nonce, codeChallenge);
        attemptDao.save(new LineOAuthAttempt(sha256Hex(state), codeVerifier, nonce, expiresAt));
        log.info("已建立 LINE OAuth 授權流程，有效期限至 {}", expiresAt);
        return new AuthorizeResponse(authorizationUrl, expiresAt);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse callback(CallbackRequest request) {
        Instant now = Instant.now();
        String stateHash = sha256Hex(request.state());
        LineOAuthAttempt attempt = attemptDao.findByStateHash(stateHash).orElseGet(() -> {
            LineOAuthAttempt unknown = LineOAuthAttempt.unknownStateFailure(stateHash, now);
            attemptDao.save(unknown);
            return unknown;
        });
        if (!attempt.isPending()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "LINE 登入 state 無效或已使用，請重新登入。");
        }
        if (attempt.isExpired(now)) {
            attempt.fail("STATE_EXPIRED", now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "LINE 登入已逾時，請重新登入。");
        }
        if (request.error() != null && !request.error().isBlank()) {
            attempt.deny("ACCESS_DENIED", now);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "LINE 授權已取消，請重新操作。");
        }
        if (request.code() == null || request.code().isBlank()) {
            attempt.fail("INVALID_CALLBACK", now);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "LINE callback 缺少授權碼。");
        }

        try {
            LineOAuthClient.Profile profile = client.exchangeAndVerify(request.code(), attempt.getCodeVerifier(), attempt.getNonce());
            UserAccount user = findOrCreateUser(profile);
            if (!user.isActive()) {
                attempt.fail("ACCOUNT_DISABLED", now);
                throw new BusinessException(HttpStatus.FORBIDDEN, "此帳號已停用，無法登入。");
            }
            List<String> roles = userRoleDao.findAllByUser(user).stream().map(row -> row.getRole().getRoleCode()).toList();
            attempt.succeed(user, now);
            log.info("LINE OAuth 登入成功，帳號 {}", user.getUsername());
            return new LoginResponse(jwtService.create(user.getUsername(), roles), "Bearer", user.getUsername(), user.getFullName(), roles);
        } catch (BusinessException ex) {
            if (attempt.isPending()) attempt.fail("PROVIDER_ERROR", now);
            throw ex;
        } catch (RuntimeException ex) {
            if (attempt.isPending()) attempt.fail("PROVIDER_ERROR", now);
            log.warn("LINE OAuth 登入流程失敗，resultCode={}", attempt.getResultCode());
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "LINE 驗證服務暫時無法使用。");
        }
    }

    private UserAccount findOrCreateUser(LineOAuthClient.Profile profile) {
        return accountDao.findByLineUserId(profile.lineUserId()).map(account -> {
            account.updateProfile(profile.displayName(), profile.pictureUrl());
            return account.getUser();
        }).orElseGet(() -> createUser(profile));
    }

    private UserAccount createUser(LineOAuthClient.Profile profile) {
        String identityHash = sha256Hex(profile.lineUserId()).substring(0, 20);
        String username = uniqueUsername("line_" + identityHash);
        String fallbackEmail = "line_" + identityHash + "@oauth.invalid";
        String email = profile.email() == null || profile.email().isBlank() || userDao.existsByEmail(profile.email())
            ? fallbackEmail : profile.email();
        String unusablePassword = passwordEncoder.encode(randomUrlToken(48));
        UserAccount user = userDao.save(new UserAccount(profile.displayName(), username, email, unusablePassword, "LINE OAuth"));
        accountDao.save(new LineOAuthAccount(user, profile.lineUserId(), profile.displayName(), profile.pictureUrl()));
        var employee = roleDao.findByRoleCode("EMPLOYEE")
            .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "系統缺少 EMPLOYEE 角色設定。"));
        userRoleDao.save(new UserRole(user, employee));
        log.info("已建立 LINE OAuth 首次註冊帳號 {}", username);
        return user;
    }

    private String uniqueUsername(String base) {
        if (!userDao.existsByUsername(base)) return base;
        return base.substring(0, Math.min(base.length(), 50)) + "_" + randomUrlToken(5);
    }

    private static String randomUrlToken(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return base64Url(value);
    }

    private static String sha256Hex(String value) {
        return HexFormat.of().formatHex(sha256Bytes(value));
    }

    private static byte[] sha256Bytes(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
