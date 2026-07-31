package com.agentflow.base.service;

import com.agentflow.base.dao.AssignedTaskDao;
import com.agentflow.base.dao.CompanyDao;
import com.agentflow.base.dao.CompanyMembershipDao;
import com.agentflow.base.dao.SupervisorEmployeeBindingDao;
import com.agentflow.base.dao.SupervisorProfileDao;
import com.agentflow.base.dao.UserAccountDao;
import com.agentflow.base.dao.UserRoleDao;
import com.agentflow.base.exception.BusinessException;
import com.agentflow.base.model.bo.AssignedTask;
import com.agentflow.base.model.bo.AssignedTask.Status;
import com.agentflow.base.model.bo.Company;
import com.agentflow.base.model.bo.CompanyMembership;
import com.agentflow.base.model.bo.CompanyMembership.MemberType;
import com.agentflow.base.model.bo.SupervisorEmployeeBinding;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.dto.TaskAssignmentDtos.AssigneeResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.CompanyBindingRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.ContextResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.EmployeeBindingRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.EmployeeBindingResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.EmployeeResponse;
import com.agentflow.base.model.dto.TaskAssignmentDtos.ReturnRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.TaskRequest;
import com.agentflow.base.model.dto.TaskAssignmentDtos.TaskResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskAssignmentService {
    private static final Map<String, String> SORT_FIELDS = Map.of(
        "name", "name",
        "assignee", "assignee.username",
        "assignedAt", "assignedAt",
        "deadline", "deadline",
        "status", "status"
    );

    private final UserAccountDao userDao;
    private final UserRoleDao userRoleDao;
    private final CompanyDao companyDao;
    private final CompanyMembershipDao membershipDao;
    private final SupervisorProfileDao supervisorDao;
    private final SupervisorEmployeeBindingDao employeeBindingDao;
    private final AssignedTaskDao taskDao;

    /** 注入任務指派所需的資料存取元件。 */
    public TaskAssignmentService(
        UserAccountDao userDao,
        UserRoleDao userRoleDao,
        CompanyDao companyDao,
        CompanyMembershipDao membershipDao,
        SupervisorProfileDao supervisorDao,
        SupervisorEmployeeBindingDao employeeBindingDao,
        AssignedTaskDao taskDao
    ) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.companyDao = companyDao;
        this.membershipDao = membershipDao;
        this.supervisorDao = supervisorDao;
        this.employeeBindingDao = employeeBindingDao;
        this.taskDao = taskDao;
    }

    /** 取得目前登入者的公司與角色情境。 */
    @Transactional(readOnly = true)
    public ContextResponse context(String username) {
        UserAccount user = currentUser(username);
        CompanyMembership membership = membershipDao.findByUser(user).orElse(null);
        List<String> roles = userRoleDao.findAllByUser(user).stream()
            .map(row -> row.getRole().getRoleCode())
            .sorted()
            .toList();
        return new ContextResponse(
            user.getId(),
            user.getUsername(),
            roles,
            membership == null ? null : membership.getCompany().getId(),
            membership == null ? null : membership.getCompany().getName()
        );
    }

    /** 依既有公司名稱綁定目前使用者。 */
    public ContextResponse bindCurrentUserCompany(String username, CompanyBindingRequest request) {
        UserAccount user = currentUser(username);
        if (membershipDao.findByUser(user).isPresent()) {
            throw conflict("目前使用者已綁定公司。");
        }
        Company company = companyDao.findByNameIgnoreCase(request.companyName().trim())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到指定公司。"));
        MemberType type = supervisorDao.existsByUser(user) ? MemberType.SUPERVISOR : MemberType.EMPLOYEE;
        membershipDao.save(new CompanyMembership(company, user, type));
        return context(username);
    }

    /** 主管依信箱搜尋同公司員工。 */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findEmployees(String username, String email) {
        UserAccount supervisor = requireSupervisor(username);
        Company company = requireMembership(supervisor).getCompany();
        return membershipDao.findAllByCompanyAndMemberTypeOrderByUser_FullNameAsc(company, MemberType.EMPLOYEE)
            .stream()
            .map(CompanyMembership::getUser)
            .filter(user -> normalize(user.getEmail()).contains(normalize(email)))
            .map(user -> {
                UUID bindingId = employeeBindingDao.findByEmployee(user)
                    .map(SupervisorEmployeeBinding::getId)
                    .orElse(null);
                return new EmployeeResponse(user.getId(), user.getFullName(), user.getUsername(), user.getEmail(), bindingId);
            })
            .toList();
    }

    /** 取得目前主管的員工綁定。 */
    @Transactional(readOnly = true)
    public List<EmployeeBindingResponse> findEmployeeBindings(String username) {
        UserAccount supervisor = requireSupervisor(username);
        return employeeBindingDao.findAllBySupervisorOrderByEmployee_FullNameAsc(supervisor).stream()
            .map(this::toEmployeeBindingResponse)
            .toList();
    }

    /** 建立目前主管與同公司員工的唯一綁定。 */
    public EmployeeBindingResponse bindEmployee(String username, EmployeeBindingRequest request) {
        UserAccount supervisor = requireSupervisor(username);
        UserAccount employee = userDao.findById(request.employeeId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到員工。"));
        ensureSameCompany(supervisor, employee);
        CompanyMembership employeeMembership = requireMembership(employee);
        if (employeeMembership.getMemberType() != MemberType.EMPLOYEE) {
            throw conflict("只有員工身分可以綁定至主管。");
        }
        if (employeeBindingDao.existsByEmployee(employee)) {
            throw conflict("該員工已綁定主管。");
        }
        return toEmployeeBindingResponse(employeeBindingDao.save(
            new SupervisorEmployeeBinding(supervisor, employee)
        ));
    }

    /** 取消目前主管擁有的員工綁定。 */
    public void unbindEmployee(String username, UUID id) {
        UserAccount supervisor = requireSupervisor(username);
        SupervisorEmployeeBinding binding = employeeBindingDao.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到主管員工綁定。"));
        if (!binding.getSupervisor().getId().equals(supervisor.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能取消自己的員工綁定。");
        }
        employeeBindingDao.delete(binding);
    }

    /** 取得自己、同公司主管及旗下員工，並依使用者去重。 */
    @Transactional(readOnly = true)
    public List<AssigneeResponse> findAssignees(String username) {
        UserAccount supervisor = requireSupervisor(username);
        Company company = requireMembership(supervisor).getCompany();
        Map<UUID, AssigneeResponse> result = new LinkedHashMap<>();
        result.put(supervisor.getId(), assignee(supervisor, "SELF"));

        membershipDao.findAllByCompanyAndMemberTypeOrderByUser_FullNameAsc(company, MemberType.SUPERVISOR)
            .stream()
            .map(CompanyMembership::getUser)
            .forEach(user -> result.put(user.getId(), assignee(user, "SUPERVISOR")));
        employeeBindingDao.findAllBySupervisorOrderByEmployee_FullNameAsc(supervisor)
            .stream()
            .map(SupervisorEmployeeBinding::getEmployee)
            .forEach(user -> result.put(user.getId(), assignee(user, "EMPLOYEE")));
        return List.copyOf(result.values());
    }

    /** 建立任務並驗證受派人資格。 */
    public TaskResponse createTask(String username, TaskRequest request) {
        UserAccount creator = requireSupervisor(username);
        UserAccount assignee = validAssignee(creator, request.assigneeId());
        AssignedTask task = new AssignedTask(
            request.name().trim(),
            optional(request.content()),
            request.deadline(),
            creator,
            assignee
        );
        return toTaskResponse(taskDao.save(task));
    }

    /** 依條件查詢目前主管建立的任務並採白名單排序。 */
    @Transactional(readOnly = true)
    public List<TaskResponse> findTasks(
        String username,
        String name,
        String assignee,
        Instant assignedFrom,
        Instant assignedTo,
        Instant deadlineFrom,
        Instant deadlineTo,
        String sortBy,
        String direction
    ) {
        UserAccount creator = requireSupervisor(username);
        validateRange(assignedFrom, assignedTo, "指派日期");
        validateRange(deadlineFrom, deadlineTo, "期限日期");
        String property = SORT_FIELDS.get(sortBy);
        if (property == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支援的排序欄位。");
        }
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
        return taskDao.searchCreatedTasks(
            creator,
            normalize(name),
            normalize(assignee),
            assignedFrom,
            assignedTo,
            deadlineFrom,
            deadlineTo,
            Sort.by(sortDirection, property)
        ).stream().map(this::toTaskResponse).toList();
    }

    /** 修改自己的任務；退回任務修改後重新指派。 */
    public TaskResponse updateTask(String username, UUID id, TaskRequest request) {
        UserAccount creator = requireSupervisor(username);
        AssignedTask task = ownedTask(creator, id);
        if (task.getStatus() == Status.WITHDRAWN) {
            throw conflict("已撤回任務不得修改。");
        }
        task.update(
            request.name().trim(),
            optional(request.content()),
            request.deadline(),
            validAssignee(creator, request.assigneeId())
        );
        return toTaskResponse(task);
    }

    /** 刪除自己建立且未退回的任務。 */
    public void deleteTask(String username, UUID id) {
        AssignedTask task = ownedTask(requireSupervisor(username), id);
        if (task.getStatus() == Status.RETURNED) {
            throw conflict("退回任務須保留原因，不得刪除。");
        }
        taskDao.delete(task);
    }

    /** 撤回自己仍在指派中的任務。 */
    public TaskResponse withdrawTask(String username, UUID id) {
        AssignedTask task = ownedTask(requireSupervisor(username), id);
        if (task.getStatus() != Status.ASSIGNED) {
            throw conflict("只有指派中的任務可撤回。");
        }
        task.withdraw();
        return toTaskResponse(task);
    }

    /** 查詢目前登入者收到的任務。 */
    @Transactional(readOnly = true)
    public List<TaskResponse> inbox(String username) {
        return taskDao.findAllByAssigneeOrderByAssignedAtDesc(currentUser(username)).stream()
            .map(this::toTaskResponse)
            .toList();
    }

    /** 由受派人退回指派中的任務。 */
    public TaskResponse returnTask(String username, UUID id, ReturnRequest request) {
        UserAccount assignee = currentUser(username);
        AssignedTask task = taskDao.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到任務。"));
        if (!task.getAssignee().getId().equals(assignee.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只有受派人可以退回任務。");
        }
        if (task.getStatus() != Status.ASSIGNED) {
            throw conflict("只有指派中的任務可退回。");
        }
        task.returnTask(request.reason().trim());
        return toTaskResponse(task);
    }

    /** 取得目前登入使用者。 */
    private UserAccount currentUser(String username) {
        return userDao.findByUsername(username)
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "找不到登入使用者。"));
    }

    /** 驗證目前使用者具主管資料。 */
    private UserAccount requireSupervisor(String username) {
        UserAccount user = currentUser(username);
        if (!supervisorDao.existsByUser(user)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "目前使用者不是主管。");
        }
        return user;
    }

    /** 取得必要公司綁定。 */
    private CompanyMembership requireMembership(UserAccount user) {
        return membershipDao.findByUser(user)
            .orElseThrow(() -> conflict("請先綁定公司。"));
    }

    /** 驗證兩位使用者屬於同一公司。 */
    private void ensureSameCompany(UserAccount first, UserAccount second) {
        UUID firstCompany = requireMembership(first).getCompany().getId();
        UUID secondCompany = requireMembership(second).getCompany().getId();
        if (!firstCompany.equals(secondCompany)) {
            throw conflict("只能操作同公司的使用者。");
        }
    }

    /** 驗證任務受派人為允許範圍。 */
    private UserAccount validAssignee(UserAccount creator, UUID assigneeId) {
        UserAccount assignee = userDao.findById(assigneeId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到受派人。"));
        ensureSameCompany(creator, assignee);
        boolean valid = creator.getId().equals(assignee.getId())
            || supervisorDao.existsByUser(assignee)
            || employeeBindingDao.existsBySupervisorAndEmployee(creator, assignee);
        if (!valid) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "受派人不是同公司主管或目前主管旗下員工。");
        }
        return assignee;
    }

    /** 取得屬於目前主管的任務。 */
    private AssignedTask ownedTask(UserAccount creator, UUID id) {
        AssignedTask task = taskDao.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "找不到任務。"));
        if (!task.getCreator().getId().equals(creator.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能操作自己建立的任務。");
        }
        return task;
    }

    /** 驗證日期區間順序。 */
    private void validateRange(Instant from, Instant to, String label) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, label + "起日不得晚於迄日。");
        }
    }

    private AssigneeResponse assignee(UserAccount user, String type) {
        return new AssigneeResponse(user.getId(), user.getFullName(), user.getUsername(), type);
    }

    private EmployeeBindingResponse toEmployeeBindingResponse(SupervisorEmployeeBinding binding) {
        return new EmployeeBindingResponse(
            binding.getId(),
            binding.getSupervisor().getId(),
            binding.getSupervisor().getFullName(),
            binding.getEmployee().getId(),
            binding.getEmployee().getFullName(),
            binding.getEmployee().getEmail()
        );
    }

    private TaskResponse toTaskResponse(AssignedTask task) {
        return new TaskResponse(
            task.getId(),
            task.getName(),
            task.getContent(),
            task.getDeadline(),
            task.getCreator().getId(),
            task.getCreator().getFullName(),
            task.getAssignee().getId(),
            task.getAssignee().getFullName(),
            task.getAssignee().getUsername(),
            task.getAssignedAt(),
            task.getStatus().name(),
            task.getReturnReason(),
            task.getReturnedAt(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String optional(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private BusinessException conflict(String message) {
        return new BusinessException(HttpStatus.CONFLICT, message);
    }
}
