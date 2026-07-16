package com.agentflow.base.service;

import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final UserAccountDao userDao;
    private final UserRoleDao userRoleDao;
    public AccountService(UserAccountDao userDao, UserRoleDao userRoleDao) { this.userDao = userDao; this.userRoleDao = userRoleDao; }

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("查詢登入帳號 {}", username);
        var account = userDao.findByUsername(username).orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤。"));
        var authorities = userRoleDao.findAllByUser(account).stream().map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getRoleCode())).collect(Collectors.toSet());
        return User.withUsername(account.getUsername()).password(account.getPasswordHash()).disabled(!account.isActive()).authorities(authorities).build();
    }
}

