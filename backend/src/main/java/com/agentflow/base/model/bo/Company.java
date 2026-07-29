package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "company")
public class Company extends BaseEntity {
    @Nationalized
    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 提供 JPA 建立公司實體。
     */
    protected Company() {
    }

    /**
     * 建立公司主檔。
     *
     * @param name 公司名稱
     * @param description 公司說明
     */
    public Company(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 更新公司可編輯資料。
     *
     * @param name 公司名稱
     * @param description 公司說明
     */
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 取得公司名稱。
     *
     * @return 公司名稱
     */
    public String getName() {
        return name;
    }

    /**
     * 取得公司說明。
     *
     * @return 公司說明
     */
    public String getDescription() {
        return description;
    }
}
