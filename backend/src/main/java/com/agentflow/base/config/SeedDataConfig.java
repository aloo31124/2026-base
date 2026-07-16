package com.agentflow.base.config;

import com.agentflow.base.dao.RoleDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.model.bo.Role;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class SeedDataConfig {
    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);
    @Bean CommandLineRunner seedData(SeedService seedService) { return args -> seedService.seed(); }

    @org.springframework.stereotype.Service
    static class SeedService {
        private final RoleDao roleDao; private final UserAccountDao userDao; private final UserRoleDao userRoleDao; private final PasswordEncoder encoder;
        SeedService(RoleDao roleDao, UserAccountDao userDao, UserRoleDao userRoleDao, PasswordEncoder encoder) { this.roleDao = roleDao; this.userDao = userDao; this.userRoleDao = userRoleDao; this.encoder = encoder; }
        @Transactional void seed() {
            Role admin = role("SYSTEM_ADMIN", "系統管理員"); role("MANAGER", "主管"); Role employee = role("EMPLOYEE", "員工");
            if (userDao.count() == 0) {
                create("System Admin", "admin", "admin@agentflow.local", "admin123", List.of(admin, employee));
                create("System Admin 2", "admin2", "admin2@agentflow.local", "admin234", List.of(admin, employee));
                create("Demo User", "user", "user@agentflow.local", "admin123", List.of(employee));
                create("Demo User 2", "user2", "user2@agentflow.local", "admin234", List.of(employee));
                log.info("已建立 2 組系統管理員與 2 組一般使用者初始帳號");
            }
        }
        private Role role(String code, String name) { return roleDao.findByRoleCode(code).orElseGet(() -> roleDao.save(new Role(code, name))); }
        private void create(String name, String username, String email, String password, List<Role> roles) {
            UserAccount user = userDao.save(new UserAccount(name, username, email, encoder.encode(password), "系統初始化"));
            roles.forEach(role -> userRoleDao.save(new UserRole(user, role)));
        }
    }
}

