package com.agentflow.base.model.bo;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "supervisor_employee_binding",
    uniqueConstraints = @UniqueConstraint(columnNames = "employee_user_id")
)
public class SupervisorEmployeeBinding extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supervisor_user_id", nullable = false)
    private UserAccount supervisor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_user_id", nullable = false)
    private UserAccount employee;

    /** 提供 JPA 建立實體。 */
    protected SupervisorEmployeeBinding() {
    }

    /**
     * 建立主管與員工綁定。
     *
     * @param supervisor 主管使用者
     * @param employee 員工使用者
     */
    public SupervisorEmployeeBinding(UserAccount supervisor, UserAccount employee) {
        this.supervisor = supervisor;
        this.employee = employee;
    }

    /** @return 主管使用者 */
    public UserAccount getSupervisor() {
        return supervisor;
    }

    /** @return 員工使用者 */
    public UserAccount getEmployee() {
        return employee;
    }
}
