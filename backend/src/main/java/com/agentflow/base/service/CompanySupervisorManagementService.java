package com.agentflow.base.service;

import com.agentflow.base.dao.CompanyDao;
import com.agentflow.base.dao.CompanyMembershipDao;
import com.agentflow.base.dao.RoleDao;
import com.agentflow.base.dao.SupervisorProfileDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.Company;
import com.agentflow.base.model.bo.CompanyMembership;
import com.agentflow.base.model.bo.CompanyMembership.MemberType;
import com.agentflow.base.model.bo.SupervisorProfile;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.BindingRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.BindingResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.CompanyRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.CompanyResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.EmployeeBindingRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.EmployeeBindingResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.SupervisorCreateRequest;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.SupervisorResponse;
import com.agentflow.base.model.dto.CompanySupervisorManagementDtos.SupervisorUpdateRequest;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanySupervisorManagementService {
    private static final Logger log = LoggerFactory.getLogger(CompanySupervisorManagementService.class);
    private static final String MANAGER_ROLE = "MANAGER";
    private static final String EMPLOYEE_ROLE = "EMPLOYEE";

    private final CompanyDao companyDao;
    private final SupervisorProfileDao supervisorDao;
    private final CompanyMembershipDao membershipDao;
    private final UserAccountDao userDao;
    private final RoleDao roleDao;
    private final UserRoleDao userRoleDao;

    /**
     * 注入公司主管管理需要的資料存取元件。
     */
    public CompanySupervisorManagementService(
        CompanyDao companyDao,
        SupervisorProfileDao supervisorDao,
        CompanyMembershipDao membershipDao,
        UserAccountDao userDao,
        RoleDao roleDao,
        UserRoleDao userRoleDao
    ) {
        this.companyDao = companyDao;
        this.supervisorDao = supervisorDao;
        this.membershipDao = membershipDao;
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.userRoleDao = userRoleDao;
    }

    /**
     * 依名稱查詢公司；空字串代表全部。
     */
    @Transactional(readOnly = true)
    public List<CompanyResponse> findCompanies(String name) {
        String keyword = normalizeSearch(name);
        List<Company> companies = keyword.isEmpty()
            ? companyDao.findAllByOrderByNameAsc()
            : companyDao.findAllByNameContainingIgnoreCaseOrderByNameAsc(keyword);
        return companies.stream().map(this::toCompanyResponse).toList();
    }

    /**
     * 新增唯一名稱公司。
     */
    public CompanyResponse createCompany(CompanyRequest request) {
        String name = normalizeRequired(request.name(), "公司名稱不得為空。");
        ensureCompanyNameAvailable(name, null);
        Company company = companyDao.save(new Company(name, normalizeOptional(request.description())));
        log.info("建立公司 {}", company.getId());
        return toCompanyResponse(company);
    }

    /**
     * 修改公司名稱與說明。
     */
    public CompanyResponse updateCompany(UUID id, CompanyRequest request) {
        Company company = getCompany(id);
        String name = normalizeRequired(request.name(), "公司名稱不得為空。");
        ensureCompanyNameAvailable(name, id);
        company.update(name, normalizeOptional(request.description()));
        log.info("更新公司 {}", id);
        return toCompanyResponse(company);
    }

    /**
     * 刪除沒有成員綁定的公司。
     */
    public void deleteCompany(UUID id) {
        Company company = getCompany(id);
        if (membershipDao.existsByCompany(company)) {
            throw new BusinessException(HttpStatus.CONFLICT, "公司仍有主管或員工綁定，請先取消綁定。");
        }
        companyDao.delete(company);
        log.info("刪除公司 {}", id);
    }

    /**
     * 依姓名、帳號或職稱查詢主管。
     */
    @Transactional(readOnly = true)
    public List<SupervisorResponse> findSupervisors(String keyword) {
        return supervisorDao.search(normalizeSearch(keyword)).stream()
            .map(this::toSupervisorResponse)
            .toList();
    }

    /**
     * 由既有啟用使用者建立主管資料並授予角色。
     */
    public SupervisorResponse createSupervisor(SupervisorCreateRequest request) {
        UserAccount user = userDao.findById(request.userId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到已註冊使用者。"));
        if (!user.isActive()) {
            throw new BusinessException(HttpStatus.CONFLICT, "已停用的使用者不得建立為主管。");
        }
        if (supervisorDao.existsByUser(user)) {
            throw new BusinessException(HttpStatus.CONFLICT, "該使用者已是主管。");
        }

        // 主管資料與 RBAC 角色在同一交易內建立，避免只完成一半。
        SupervisorProfile supervisor = supervisorDao.save(
            new SupervisorProfile(user, normalizeRequired(request.title(), "主管職稱不得為空。"))
        );
        if (!userRoleDao.existsByUserAndRole_RoleCode(user, MANAGER_ROLE)) {
            var role = roleDao.findByRoleCode(MANAGER_ROLE)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到主管角色。"));
            userRoleDao.save(new UserRole(user, role));
        }
        log.info("建立主管 {} 使用者 {}", supervisor.getId(), user.getId());
        return toSupervisorResponse(supervisor);
    }

    /**
     * 修改主管職稱。
     */
    public SupervisorResponse updateSupervisor(UUID id, SupervisorUpdateRequest request) {
        SupervisorProfile supervisor = getSupervisor(id);
        supervisor.updateTitle(normalizeRequired(request.title(), "主管職稱不得為空。"));
        log.info("更新主管 {}", id);
        return toSupervisorResponse(supervisor);
    }

    /**
     * 移除未綁定公司的主管資料與附加角色。
     */
    public void deleteSupervisor(UUID id) {
        SupervisorProfile supervisor = getSupervisor(id);
        UserAccount user = supervisor.getUser();
        if (membershipDao.findByUser(user).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "主管仍綁定公司，請先取消綁定。");
        }

        // 先移除主管資料，再移除角色關聯，保持身分狀態一致。
        supervisorDao.delete(supervisor);
        userRoleDao.findByUserAndRole_RoleCode(user, MANAGER_ROLE).ifPresent(userRoleDao::delete);
        log.info("刪除主管 {} 使用者 {}", id, user.getId());
    }

    /**
     * 依公司與主管名稱查詢主管綁定。
     */
    @Transactional(readOnly = true)
    public List<BindingResponse> findBindings(String companyName, String supervisorName) {
        return membershipDao.searchSupervisorBindings(
                normalizeSearch(companyName),
                normalizeSearch(supervisorName)
            ).stream()
            .map(this::toBindingResponse)
            .toList();
    }

    /**
     * 建立公司主管綁定並強制一人一家公司。
     */
    public BindingResponse createBinding(BindingRequest request) {
        Company company = getCompany(request.companyId());
        SupervisorProfile supervisor = getSupervisor(request.supervisorId());
        UserAccount user = supervisor.getUser();
        if (membershipDao.findByUser(user).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "該主管已綁定公司，請先取消原綁定。");
        }

        CompanyMembership membership = membershipDao.save(
            new CompanyMembership(company, user, MemberType.SUPERVISOR)
        );
        log.info("綁定主管 {} 至公司 {}", supervisor.getId(), company.getId());
        return toBindingResponse(membership);
    }

    /**
     * 取消公司主管綁定。
     */
    public void deleteBinding(UUID id) {
        CompanyMembership membership = membershipDao.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到公司主管綁定。"));
        if (membership.getMemberType() != MemberType.SUPERVISOR) {
            throw new BusinessException(HttpStatus.CONFLICT, "此綁定不是主管綁定，無法由本頁取消。");
        }
        membershipDao.delete(membership);
        log.info("取消公司主管綁定 {}", id);
    }

    /**
     * 依公司與員工名稱查詢員工綁定。
     */
    @Transactional(readOnly = true)
    public List<EmployeeBindingResponse> findEmployeeBindings(String companyName, String employeeName) {
        return membershipDao.searchEmployeeBindings(
                normalizeSearch(companyName),
                normalizeSearch(employeeName)
            ).stream()
            .map(this::toEmployeeBindingResponse)
            .toList();
    }

    /**
     * 建立公司員工綁定並強制員工資格與一人一家公司。
     */
    public EmployeeBindingResponse createEmployeeBinding(EmployeeBindingRequest request) {
        Company company = getCompany(request.companyId());
        UserAccount user = userDao.findById(request.userId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到已註冊使用者。"));

        // 員工候選必須為啟用且具員工角色，避免管理員綁定不具員工身分的帳號。
        if (!user.isActive()) {
            throw new BusinessException(HttpStatus.CONFLICT, "已停用的使用者不得綁定為員工。");
        }
        if (!userRoleDao.existsByUserAndRole_RoleCode(user, EMPLOYEE_ROLE)) {
            throw new BusinessException(HttpStatus.CONFLICT, "該使用者不具員工角色。");
        }
        if (supervisorDao.existsByUser(user)) {
            throw new BusinessException(HttpStatus.CONFLICT, "該使用者已有主管身分，請使用主管綁定。");
        }
        if (membershipDao.findByUser(user).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "該員工已綁定公司，請先取消原綁定。");
        }

        CompanyMembership membership = membershipDao.save(
            new CompanyMembership(company, user, MemberType.EMPLOYEE)
        );
        log.info("綁定員工 {} 至公司 {}", user.getId(), company.getId());
        return toEmployeeBindingResponse(membership);
    }

    /**
     * 取消公司員工綁定。
     */
    public void deleteEmployeeBinding(UUID id) {
        CompanyMembership membership = membershipDao.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到公司員工綁定。"));
        if (membership.getMemberType() != MemberType.EMPLOYEE) {
            throw new BusinessException(HttpStatus.CONFLICT, "此綁定不是員工綁定，無法由員工流程取消。");
        }
        membershipDao.delete(membership);
        log.info("取消公司員工綁定 {}", id);
    }

    /**
     * 取得公司，不存在時回覆 404。
     */
    private Company getCompany(UUID id) {
        return companyDao.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到公司。"));
    }

    /**
     * 取得主管，不存在時回覆 404。
     */
    private SupervisorProfile getSupervisor(UUID id) {
        return supervisorDao.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到主管。"));
    }

    /**
     * 確認公司名稱未被其他公司使用。
     */
    private void ensureCompanyNameAvailable(String name, UUID currentId) {
        companyDao.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessException(HttpStatus.CONFLICT, "公司名稱已存在。");
            }
        });
    }

    /**
     * 將公司轉為 API 回應。
     */
    private CompanyResponse toCompanyResponse(Company company) {
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getDescription(),
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }

    /**
     * 將主管轉為 API 回應並附上目前公司。
     */
    private SupervisorResponse toSupervisorResponse(SupervisorProfile supervisor) {
        UserAccount user = supervisor.getUser();
        CompanyMembership membership = membershipDao.findByUser(user).orElse(null);
        return new SupervisorResponse(
            supervisor.getId(),
            user.getId(),
            user.getFullName(),
            user.getUsername(),
            user.getEmail(),
            supervisor.getTitle(),
            membership == null ? null : membership.getCompany().getId(),
            membership == null ? null : membership.getCompany().getName(),
            supervisor.getCreatedAt(),
            supervisor.getUpdatedAt()
        );
    }

    /**
     * 將綁定轉為 API 回應。
     */
    private BindingResponse toBindingResponse(CompanyMembership membership) {
        UserAccount user = membership.getUser();
        SupervisorProfile supervisor = supervisorDao.findByUser(user)
            .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "主管綁定缺少主管資料。"));
        return new BindingResponse(
            membership.getId(),
            membership.getCompany().getId(),
            membership.getCompany().getName(),
            supervisor.getId(),
            user.getId(),
            user.getFullName(),
            user.getUsername(),
            supervisor.getTitle(),
            membership.getCreatedAt()
        );
    }

    /**
     * 將員工綁定轉為 API 回應。
     */
    private EmployeeBindingResponse toEmployeeBindingResponse(CompanyMembership membership) {
        UserAccount user = membership.getUser();
        return new EmployeeBindingResponse(
            membership.getId(),
            membership.getCompany().getId(),
            membership.getCompany().getName(),
            user.getId(),
            user.getFullName(),
            user.getUsername(),
            user.getEmail(),
            membership.getCreatedAt()
        );
    }

    /**
     * 正規化必填文字。
     */
    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    /**
     * 正規化可選文字，空白轉為 null。
     */
    private String normalizeOptional(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /**
     * 正規化查詢字串供忽略大小寫比對。
     */
    private String normalizeSearch(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
