package com.agentflow.base.dao;

import com.agentflow.base.model.bo.SupervisorEmployeeBinding;
import com.agentflow.base.model.bo.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupervisorEmployeeBindingDao extends JpaRepository<SupervisorEmployeeBinding, UUID> {
    boolean existsByEmployee(UserAccount employee);
    boolean existsBySupervisorAndEmployee(UserAccount supervisor, UserAccount employee);
    Optional<SupervisorEmployeeBinding> findByEmployee(UserAccount employee);

    @EntityGraph(attributePaths = {"supervisor", "employee"})
    List<SupervisorEmployeeBinding> findAllBySupervisorOrderByEmployee_FullNameAsc(UserAccount supervisor);
}
