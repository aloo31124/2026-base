package com.agentflow.base.dao;

import com.agentflow.base.model.bo.SupervisorProfile;
import com.agentflow.base.model.bo.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupervisorProfileDao extends JpaRepository<SupervisorProfile, UUID> {
    /**
     * 依使用者查找主管資料。
     *
     * @param user 使用者
     * @return 主管資料
     */
    Optional<SupervisorProfile> findByUser(UserAccount user);

    /**
     * 判斷使用者是否已有主管資料。
     *
     * @param user 使用者
     * @return 是否存在
     */
    boolean existsByUser(UserAccount user);

    /**
     * 依姓名、帳號或職稱查詢主管。
     *
     * @param keyword 小寫查詢字串
     * @return 主管列表
     */
    @EntityGraph(attributePaths = "user")
    @Query("""
        select supervisor
        from SupervisorProfile supervisor
        where :keyword = ''
           or lower(supervisor.user.fullName) like concat('%', :keyword, '%')
           or lower(supervisor.user.username) like concat('%', :keyword, '%')
           or lower(supervisor.title) like concat('%', :keyword, '%')
        order by supervisor.user.fullName asc
        """)
    List<SupervisorProfile> search(@Param("keyword") String keyword);
}
