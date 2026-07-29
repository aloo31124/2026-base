package com.agentflow.base.service;

import com.agentflow.base.dao.RoleDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import com.agentflow.base.model.dto.UserDtos.UserRequest;
import com.agentflow.base.model.dto.UserDtos.UserResponse;
import com.agentflow.base.model.dto.UserDtos.UserUpdateRequest;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserAccountDao userDao; private final RoleDao roleDao; private final UserRoleDao userRoleDao; private final PasswordEncoder encoder; private final PasswordPolicyService policyService;
    public UserService(UserAccountDao userDao, RoleDao roleDao, UserRoleDao userRoleDao, PasswordEncoder encoder, PasswordPolicyService policyService) { this.userDao = userDao; this.roleDao = roleDao; this.userRoleDao = userRoleDao; this.encoder = encoder; this.policyService = policyService; }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() { log.info("查詢使用者列表"); return userDao.findAll().stream().map(this::toResponse).toList(); }

    public UserResponse create(UserRequest request) {
        log.info("管理員新增使用者 {}", request.username());
        if (userDao.findByUsername(request.username()).isPresent() || userDao.existsByEmail(request.email())) throw new BusinessException(HttpStatus.CONFLICT, "帳號或信箱已存在。");
        policyService.validate(request.password());
        UserAccount user = userDao.save(new UserAccount(request.fullName(), request.username(), request.email(), encoder.encode(request.password()), "管理員新增"));
        assign(user, "EMPLOYEE"); return toResponse(user);
    }

    public UserResponse update(UUID id, UserUpdateRequest request) {
        UserAccount user = get(id); user.update(request.fullName(), request.email());
        if (request.password() != null && !request.password().isBlank()) { policyService.validate(request.password()); user.updatePassword(encoder.encode(request.password())); }
        log.info("更新使用者 {}", id); return toResponse(user);
    }

    public UserResponse disable(UUID id) { UserAccount user = get(id); user.disable(); log.info("停用使用者 {}", id); return toResponse(user); }
    public UserResponse assignRole(UUID id, String roleCode) { UserAccount user = get(id); assign(user, roleCode); log.info("授予使用者 {} 角色 {}", id, roleCode); return toResponse(user); }

    private void assign(UserAccount user, String code) {
        if (!userRoleDao.existsByUserAndRole_RoleCode(user, code)) {
            var role = roleDao.findByRoleCode(code).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "角色不存在：" + code));
            userRoleDao.save(new UserRole(user, role));
        }
    }
    private UserAccount get(UUID id) { return userDao.findById(id).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到使用者。")); }
    private UserResponse toResponse(UserAccount user) { return new UserResponse(user.getId(), user.getFullName(), user.getUsername(), user.getEmail(), user.getRegistrationMethod(), user.isActive(), userRoleDao.findAllByUser(user).stream().map(r -> r.getRole().getRoleCode()).toList()); }
}
