package com.agentflow.base.dao;

import com.agentflow.base.model.bo.Company;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyDao extends JpaRepository<Company, UUID> {
    /**
     * 依忽略大小寫的完整名稱查找公司。
     *
     * @param name 公司名稱
     * @return 公司
     */
    Optional<Company> findByNameIgnoreCase(String name);

    /**
     * 依名稱片段查找並排序公司。
     *
     * @param name 公司名稱片段
     * @return 公司列表
     */
    List<Company> findAllByNameContainingIgnoreCaseOrderByNameAsc(String name);

    /**
     * 依名稱排序取得所有公司。
     *
     * @return 公司列表
     */
    List<Company> findAllByOrderByNameAsc();
}
