package com.agentflow.base.dao;

import com.agentflow.base.model.bo.Company;
import com.agentflow.base.model.bo.CompanyMembership;
import com.agentflow.base.model.bo.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyMembershipDao extends JpaRepository<CompanyMembership, UUID> {
    /**
     * 判斷公司是否仍有成員。
     *
     * @param company 公司
     * @return 是否存在
     */
    boolean existsByCompany(Company company);

    /**
     * 依使用者查找唯一公司綁定。
     *
     * @param user 使用者
     * @return 公司綁定
     */
    Optional<CompanyMembership> findByUser(UserAccount user);

    /**
     * 依公司及主管關鍵字查詢綁定。
     *
     * @param companyName 小寫公司名稱
     * @param supervisorName 小寫主管姓名或帳號
     * @return 綁定列表
     */
    @EntityGraph(attributePaths = {"company", "user"})
    @Query("""
        select membership
        from CompanyMembership membership
        where membership.memberType = com.agentflow.base.model.bo.CompanyMembership.MemberType.SUPERVISOR
          and (:companyName = '' or lower(membership.company.name) like concat('%', :companyName, '%'))
          and (
            :supervisorName = ''
            or lower(membership.user.fullName) like concat('%', :supervisorName, '%')
            or lower(membership.user.username) like concat('%', :supervisorName, '%')
          )
        order by membership.company.name asc, membership.user.fullName asc
        """)
    List<CompanyMembership> searchSupervisorBindings(
        @Param("companyName") String companyName,
        @Param("supervisorName") String supervisorName
    );
}
