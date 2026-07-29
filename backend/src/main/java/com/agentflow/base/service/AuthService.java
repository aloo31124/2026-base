package com.agentflow.base.service;

import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.dto.AuthDtos.LoginRequest;
import com.agentflow.base.model.dto.AuthDtos.LoginResponse;
import com.agentflow.base.security.JwtService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager; private final UserAccountDao userDao; private final UserRoleDao userRoleDao; private final JwtService jwtService;
    public AuthService(AuthenticationManager authenticationManager, UserAccountDao userDao, UserRoleDao userRoleDao, JwtService jwtService) { this.authenticationManager = authenticationManager; this.userDao = userDao; this.userRoleDao = userRoleDao; this.jwtService = jwtService; }
    public LoginResponse login(LoginRequest request) {
        String username = request.username().contains("@") ? request.username().trim().toLowerCase() : request.username();
        log.info("登入驗證: {}", username);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        var user = userDao.findByUsername(username).orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤。"));
        List<String> roles = userRoleDao.findAllByUser(user).stream().map(r -> r.getRole().getRoleCode()).toList();
        return new LoginResponse(jwtService.create(user.getUsername(), roles), "Bearer", user.getUsername(), user.getFullName(), roles);
    }
}
